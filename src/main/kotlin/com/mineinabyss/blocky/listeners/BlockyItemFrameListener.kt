package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.components.BlockyEntity
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.blocky.components.BlockySeat
import com.mineinabyss.blocky.components.EntityType
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Explosive
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.hanging.HangingBreakEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
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
        val item = item ?: return
        val hand = hand ?: return
        val gearyItem = item.toGearyOrNull(player) ?: return
        val blockyEntity = gearyItem.get<BlockyEntity>() ?: return
        val against = clickedBlock ?: return
        val targetBlock = getTargetBlock(against, blockFace) ?: return
        val targetData = targetBlock.blockData

        if (action != Action.RIGHT_CLICK_BLOCK || hand != EquipmentSlot.HAND) return
        if (blockyEntity.entityType != EntityType.ITEM_FRAME) return
        targetBlock.setType(Material.AIR, false)
        val blockyPlace = BlockPlaceEvent(targetBlock, targetBlock.state, against, item, player, true, hand)
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
        if (player.gameMode != GameMode.CREATIVE) item.subtract()
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun HangingBreakEvent.onBreakHanging() {
        if (cause == HangingBreakEvent.RemoveCause.ENTITY) return
        if (entity.toGearyOrNull() != null) isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockBreakEvent.onBreakingBarrier() {
        if (block.type != Material.BARRIER || player.gameMode != GameMode.CREATIVE) return
        block.getAssociatedBlockyFrame(10.0)?.removeBlockyFrame(player, this)
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerInteractEntityEvent.onRotatingFrame() {
        if (rightClicked is ItemFrame && rightClicked.toGearyOrNull()?.has<BlockyEntity>() == true) isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerInteractEvent.onSitting() {
        val block = clickedBlock ?: return
        if (action != Action.RIGHT_CLICK_BLOCK || hand != EquipmentSlot.HAND) return
        if (block.type != Material.BARRIER || player.isSneaking) return

        block.sitOnSeat(player)
        if (!player.inventory.itemInMainHand.type.isAir) isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun ProjectileHitEvent.onProjectileHit() {
        (hitBlock != null && hitBlock?.type == Material.BARRIER &&
        hitEntity != null && hitEntity is ItemFrame && hitEntity?.toGearyOrNull()?.has<BlockyEntity>() == true) || return

        //TODO Consider making shooter handle for drops
        if (entity is Explosive) { (hitEntity as ItemFrame).removeBlockyFrame(null,this)
        } else isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockExplodeEvent.onBlockExplode() {
        val blockyBlocks = blockList().filter { it.type == Material.BARRIER && it.getAssociatedBlockyFrame(this.yield.toDouble()) != null }
        val frames = blockyBlocks.map { it.getAssociatedBlockyFrame(this.yield.toDouble()) }.toSet().toList()
        frames.forEach { it?.removeBlockyFrame(null, this) }
    }

    @EventHandler(ignoreCancelled = true)
    fun EntityDamageByEntityEvent.onBreakingFrame() {
        if (entity !is ItemFrame) return
        val gearyEntity = entity.toGearyOrNull() ?: return
        if (!gearyEntity.has<BlockyEntity>() || !gearyEntity.has<BlockyInfo>()) return
        if (gearyEntity.get<BlockyInfo>()?.isUnbreakable == true) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerQuitEvent.onQuit() {
        val seat = player.vehicle as? ArmorStand ?: return
        seat.toGearyOrNull()?.has<BlockySeat>() ?: return || return
        player.leaveVehicle()
    }
}
