package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.components.BlockType
import com.mineinabyss.blocky.components.BlockyBlock
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.blocky.components.BlockyLight
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.looty.tracking.toGearyOrNull
import io.papermc.paper.event.block.BlockBreakBlockEvent
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class BlockyChorusPlantListener : Listener {

    @EventHandler
    fun BlockGrowEvent.cancelChorusGrow() {
        if (block.type == Material.CHORUS_PLANT || block.type == Material.CHORUS_FLOWER)
            isCancelled = true
    }

    @EventHandler
    fun BlockSpreadEvent.cancelChorusGrow() {
        if (source.type == Material.CHORUS_PLANT || source.type == Material.CHORUS_FLOWER)
            isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockPistonExtendEvent.cancelBlockyPiston() {
        isCancelled = blocks.any { it.isBlockyTransparent() }
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockPistonRetractEvent.cancelBlockyPiston() {
        isCancelled = blocks.any { it.isBlockyTransparent() }
    }

    @EventHandler
    fun BlockPhysicsEvent.onChorusPhysics() {
        if (block.type == Material.CHORUS_FLOWER || block.type == Material.CHORUS_PLANT) {
            if (sourceBlock.isLiquid) BlockBreakBlockEvent(block, sourceBlock, emptyList()).callEvent()
            isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun PlayerInteractEvent.onPrePlacingBlockyTransparent() {
        if (action != Action.RIGHT_CLICK_BLOCK) return
        if (hand != EquipmentSlot.HAND) return

        val gearyItem = item?.toGearyOrNull(player) ?: return
        val blockyBlock = gearyItem.get<BlockyBlock>() ?: return
        val blockyLight = gearyItem.get<BlockyLight>()?.lightLevel
        val against = clickedBlock ?: return

        if ((against.type.isInteractable && against.getGearyEntityFromBlock() == null) && !player.isSneaking) return
        if (!gearyItem.has<BlockyInfo>()) return
        if (blockyBlock.blockType != BlockType.TRANSPARENT) return

        val placed = placeBlockyBlock(player, hand!!, item!!, against, blockFace, gearyItem.getBlockyTransparent(blockFace)) ?: return
        if (gearyItem.has<BlockyLight>())
            handleLight.createBlockLight(placed.location, blockyLight!!)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockPlaceEvent.onPlacingBlockyBlock() {
        val gearyItem = itemInHand.toGearyOrNull(player) ?: return
        val blockyBlock = gearyItem.get<BlockyBlock>() ?: return
        val blockFace = blockAgainst.getFace(blockPlaced) ?: BlockFace.UP

        if (!gearyItem.has<BlockyInfo>()) return
        if (blockyBlock.blockType != BlockType.TRANSPARENT) return

        block.setBlockData(gearyItem.getBlockyTransparent(blockFace), false)
        player.swingMainHand()
    }

    @EventHandler(ignoreCancelled = true)
    fun EntityExplodeEvent.onExplodingBlocky() {
        blockList().forEach { block ->
            val prefab = block.getGearyEntityFromBlock() ?: return@forEach
            if (!block.isBlockyTransparent()) return@forEach
            if (prefab.has<BlockyInfo>()) handleBlockyDrops(block, null)
            block.setType(Material.AIR, false)
        }
    }
}
