package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.components.*
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.Material
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class BlockyLeafListener : Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun LeavesDecayEvent.onLeafDecay() {
        if (leafConfig.disableAllLeafDecay) isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockPistonExtendEvent.cancelBlockyPiston() {
        if (blocks.any { it.isBlockyLeaf() }) isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockPistonRetractEvent.cancelBlockyPiston() {
        if (blocks.any { it.isBlockyLeaf() }) isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockBurnEvent.onBurnBlockyLeaf() {
        if (!block.isBlockyLeaf()) return
        if (leafConfig.disableBurnForBlockyLeaves) isCancelled = true
        if (block.getGearyEntityFromBlock()?.has<BlockyBurnable>() == true) isCancelled = false
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerInteractEvent.onPreBlockyLeafPlace() {
        player.eyeLocation.getNearbyEntitiesByType(ItemFrame::class.java, 0.1, 0.1, 0.1).firstOrNull() ?: return
        if (action != Action.RIGHT_CLICK_BLOCK) return
        if (hand != EquipmentSlot.HAND) return

        val gearyItem = item?.toGearyOrNull(player) ?: return
        val blockyBlock = gearyItem.get<BlockyBlock>() ?: return
        val blockyLight = gearyItem.get<BlockyLight>()?.lightLevel
        val against = clickedBlock ?: return
        if ((against.type.isInteractable && against.getPrefabFromBlock()
                ?.toEntity() == null) && !player.isSneaking
        ) return

        if (!gearyItem.has<BlockyInfo>()) return
        if (blockyBlock.blockType != BlockType.LEAF) return

        val placed = placeBlockyBlock(player, hand!!, item!!, against, blockFace, blockyBlock.getBlockyLeaf()) ?: return
        if (gearyItem.has<BlockyLight>()) handleLight.createBlockLight(placed.location, blockyLight!!)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockPlaceEvent.onPlacingBlockyBlock() {
        val gearyItem = itemInHand.toGearyOrNull(player) ?: return
        val blockyBlock = gearyItem.get<BlockyBlock>() ?: return

        if (!gearyItem.has<BlockyInfo>()) return
        if (blockyBlock.blockType != BlockType.LEAF) return

        block.setBlockData(blockyBlock.getBlockyLeaf(), false)
        player.swingMainHand()
    }

    @EventHandler(ignoreCancelled = true)
    fun EntityExplodeEvent.onExplodingBlocky() {
        blockList().forEach { block ->
            val prefab = block.getGearyEntityFromBlock() ?: return@forEach
            if (!block.isBlockyLeaf()) return@forEach
            if (prefab.has<BlockyInfo>()) handleBlockyDrops(block, null)
            block.setType(Material.AIR, false)
        }
    }
}
