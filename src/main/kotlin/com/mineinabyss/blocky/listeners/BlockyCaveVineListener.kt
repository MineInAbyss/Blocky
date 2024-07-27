package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.helpers.CaveVineHelpers
import com.mineinabyss.blocky.helpers.GenericHelpers.isInteractable
import com.mineinabyss.blocky.helpers.decode
import com.mineinabyss.blocky.helpers.gearyInventory
import com.mineinabyss.blocky.helpers.placeBlockyBlock
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.idofront.util.to
import io.papermc.paper.event.block.BlockBreakBlockEvent
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.CaveVines
import org.bukkit.block.data.type.CaveVinesPlant
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockGrowEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class BlockyCaveVineListener : Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockGrowEvent.onCaveVineGrow() {
        if (block.blockData is CaveVinesPlant) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockPlaceEvent.onGlowBerryPlace() {
        if (itemInHand.type != Material.GLOW_BERRIES || CaveVineHelpers.isBlockyCaveVine(itemInHand)) return
        if (blockPlaced.type != Material.CAVE_VINES || CaveVineHelpers.isBlockyCaveVine(blockAgainst)) return

        blockPlaced.setBlockData(CaveVineHelpers.defaultBlockData, false)
        if (blockAgainst.type == Material.CAVE_VINES) blockAgainst.setType(Material.CAVE_VINES_PLANT, false)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockBreakEvent.onBreakCaveVinePlant() {
        //if (block.type != Material.CAVE_VINES_PLANT) return
        //block.setType(Material.AIR, false)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun PlayerInteractEvent.onGrowCaveVineGlowBerries() {
        val (block, item) = (clickedBlock ?: return) to (item ?: return)
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
        val (block, item, hand) = (clickedBlock ?: return) to (item?.takeIf { it.type != Material.GLOW_BERRIES } ?: return) to (hand ?: return)
        if (action != Action.RIGHT_CLICK_BLOCK || hand != EquipmentSlot.HAND) return
        if (!player.isSneaking && block.isInteractable()) return

        val blockyVine = player.gearyInventory?.get(hand)?.get<SetBlock>() ?: return
        if (blockyVine.blockType != SetBlock.BlockType.CAVEVINE) return
        if (blockFace == BlockFace.UP && block.blockData is CaveVinesPlant) {
            isCancelled = true
            return
        }

        placeBlockyBlock(player, hand, item, block, blockFace, CaveVineHelpers.blockyCaveVine(blockyVine))
    }
}
