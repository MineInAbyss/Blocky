package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.api.BlockyFurnitures.blockyFurniture
import com.mineinabyss.blocky.api.BlockyFurnitures.blockyFurnitureEntity
import com.mineinabyss.blocky.api.BlockyFurnitures.blockySeat
import com.mineinabyss.blocky.api.BlockyFurnitures.isFurnitureHitbox
import com.mineinabyss.blocky.api.BlockyFurnitures.removeFurniture
import com.mineinabyss.blocky.api.events.furniture.BlockyFurniturePlaceEvent
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.core.BlockyFurnitureHitbox
import com.mineinabyss.blocky.components.core.BlockyInfo
import com.mineinabyss.blocky.components.features.BlockyPlacableOn
import com.mineinabyss.blocky.components.features.BlockySeat
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import io.th0rgal.protectionlib.ProtectionLib
import org.bukkit.GameMode
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.*
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot

class BlockyFurnitureListener : Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.prePlacingFurniture() {
        val (block, item, hand) = (clickedBlock ?: return) to (item ?: return) to (hand ?: return)
        val targetBlock = getTargetBlock(block, blockFace) ?: return
        val gearyEntity = player.gearyInventory?.get(hand) ?: return
        val furniture = gearyEntity.get<BlockyFurniture>() ?: return
        val rotation = getRotation(player.location.yaw, furniture)
        val yaw = if (furniture.hasStrictRotation) getYaw(rotation) else player.location.yaw

        if (action != Action.RIGHT_CLICK_BLOCK || player.gameMode == GameMode.ADVENTURE) return
        if (!furniture.hasEnoughSpace(targetBlock.location, yaw)) return
        when {
            !furniture.hasEnoughSpace(targetBlock.location, yaw) -> return
            !ProtectionLib.canBuild(player, targetBlock.location) -> return
            gearyEntity.get<BlockyPlacableOn>()?.isPlacableOn(targetBlock, blockFace) == false -> return
            targetBlock.getRelative(BlockFace.DOWN).isVanillaNoteBlock -> return
        }

        val newFurniture = placeBlockyFurniture(gearyEntity, targetBlock.location, rotation, yaw, item) ?: return

        if (!BlockyFurniturePlaceEvent(newFurniture, player).callEvent()) {
            removeFurniture(newFurniture)
            return
        }

        player.swingHand(hand)
        if (player.gameMode != GameMode.CREATIVE) player.inventory.itemInMainHand.subtract()
        setUseInteractedBlock(Event.Result.DENY)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun BlockBreakEvent.onBreakingHitbox() {
        if (!block.isFurnitureHitbox || player.gameMode != GameMode.CREATIVE) return
        block.blockyFurnitureEntity?.let { removeFurniture(it, player) }
    }

    @EventHandler(ignoreCancelled = true)
    fun EntityDamageByEntityEvent.onBreakingFurniture() {
        val furniture = (entity as? Interaction)?.baseFurniture ?: this as? ItemDisplay ?: return
        if (furniture.toGearyOrNull()?.get<BlockyInfo>()?.isUnbreakable == true) isCancelled = true
        else (damager as? Player)?.let { removeFurniture(furniture, it) } ?: removeFurniture(furniture)
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun PlayerInteractEntityEvent.onSitting() {
        val entity = rightClicked as? Interaction ?: return
        if (!ProtectionLib.canInteract(player, entity.location)) return
        if (!entity.isFurnitureHitbox || player.isSneaking) return

        player.sitOnBlockySeat(entity)
        isCancelled = true
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun PlayerInteractEvent.onSitting() {
        val block = clickedBlock ?: return
        if (action != Action.RIGHT_CLICK_BLOCK || hand != EquipmentSlot.HAND) return
        if (!ProtectionLib.canInteract(player, block.location)) return
        if (!block.isFurnitureHitbox || player.isSneaking) return

        player.sitOnBlockySeat(block)
        isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun ProjectileHitEvent.onProjectileHit() {
        if (hitBlock?.isFurnitureHitbox != true) return
        val furniture = hitEntity?.let { (hitEntity as? Interaction)?.baseFurniture ?: this as? ItemDisplay ?: return }

        (entity.shooter as? Player).let { player ->
            (hitBlock?.location ?: hitEntity?.location)?.let { loc ->
                when {
                    player?.let { ProtectionLib.canBuild(it, loc) } == true ->
                        isCancelled = true
                    entity is Explosive -> {
                        isCancelled = true
                        attemptBreakBlockyBlock(loc.block, player)
                        Unit
                    }
                    //TODO Fix this
                    furniture != null -> {
                        isCancelled = true
                        if (furniture.toGearyOrNull()?.get<BlockyFurnitureHitbox>()?.hitbox?.isNotEmpty() == true)
                            removeFurniture(furniture)
                    }
                    else -> {}
                }
            }
        }

        if (entity is Explosive) furniture?.let { removeFurniture(furniture) }
        else isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockExplodeEvent.onBlockExplode() {
        blockList().filter { it.isFurnitureHitbox && it.blockyFurniture != null }
            .mapNotNull { it.blockyFurnitureEntity }.toSet()
            .forEach { removeFurniture(it) }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerQuitEvent.onQuit() {
        val seat = player.vehicle as? ArmorStand ?: return
        seat.toGearyOrNull()?.has<BlockySeat>() ?: return || return
        player.leaveVehicle()
    }

    private fun Player.sitOnBlockySeat(block: Block) {
        block.blockySeat?.let {
            if (this.passengers.isEmpty()) it.addPassenger(this)
        }
    }

    private fun Player.sitOnBlockySeat(entity: Interaction) {
        entity.blockySeat?.let {
            if (this.passengers.isEmpty()) it.addPassenger(this)
        }
    }
}
