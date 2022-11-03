package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.core.BlockyFurniture.FurnitureType
import com.mineinabyss.blocky.components.core.BlockyInfo
import com.mineinabyss.blocky.components.features.BlockySeat
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.idofront.events.call
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

class BlockyFurnitureListener : Listener {

    @EventHandler
    fun HangingPlaceEvent.onPlacingItemFrame() {
        val player = player ?: return
        val item = itemStack?.toGearyOrNull(player) ?: return
        if (item.get<BlockyFurniture>()?.furnitureType == FurnitureType.ITEM_FRAME) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.prePlacingFurniture() {
        val item = item ?: return
        val hand = hand ?: return
        val gearyItem = item.toGearyOrNull(player) ?: return
        val furniture = gearyItem.get<BlockyFurniture>() ?: return
        val against = clickedBlock ?: return
        val targetBlock = getTargetBlock(against, blockFace) ?: return
        val targetData = targetBlock.blockData
        if (action != Action.RIGHT_CLICK_BLOCK || hand != EquipmentSlot.HAND) return

        targetBlock.setType(Material.AIR, false)
        val blockyPlace = BlockPlaceEvent(targetBlock, targetBlock.state, against, item, player, true, hand)
        val rotation = getRotation(player.eyeLocation.yaw, furniture.collisionHitbox.isNotEmpty())
        val yaw = getYaw(rotation)

        if (!furniture.hasEnoughSpace(targetBlock.location, yaw)) {
            blockyPlace.isCancelled = true
            player.error("There is not enough space to place this block here.")
            return
        }

        blockyPlace.call()
        if (!blockyPlace.canBuild() || blockyPlace.isCancelled) {
            targetBlock.setBlockData(targetData, false)
            return
        }

        gearyItem.placeBlockyFurniture(rotation, yaw, targetBlock.location, player)
        player.swingMainHand()
        if (player.gameMode != GameMode.CREATIVE) item.subtract()
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun HangingBreakEvent.onBreakHanging() {
        if (cause == HangingBreakEvent.RemoveCause.ENTITY) return
        if (entity.toGearyOrNull() != null) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun BlockBreakEvent.onBreakingBarrier() {
        if (block.type != Material.BARRIER || player.gameMode != GameMode.CREATIVE) return
        block.getBlockyFurniture()?.removeBlockyFurniture(player)
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerInteractEntityEvent.onRotatingFrame() {
        if (rightClicked is ItemFrame && rightClicked.toGearyOrNull()?.has<BlockyFurniture>() == true) isCancelled = true
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
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
        hitEntity != null && hitEntity is ItemFrame && hitEntity?.toGearyOrNull()?.has<BlockyFurniture>() == true) || return

        //TODO Consider making shooter handle for drops
        if (entity is Explosive) { (hitEntity as ItemFrame).removeBlockyFurniture(null) }
        else isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockExplodeEvent.onBlockExplode() {
        val blockyBlocks = blockList().filter { it.type == Material.BARRIER && it.getBlockyFurniture() != null }
        blockyBlocks.map { it.getBlockyFurniture() }.toSet()
            .forEach { it?.removeBlockyFurniture(null) }
    }

    @EventHandler(ignoreCancelled = true)
    fun EntityDamageByEntityEvent.onBreakingFrame() {
        if (entity !is ItemFrame) return
        val gearyEntity = entity.toGearyOrNull() ?: return
        if (!gearyEntity.has<BlockyFurniture>() || !gearyEntity.has<BlockyInfo>()) return
        if (gearyEntity.get<BlockyInfo>()?.isUnbreakable == true) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerQuitEvent.onQuit() {
        val seat = player.vehicle as? ArmorStand ?: return
        seat.toGearyOrNull()?.has<BlockySeat>() ?: return || return
        player.leaveVehicle()
    }
}
