package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.api.BlockyFurnitures.blockyFurniture
import com.mineinabyss.blocky.api.BlockyFurnitures.blockyFurnitureEntity
import com.mineinabyss.blocky.api.BlockyFurnitures.blockySeat
import com.mineinabyss.blocky.api.BlockyFurnitures.isBlockyFurniture
import com.mineinabyss.blocky.api.BlockyFurnitures.isFurnitureHitbox
import com.mineinabyss.blocky.api.BlockyFurnitures.removeBlockyFurniture
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.core.BlockyInfo
import com.mineinabyss.blocky.components.features.BlockySeat
import com.mineinabyss.blocky.helpers.attemptBreakBlockyBlock
import com.mineinabyss.blocky.helpers.getTargetBlock
import com.mineinabyss.blocky.helpers.placeBlockyFurniture
import com.mineinabyss.blocky.itemProvider
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.nms.aliases.toNMS
import io.th0rgal.protectionlib.ProtectionLib
import org.bukkit.GameMode
import org.bukkit.block.Block
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Explosive
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot

class BlockyFurnitureListener : Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.prePlacingFurniture() {
        val targetBlock = clickedBlock?.let { getTargetBlock(it, blockFace) } ?: return
        if (action != Action.RIGHT_CLICK_BLOCK || hand != EquipmentSlot.HAND) return

        item?.toNMS()?.let { itemProvider.deserializeItemStackToEntity(it, player.toGeary()) }?.placeBlockyFurniture(player, targetBlock.location, blockFace, item!!)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun BlockBreakEvent.onBreakingHitbox() {
        if (!block.isFurnitureHitbox || player.gameMode != GameMode.CREATIVE) return
        block.blockyFurnitureEntity?.removeBlockyFurniture(player)
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun PlayerInteractEvent.onSitting() {
        val block = clickedBlock ?: return
        if (action != Action.RIGHT_CLICK_BLOCK || hand != EquipmentSlot.HAND) return
        if (!ProtectionLib.canInteract(player, block.location)) return
        if (!block.isFurnitureHitbox || player.isSneaking) return

        player.sitOnBlockySeat(block)
        if (!player.inventory.itemInMainHand.type.isAir) isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun ProjectileHitEvent.onProjectileHit() {
        if ((hitBlock?.isFurnitureHitbox != true || hitEntity?.isBlockyFurniture != true)) return

        (entity.shooter as? Player).let { player ->
            (hitBlock?.location ?: hitEntity?.location)?.let { loc ->
                when {
                    player?.let { ProtectionLib.canBuild(it, loc) } == true ->
                        isCancelled = true
                    entity is Explosive -> {
                        isCancelled = true
                        loc.block.attemptBreakBlockyBlock(player)
                    }
                    hitEntity?.isBlockyFurniture == true -> {
                        isCancelled = true
                        if (entity.toGeary().get<BlockyFurniture>()?.collisionHitbox?.isNotEmpty() == true)
                            loc.block.attemptBreakBlockyBlock(player)
                    }
                }
            }
        }


        if (entity is Explosive) {
            (hitEntity as ItemFrame).removeBlockyFurniture(null)
        } else isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockExplodeEvent.onBlockExplode() {
        blockList().filter { it.isFurnitureHitbox && it.blockyFurniture != null }
            .map { it.blockyFurnitureEntity }.toSet()
            .forEach { it?.removeBlockyFurniture(null) }
    }

    @EventHandler(ignoreCancelled = true)
    fun EntityDamageByEntityEvent.onBreakingFrame() {
        if (!entity.isBlockyFurniture) return
        else if (entity.toGearyOrNull()?.get<BlockyInfo>()?.isUnbreakable == true) isCancelled = true
        else entity.removeBlockyFurniture(damager as? Player)
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
}
