package com.mineinabyss.blocky.listeners

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.mineinabyss.blocky.api.BlockyFurnitures
import com.mineinabyss.blocky.api.BlockyFurnitures.isBlockyFurniture
import com.mineinabyss.blocky.api.events.furniture.BlockyFurnitureInteractEvent
import com.mineinabyss.blocky.api.events.furniture.BlockyFurniturePlaceEvent
import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.features.BlockyPlacableOn
import com.mineinabyss.blocky.components.features.furniture.BlockySeats
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.plugin.Plugins
import com.mineinabyss.idofront.util.to
import com.ticxo.modelengine.api.events.BaseEntityInteractEvent
import io.papermc.paper.event.packet.PlayerChunkLoadEvent
import io.papermc.paper.event.packet.PlayerChunkUnloadEvent
import io.papermc.paper.event.player.PlayerTrackEntityEvent
import io.papermc.paper.event.player.PlayerUntrackEntityEvent
import io.th0rgal.protectionlib.ProtectionLib
import org.bukkit.*
import org.bukkit.block.BlockFace
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.util.Vector

class BlockyFurnitureListener : Listener {

    @EventHandler(priority = EventPriority.HIGH)
    fun PlayerTrackEntityEvent.onTrackEntity() {
        val baseEntity = entity as? ItemDisplay ?: return
        blocky.plugin.launch {
            delay(2.ticks)
            FurniturePacketHelpers.sendInteractionEntityPacket(baseEntity, player)
            FurniturePacketHelpers.sendCollisionHitboxPacket(baseEntity, player)
            FurniturePacketHelpers.sendLightPacket(baseEntity, player)
        }
    }

    @EventHandler
    fun PlayerUntrackEntityEvent.onUntrackEntity() {
        val baseEntity = entity as? ItemDisplay ?: return
        FurniturePacketHelpers.removeInteractionHitboxPacket(baseEntity, player)
        FurniturePacketHelpers.removeHitboxOutlinePacket(baseEntity, player)
        FurniturePacketHelpers.removeCollisionHitboxPacket(baseEntity, player)
        FurniturePacketHelpers.removeLightPacket(baseEntity, player)
    }

    @EventHandler
    fun EntityRemoveFromWorldEvent.onRemoveEntity() {
        val baseEntity = entity as? ItemDisplay ?: return
        FurniturePacketHelpers.removeInteractionHitboxPacket(baseEntity)
        FurniturePacketHelpers.removeHitboxOutlinePacket(baseEntity)
        FurniturePacketHelpers.removeCollisionHitboxPacket(baseEntity)
        FurniturePacketHelpers.removeLightPacket(baseEntity)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.prePlacingFurniture() {
        val (block, item, hand) = (clickedBlock ?: return) to (item ?: return) to (hand ?: return)
        val targetBlock = FurnitureHelpers.targetBlock(block, blockFace) ?: return
        val gearyEntity = player.gearyInventory?.get(hand) ?: return
        val furniture = gearyEntity.get<BlockyFurniture>() ?: return
        val yaw = if (furniture.hasStrictRotation)
            FurnitureHelpers.yaw(FurnitureHelpers.rotation(player.yaw, furniture))
        else player.yaw

        when {
            action != Action.RIGHT_CLICK_BLOCK || player.gameMode == GameMode.ADVENTURE -> return
            !FurnitureHelpers.hasEnoughSpace(furniture, targetBlock.location, yaw) -> return
            !ProtectionLib.canBuild(player, targetBlock.location) -> return
            gearyEntity.get<BlockyPlacableOn>()?.isPlacableOn(targetBlock, blockFace) == false -> return
            targetBlock.getRelative(BlockFace.DOWN).isVanillaNoteBlock -> return
        }

        val prefabKey = gearyEntity.prefabs.firstOrNull()?.get<PrefabKey>() ?: gearyEntity.get<PrefabKey>() ?: return
        val newFurniture = FurnitureHelpers.placeBlockyFurniture(prefabKey, targetBlock.location, yaw, item) ?: return

        if (!BlockyFurniturePlaceEvent(newFurniture, player, hand, item).callEvent()) {
            BlockyFurnitures.removeFurniture(newFurniture)
            return
        }

        newFurniture.location.block.type = Material.AIR

        player.swingHand(hand)
        if (player.gameMode != GameMode.CREATIVE) player.inventory.getItem(hand).subtract(1)
        setUseInteractedBlock(Event.Result.DENY)
        player.world.sendGameEvent(null, GameEvent.BLOCK_PLACE, newFurniture.location.toVector())
    }

    @EventHandler
    fun PlayerUseUnknownEntityEvent.onInteract() {
        val baseFurniture = FurniturePacketHelpers.baseFurnitureFromInteractionHitbox(entityId) ?: return
        blocky.plugin.launch(blocky.plugin.minecraftDispatcher) {
            when {
                isAttack -> BlockyFurnitures.removeFurniture(baseFurniture, player)
                else -> BlockyFurnitureInteractEvent(
                    baseFurniture, player, hand, player.inventory.itemInMainHand, baseFurniture.location.add(clickedRelativePosition ?: Vector())
                ).callEvent()
            }
        }
    }

    @EventHandler
    fun PlayerInteractEvent.onInteract() {
        val baseFurniture = FurniturePacketHelpers.baseFurnitureFromCollisionHitbox(interactionPoint?.block?.toBlockPos() ?: return) ?: return
        BlockyFurnitureInteractEvent(baseFurniture, player, hand!!, player.inventory.itemInMainHand, interactionPoint ?: baseFurniture.location).callEvent()
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun BlockDamageEvent.onCollisionHitboxDamage() {
        //TODO Implement custom break speed logic for this
        // Should work no issues
        FurniturePacketHelpers.baseFurnitureFromCollisionHitbox(block.toBlockPos())?.let {
            BlockyFurnitures.removeFurniture(it, player)
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun BlockBreakEvent.onCollisionHitboxBreak() {
        // Mainly for players in creative-mode
        FurniturePacketHelpers.baseFurnitureFromCollisionHitbox(block.toBlockPos())?.let {
            BlockyFurnitures.removeFurniture(it, player)
        }
    }

    init {
        if (Plugins.isEnabled("ModelEngine")) {
            blocky.logger.s("ModelEngine detected, enabling ModelEngine-Furniture-Interaction Listener!")
            Bukkit.getPluginManager().registerEvents(object : Listener {
                @EventHandler
                fun BaseEntityInteractEvent.onModelEngineInteract() {
                    val baseEntity = (baseEntity.original as? ItemDisplay)?.takeIf { it.isBlockyFurniture } ?: return
                    when {
                        action == BaseEntityInteractEvent.Action.ATTACK -> BlockyFurnitures.removeFurniture(baseEntity, player)
                        else -> BlockyFurnitureInteractEvent(baseEntity, player, slot, player.inventory.itemInMainHand, baseEntity.location.add(clickedPosition ?: Vector())).callEvent()
                    }
                }
            }, blocky.plugin)
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun BlockyFurnitureInteractEvent.onSitting() {
        if (!ProtectionLib.canInteract(player, entity.location) || player.isSneaking) return

        player.sitOnBlockySeat(baseEntity, interactionPoint)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerQuitEvent.onQuit() {
        (player.vehicle as? ArmorStand)?.toGearyOrNull()?.get<BlockySeats>() ?: return
        player.leaveVehicle()
    }

    private fun Player.sitOnBlockySeat(entity: ItemDisplay, location: Location = entity.location) {
        BlockyFurnitures.blockySeat(entity, location)?.addPassenger(this)
    }
}
