package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.api.BlockyFurnitures.isBlockyFurniture
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.core.BlockyFurniture.FurnitureType
import com.mineinabyss.blocky.components.core.BlockyInfo
import com.mineinabyss.blocky.components.features.BlockySeat
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.looty.tracking.toGearyOrNull
import io.th0rgal.protectionlib.ProtectionLib
import org.bukkit.GameMode
import org.bukkit.Material
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
import org.bukkit.event.hanging.HangingBreakEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot

class BlockyFurnitureListener : Listener {

    @EventHandler
    fun HangingPlaceEvent.onPlacingItemFrame() {
        val item = player?.let { itemStack?.toGearyOrNull(it) } ?: return
        item.get<BlockyFurniture>()?.furnitureType?.let {
            if (it == FurnitureType.ITEM_FRAME || it == FurnitureType.GLOW_ITEM_FRAME)
                isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.prePlacingFurniture() {
        val targetBlock = clickedBlock?.let { getTargetBlock(it, blockFace) } ?: return
        if (action != Action.RIGHT_CLICK_BLOCK || hand != EquipmentSlot.HAND) return

        item?.toGearyOrNull(player)?.placeBlockyFurniture(player, targetBlock.location, blockFace, item!!)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun HangingBreakEvent.onBreakHanging() {
        if (cause == HangingBreakEvent.RemoveCause.ENTITY) return
        if (entity.toGearyOrNull() != null) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun BlockBreakEvent.onBreakingBarrier() {
        if (block.type != Material.BARRIER || player.gameMode != GameMode.CREATIVE) return
        block.blockyFurniture?.removeBlockyFurniture(player)
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerInteractEntityEvent.onRotatingFrame() {
        if (rightClicked is ItemFrame && rightClicked.toGearyOrNull()?.has<BlockyFurniture>() == true)
            isCancelled = true
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun PlayerInteractEvent.onSitting() {
        val block = clickedBlock ?: return
        if (action != Action.RIGHT_CLICK_BLOCK || hand != EquipmentSlot.HAND) return
        if (block.type != Material.BARRIER || player.isSneaking) return

        player.sitOnBlockySeat(block)
        if (!player.inventory.itemInMainHand.type.isAir) isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun ProjectileHitEvent.onProjectileHit() {
        (hitBlock?.type != Material.BARRIER || hitEntity?.isBlockyFurniture != true) || return

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


        //TODO Consider making shooter handle for drops
        if (entity is Explosive) {
            (hitEntity as ItemFrame).removeBlockyFurniture(null)
        } else isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockExplodeEvent.onBlockExplode() {
        blockList().filter { it.type == Material.BARRIER && it.blockyFurniture != null }
            .map { it.blockyFurniture }.toSet()
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
}
