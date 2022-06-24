package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.components.*
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
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
        val item = itemStack?.toGearyOrNull(player) ?: return
        if (item.get<BlockyEntity>()?.entityType == EntityType.ITEM_FRAME) isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerInteractEvent.prePlacingItemFrame() {
        val gearyItem = item?.toGearyOrNull(player) ?: return
        val blockyEntity = gearyItem.get<BlockyEntity>() ?: return
        val against = clickedBlock ?: return
        val targetBlock = getTargetBlock(against, blockFace) ?: return
        val targetData = targetBlock.blockData

        if (action != Action.RIGHT_CLICK_BLOCK) return
        if (hand != EquipmentSlot.HAND) return
        if (blockyEntity.entityType != EntityType.ITEM_FRAME) return
        targetBlock.setType(Material.AIR, false)
        val blockyPlace = BlockPlaceEvent(targetBlock, targetBlock.state, against, item!!, player, true, hand!!)
        val frameRotation = getRotation(player.eyeLocation.yaw, blockyEntity.collisionHitbox.isNotEmpty())
        val frameYaw = getYaw(frameRotation)

        if (!blockyEntity.hasEnoughSpace(targetBlock.location, frameYaw)) {
            blockyPlace.isCancelled = true
            player.error("There is not enough space to place this block here.")
            return
        }
        blockyPlace.callEvent()
        if (!blockyPlace.canBuild() || blockyPlace.isCancelled) {
            targetBlock.setBlockData(targetData, false)
            return
        }

        gearyItem.placeBlockyFrame(frameRotation, frameYaw, blockFace, targetBlock.location, player)
        player.swingMainHand()
        if (player.gameMode != GameMode.CREATIVE) item!!.subtract()
    }

    @EventHandler
    fun HangingBreakEvent.onBreakHanging() {
        if (cause == HangingBreakEvent.RemoveCause.ENTITY) return
        if (entity.toGearyOrNull() != null) isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockBreakEvent.onBreakingBarrier() {
        if (block.type != Material.BARRIER || player.gameMode != GameMode.CREATIVE) return

        val frames = block.location.getNearbyEntitiesByType(ItemFrame::class.java, 10.0)
        frames.forEach { frame ->
            val gearyFrame = frame.toGeary()
            val gearyItem = frame.item.toGearyOrNull(player) ?: return@forEach

            if (gearyFrame.checkFrameHitbox(block.location)) {
                gearyFrame.get<BlockyBarrierHitbox>()?.barriers?.forEach { barrierLoc ->
                    barrierLoc.block.type = Material.AIR
                    removeBlockLight(barrierLoc)
                }
                if (gearyFrame.has<BlockySeatLocations>()) {
                    gearyFrame.get<BlockySeatLocations>()?.seats?.forEach seatLoc@{ seatLoc ->
                        seatLoc.getNearbyEntitiesByType(
                            ArmorStand::class.java,
                            gearyItem.get<BlockySeat>()?.heightOffset?.times(2) ?: return@forEach
                        ).forEach seat@{ stand ->
                            if (stand.toGeary().has<BlockySeat>()) stand.remove()
                            return@seat
                        }
                        return@seatLoc
                    }
                }
                removeBlockLight(frame.location)
                frame.remove()
                return
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun EntityDamageByEntityEvent.onBreakingFrame() {
        if (entity !is ItemFrame) return
        val gearyEntity = entity.toGearyOrNull() ?: return
        if (!gearyEntity.has<BlockyEntity>() || gearyEntity.has<BlockyInfo>()) return

        if (gearyEntity.get<BlockyInfo>()!!.isUnbreakable) isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerInteractEntityEvent.onRotatingFrame() {
        if (rightClicked is ItemFrame && rightClicked.toGearyOrNull()?.has<BlockyEntity>() == true) isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerInteractEvent.onSitting() {
        if (action != Action.RIGHT_CLICK_BLOCK || hand != EquipmentSlot.HAND) return
        val block = clickedBlock ?: return

        if (block.type != Material.BARRIER || player.isSneaking) return

        val frames = block.location.getNearbyEntitiesByType(ItemFrame::class.java, 20.0)
        frames.forEach { frame ->
            if (frame.toGeary().checkFrameHitbox(block.location) && frame.toGeary().has<BlockySeatLocations>()) {
                val stand =
                    block.location.getNearbyEntitiesByType(ArmorStand::class.java, 1.0).firstOrNull() ?: return@forEach
                if (stand.passengers.isEmpty()) stand.addPassenger(player)
            }
        }
        isCancelled = player.inventory.itemInMainHand.type != Material.AIR
    }

}
