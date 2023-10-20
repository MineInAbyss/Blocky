package com.mineinabyss.blocky.listeners

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.mineinabyss.blocky.api.BlockyFurnitures.getBlockySeat
import com.mineinabyss.blocky.api.BlockyFurnitures.isBlockyFurniture
import com.mineinabyss.blocky.api.BlockyFurnitures.removeFurniture
import com.mineinabyss.blocky.api.events.furniture.BlockyFurnitureInteractEvent
import com.mineinabyss.blocky.api.events.furniture.BlockyFurniturePlaceEvent
import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.features.BlockyPlacableOn
import com.mineinabyss.blocky.components.features.furniture.BlockySeat
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.helpers.prefabs
import com.mineinabyss.idofront.messaging.broadcast
import io.papermc.paper.event.packet.PlayerChunkLoadEvent
import io.th0rgal.protectionlib.ProtectionLib
import kotlinx.coroutines.delay
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent

class BlockyFurnitureListener : Listener {

    @EventHandler(priority = EventPriority.HIGH)
    fun PlayerChunkLoadEvent.onLoadChunk() {
        chunk.entities.filterIsInstance<ItemDisplay>().forEach {
            FurniturePacketHelpers.sendInteractionEntityPacket(it, player)
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun EntityAddToWorldEvent.onAddFurniture() {
        val entity = entity as? ItemDisplay ?: return
        blocky.plugin.launch(blocky.plugin.minecraftDispatcher) {
            delay(1)
            if (!entity.isBlockyFurniture) return@launch
            FurniturePacketHelpers.sendInteractionEntityPacket(entity)
            FurniturePacketHelpers.sendCollisionHitboxPacket(entity)
            FurniturePacketHelpers.sendLightPacket(entity)
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun EntityRemoveFromWorldEvent.onRemoveFurniture() {
        val entity = entity as? ItemDisplay ?: return
        if (!entity.isBlockyFurniture) return
        FurniturePacketHelpers.removeInteractionHitboxPacket(entity)
        FurniturePacketHelpers.removeCollisionHitboxPacket(entity)
        FurniturePacketHelpers.removeLightPacket(entity)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.prePlacingFurniture() {
        val (block, item, hand) = (clickedBlock ?: return) to (item ?: return) to (hand ?: return)
        val targetBlock = FurnitureHelpers.getTargetBlock(block, blockFace) ?: return
        val gearyEntity = player.gearyInventory?.get(hand) ?: return
        val furniture = gearyEntity.get<BlockyFurniture>() ?: return
        val yaw = if (furniture.hasStrictRotation) FurnitureHelpers.getYaw(FurnitureHelpers.getRotation(player.yaw, furniture)) else player.yaw

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
            removeFurniture(newFurniture)
            return
        }

        player.swingHand(hand)
        if (player.gameMode != GameMode.CREATIVE) player.inventory.itemInMainHand.subtract()
        setUseInteractedBlock(Event.Result.DENY)
    }

    @EventHandler
    fun PlayerUseUnknownEntityEvent.onInteract() {
        broadcast(this.entityId)
        val baseFurniture = FurniturePacketHelpers.getBaseFurnitureFromInteractionEntity(entityId) ?: return
        blocky.plugin.launch(blocky.plugin.minecraftDispatcher) {
            when {
                isAttack -> removeFurniture(baseFurniture, player)
                else ->  BlockyFurnitureInteractEvent(
                    baseFurniture, player,
                    hand, player.inventory.itemInMainHand,
                    null, null
                ).callEvent()
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun BlockyFurnitureInteractEvent.onSitting() {
        if (!ProtectionLib.canInteract(player, entity.location) || player.isSneaking) return

        player.sitOnBlockySeat(baseEntity, clickedBlock?.location ?: baseEntity.location)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerQuitEvent.onQuit() {
        (player.vehicle as? ArmorStand)?.toGearyOrNull()?.get<BlockySeat>() ?: return
        player.leaveVehicle()
    }

    private fun Player.sitOnBlockySeat(entity: ItemDisplay, location: Location = entity.location) {
        if (this.passengers.isEmpty()) getBlockySeat(entity, location)?.addPassenger(this)
    }
}
