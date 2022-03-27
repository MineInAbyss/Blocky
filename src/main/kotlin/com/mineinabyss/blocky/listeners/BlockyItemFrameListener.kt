package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.components.BlockyBarrierHitbox
import com.mineinabyss.blocky.components.BlockyEntity
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.blocky.components.EntityType
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.hanging.HangingBreakEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class BlockyItemFrameListener : Listener {

    @EventHandler
    fun HangingPlaceEvent.onPlacingItemFrame() {
        val player = player ?: return
        if (itemStack?.toGearyOrNull(player)?.get<BlockyEntity>()?.entityType == EntityType.ITEM_FRAME) isCancelled =
            true
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.prePlacingItemFrame() {
        if (action != Action.RIGHT_CLICK_BLOCK) return
        if (hand != EquipmentSlot.HAND) return

        val gearyItem = item?.toGearyOrNull(player) ?: return
        val blockyEntity = gearyItem.get<BlockyEntity>() ?: return
        val against = clickedBlock ?: return
        val targetBlock = getTargetBlock(against, blockFace) ?: return
        val targetData = targetBlock.blockData;

        if (blockyEntity.entityType != EntityType.ITEM_FRAME) return

        targetBlock.setType(Material.AIR, false)
        val blockyPlace = BlockPlaceEvent(targetBlock, targetBlock.state, against, item!!, player, true, hand!!)
        val frameRotation =
            getRotation(
                player.eyeLocation.yaw,
                blockyEntity.hasBarrierCollision() && blockyEntity.collisionHitbox.size > 1
            )
        val frameYaw = getYaw(frameRotation)

        if (!blockyEntity.hasEnoughSpace(frameYaw, targetBlock.location)) {
            blockyPlace.isCancelled = true
            player.error("There is not enough space for this block here.")
        }
        blockyPlace.callEvent()

        if (!blockyPlace.canBuild() || blockyPlace.isCancelled) {
            targetBlock.setBlockData(targetData, false)
            return
        }

        gearyItem.placeBlockyFrame(frameRotation, frameYaw, blockFace, targetBlock.location)
        if (player.gameMode != GameMode.CREATIVE) item!!.subtract()
    }

    @EventHandler
    fun HangingBreakEvent.onBreakHanging() {
        if (cause == HangingBreakEvent.RemoveCause.ENTITY) return
        if (entity.toGearyOrNull()?.has<BlockyEntity>() == true) isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockBreakEvent.onBreakingBarrier() {
        if (block.type != Material.BARRIER || player.gameMode != GameMode.CREATIVE) return

        val frames = block.location.getNearbyEntitiesByType(ItemFrame::class.java, 20.0)
        frames.forEach { frame ->
            if (frame.checkFrameHitbox(block.location)) {
                frame.toGeary().get<BlockyBarrierHitbox>()?.barriers?.forEach {
                    it.block.type = Material.AIR
                    removeBlockLight(it)
                }
                frame.remove()
                return
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun EntityDamageByEntityEvent.onBreakingFrame() {
        if (entity !is ItemFrame) return
        if (damager !is Player) return
        val gearyEntity = entity.toGearyOrNull() ?: return
        val blockyEntity = gearyEntity.get<BlockyEntity>() ?: return
        val blockyInfo = gearyEntity.get<BlockyInfo>() ?: return
    }


    @EventHandler(ignoreCancelled = true)
    fun PlayerInteractEntityEvent.onRotatingFrame() {
        if (rightClicked is ItemFrame && rightClicked.toGearyOrNull()?.has<BlockyEntity>() == true) isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerInteractEvent.onInteractFrame() {
        if (action != Action.RIGHT_CLICK_BLOCK || hand != EquipmentSlot.HAND) return
        val block = clickedBlock ?: return

        if (block.type != Material.BARRIER || player.isSneaking) return

        val frame = getFrame(block.location)
        //TODO Implement sititng
    }

}