package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.helpers.CaveVineHelpers
import com.mineinabyss.blocky.helpers.GenericHelpers.isInteractable
import com.mineinabyss.blocky.helpers.gearyInventory
import com.mineinabyss.blocky.helpers.placeBlockyBlock
import com.mineinabyss.blocky.helpers.to
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
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
        if (block.type == Material.CAVE_VINES_PLANT || CaveVineHelpers.isBlockyCaveVine(block)) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockPlaceEvent.onGlowBerryPlace() {
        if (itemInHand.type == Material.GLOW_BERRIES)
            blockPlaced.setBlockData(Material.CAVE_VINES.createBlockData(), false)

        // If the block above is cave vine with age 0, replicate vanilla behaviour
        if (CaveVineHelpers.isBlockyCaveVine(block)) isCancelled = true
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
        if (CaveVineHelpers.isBlockyCaveVine(block) && item.type == Material.BONE_MEAL)
            isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockBreakEvent.onBreakingBlockyCaveVine() {
        if (!CaveVineHelpers.isBlockyCaveVine(block)) return
        isDropItems = false
        CaveVineHelpers.breakCaveVineBlock(block, player)
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockBreakBlockEvent.onWaterCollide() {
        if (!CaveVineHelpers.isBlockyCaveVine(block)) return
        CaveVineHelpers.breakCaveVineBlock(block, null)
        drops.removeIf { it.type == Material.GLOW_BERRIES }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.prePlaceBlockyCaveVine() {
        val (block, item, hand) = (clickedBlock ?: return) to (item ?: return) to (hand ?: return)

        if (action != Action.RIGHT_CLICK_BLOCK || hand != EquipmentSlot.HAND) return
        if (!player.isSneaking && block.isInteractable()) return
        if (item.type == Material.GLOW_BERRIES) return // Handled by [onGlowBerryPlace()]
        if (blockFace == BlockFace.UP && player.world.getBlockData(block.location) is CaveVines) {
            isCancelled = true
            return
        }

        val gearyVine = player.gearyInventory?.get(hand) ?: return
        val blockyVine = gearyVine.get<SetBlock>() ?: return
        if (blockyVine.blockType != SetBlock.BlockType.CAVEVINE) return

        placeBlockyBlock(player, hand, item, block, blockFace, CaveVineHelpers.blockyCaveVine(blockyVine))
    }
}
