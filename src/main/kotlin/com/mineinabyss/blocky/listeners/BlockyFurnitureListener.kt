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
import com.mineinabyss.blocky.components.features.furniture.BlockySeat
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.geary.papermc.tracking.entities.events.GearyEntityAddToWorldEvent
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.messaging.logSuccess
import com.mineinabyss.idofront.plugin.Plugins
import com.ticxo.modelengine.api.events.BaseEntityInteractEvent
import io.papermc.paper.event.packet.PlayerChunkLoadEvent
import io.papermc.paper.event.packet.PlayerChunkUnloadEvent
import io.th0rgal.protectionlib.ProtectionLib
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
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
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.util.Vector
import kotlin.math.pow

class BlockyFurnitureListener : Listener {

    @EventHandler(priority = EventPriority.HIGH)
    fun PlayerChunkLoadEvent.onLoadChunk() {
        chunk.entities.filterIsInstance<ItemDisplay>().forEach {
            FurniturePacketHelpers.sendInteractionEntityPacket(it, player)
            FurniturePacketHelpers.sendCollisionHitboxPacket(it, player)
            FurniturePacketHelpers.sendLightPacket(it, player)
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun GearyEntityAddToWorldEvent.onAddFurniture() {
        val furniture = entity as? ItemDisplay ?: return
        val simulationDistance = (Bukkit.getServer().simulationDistance * 16.0).pow(2)
        blocky.plugin.server.onlinePlayers.filterNotNull().filter {
            it.world == entity.world && it.location.distanceSquared(entity.location) < simulationDistance
        }.forEach { player ->
            blocky.plugin.launch(blocky.plugin.minecraftDispatcher) {
                delay(1)
                FurniturePacketHelpers.sendInteractionEntityPacket(furniture, player)
                FurniturePacketHelpers.sendCollisionHitboxPacket(furniture, player)
                FurniturePacketHelpers.sendLightPacket(furniture, player)
            }
        }
    }

    @EventHandler
    fun PlayerChunkUnloadEvent.onUnloadChunk() {
        chunk.entities.filterIsInstance<ItemDisplay>().forEach {
            FurniturePacketHelpers.removeInteractionHitboxPacket(it, player)
            FurniturePacketHelpers.removeHitboxOutlinePacket(it, player)
            FurniturePacketHelpers.removeCollisionHitboxPacket(it, player)
            FurniturePacketHelpers.removeLightPacket(it, player)
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun EntityRemoveFromWorldEvent.onRemoveFurniture() {
        val entity = entity as? ItemDisplay ?: return
        FurniturePacketHelpers.removeInteractionHitboxPacket(entity)
        FurniturePacketHelpers.removeHitboxOutlinePacket(entity)
        FurniturePacketHelpers.removeCollisionHitboxPacket(entity)
        FurniturePacketHelpers.removeLightPacket(entity)
    }

    @EventHandler
    fun PlayerChangedWorldEvent.onChangeWorld() {
        from.entities.filterIsInstance<ItemDisplay>().forEach {
            FurniturePacketHelpers.removeInteractionHitboxPacket(it, player)
            FurniturePacketHelpers.removeHitboxOutlinePacket(it, player)
            FurniturePacketHelpers.removeCollisionHitboxPacket(it, player)
            FurniturePacketHelpers.removeLightPacket(it, player)
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.prePlacingFurniture() {
        val (block, item, hand) = (clickedBlock ?: return) to (item ?: return) to (hand ?: return)
        val targetBlock = FurnitureHelpers.targetBlock(block, blockFace) ?: return
        val gearyEntity = player.gearyInventory?.get(hand) ?: return
        val furniture = gearyEntity.get<BlockyFurniture>() ?: return
        val yaw = if (furniture.hasStrictRotation) FurnitureHelpers.yaw(
            FurnitureHelpers.rotation(
                player.yaw,
                furniture
            )
        ) else player.yaw

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

        player.swingHand(hand)
        if (player.gameMode != GameMode.CREATIVE) player.inventory.getItem(hand).subtract(1)
        setUseInteractedBlock(Event.Result.DENY)
    }

    @EventHandler
    fun PlayerUseUnknownEntityEvent.onInteract() {
        val baseFurniture = FurniturePacketHelpers.getBaseFurnitureFromInteractionEntity(entityId) ?: return
        blocky.plugin.launch(blocky.plugin.minecraftDispatcher) {
            when {
                isAttack -> BlockyFurnitures.removeFurniture(baseFurniture, player)
                else -> BlockyFurnitureInteractEvent(
                    baseFurniture, player, hand, player.inventory.itemInMainHand, clickedRelativePosition
                ).callEvent()
            }
        }
    }

    init {
        if (Plugins.isEnabled("ModelEngine")) {
            logSuccess("ModelEngine detected, enabling ModelEngine-Furniture-Interaction Listener!")
            Bukkit.getPluginManager().registerEvents(object : Listener {
                @EventHandler
                fun BaseEntityInteractEvent.onModelEngineInteract() {
                    val baseEntity = (baseEntity.original as? ItemDisplay)?.takeIf { it.isBlockyFurniture } ?: return
                    when {
                        action == BaseEntityInteractEvent.Action.ATTACK -> BlockyFurnitures.removeFurniture(baseEntity, player)
                        else -> BlockyFurnitureInteractEvent(baseEntity, player, slot, player.inventory.itemInMainHand, clickedPosition).callEvent()
                    }
                }
            }, blocky.plugin)
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun BlockyFurnitureInteractEvent.onSitting() {
        if (!ProtectionLib.canInteract(player, entity.location) || player.isSneaking) return

        player.sitOnBlockySeat(baseEntity, baseEntity.location.add(clickedRelativePosition ?: Vector()))
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerQuitEvent.onQuit() {
        (player.vehicle as? ArmorStand)?.toGearyOrNull()?.get<BlockySeat>() ?: return
        player.leaveVehicle()
    }

    private fun Player.sitOnBlockySeat(entity: ItemDisplay, location: Location = entity.location) {
        BlockyFurnitures.blockySeat(entity, location)?.addPassenger(this)
    }
}
