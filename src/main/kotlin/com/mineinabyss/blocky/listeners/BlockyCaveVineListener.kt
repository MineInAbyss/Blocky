package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.components.core.BlockyBlock
import com.mineinabyss.blocky.components.core.BlockyBlock.BlockType
import com.mineinabyss.blocky.components.core.BlockyInfo
import com.mineinabyss.blocky.components.features.BlockyLight
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.looty.tracking.toGearyOrNull
import io.papermc.paper.event.block.BlockBreakBlockEvent
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.CaveVines
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockGrowEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class BlockyCaveVineListener : Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockGrowEvent.onCaveVineGrow() {
        if (block.type == Material.CAVE_VINES_PLANT || block.isBlockyCaveVine()) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockPlaceEvent.onGlowBerryPlace() {
        if (itemInHand.type == Material.GLOW_BERRIES)
            blockPlaced.setBlockData(Material.CAVE_VINES.createBlockData(), false)

        // If the block above is cave vine with age 0, replicate vanilla behaviour
        if (block.isBlockyCaveVine()) isCancelled = true
        else if (blockAgainst.type == Material.CAVE_VINES) blockAgainst.setType(Material.CAVE_VINES_PLANT, false)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockBreakEvent.onBreakCaveVinePlant() {
        if (block.type != Material.CAVE_VINES_PLANT) return
        block.setType(Material.AIR, false)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun PlayerInteractEvent.onGrowCaveVineGlowBerries() {
        val block = clickedBlock ?: return
        val item = item ?: return

        if (hand != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK) return
        if (block.isBlockyCaveVine() && item.type == Material.BONE_MEAL)
            isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockBreakEvent.onBreakingBlockyCaveVine() {
        if (!block.isBlockyCaveVine()) return
        isDropItems = false
        breakCaveVineBlock(block, player)
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockBreakBlockEvent.onWaterCollide() {
        if (!block.isBlockyCaveVine()) return
        breakCaveVineBlock(block, null)
        drops.removeIf { it.type == Material.GLOW_BERRIES }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.prePlaceBlockyCaveVine() {
        val clickedBlock = clickedBlock ?: return
        val item = item ?: return

        if (action != Action.RIGHT_CLICK_BLOCK || hand != EquipmentSlot.HAND) return
        if (clickedBlock.type.isInteractable && !clickedBlock.isBlockyCaveVine() && !player.isSneaking) return
        if (item.type == Material.GLOW_BERRIES) return // Handled by [onGlowBerryPlace()]
        if (blockFace == BlockFace.UP && player.world.getBlockData(clickedBlock.location) is CaveVines) {
            isCancelled = true
            return
        }

        val gearyVine = item.toGearyOrNull(player) ?: return
        val blockyVine = gearyVine.get<BlockyBlock>() ?: return
        val lightLevel = gearyVine.get<BlockyLight>()?.lightLevel
        if (blockyVine.blockType != BlockType.CAVEVINE) return
        if (!gearyVine.has<BlockyInfo>()) return

        val placedWire =
            placeBlockyBlock(player, hand!!, item, clickedBlock, blockFace, blockyVine.getBlockyCaveVine()) ?: return
        if (gearyVine.has<BlockyLight>())
            handleLight.createBlockLight(placedWire.location, lightLevel!!)
    }
}
