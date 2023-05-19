package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.api.BlockyBlocks.gearyEntity
import com.mineinabyss.blocky.blockyConfig
import com.mineinabyss.blocky.components.core.BlockyBlock
import com.mineinabyss.blocky.components.core.BlockyBlock.BlockType
import com.mineinabyss.blocky.components.core.BlockyInfo
import com.mineinabyss.blocky.components.features.BlockyBurnable
import com.mineinabyss.blocky.components.features.BlockyLight
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.blocky.itemProvider
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.nms.aliases.toNMS
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
        if (blockyConfig.leafBlocks.disableAllLeafDecay) isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockPistonExtendEvent.cancelBlockyPiston() {
        if (blocks.any { it.isBlockyLeaf() }) isCancelled = true
    }

    //TODO Try and make it check components now, didnt work before but
    @EventHandler(ignoreCancelled = true)
    fun BlockPistonRetractEvent.cancelBlockyPiston() {
        if (blocks.any { it.isBlockyLeaf() }) isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockBurnEvent.onBurnBlockyLeaf() {
        if (!block.isBlockyLeaf()) return
        if (blockyConfig.leafBlocks.disableBurnForBlockyLeaves) isCancelled = true
        if (block.gearyEntity?.has<BlockyBurnable>() == true) isCancelled = false
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerInteractEvent.onPreBlockyLeafPlace() {
        player.eyeLocation.getNearbyEntitiesByType(ItemFrame::class.java, 0.1, 0.1, 0.1).firstOrNull() ?: return
        if (action != Action.RIGHT_CLICK_BLOCK) return
        if (hand != EquipmentSlot.HAND) return

        val gearyItem = item?.toNMS()?.let { itemProvider.deserializeItemStackToEntity(it, player.toGeary()) } ?: return
        val blockyBlock = gearyItem.get<BlockyBlock>() ?: return
        val blockyLight = gearyItem.get<BlockyLight>()?.lightLevel
        val against = clickedBlock ?: return
        if ((against.type.isInteractable && against.prefabKey
                ?.toEntity() == null) && !player.isSneaking
        ) return

        if (!gearyItem.has<BlockyInfo>()) return
        if (blockyBlock.blockType != BlockType.LEAF) return

        val placed = placeBlockyBlock(player, hand!!, item!!, against, blockFace, blockyBlock.getBlockyLeaf()) ?: return
        if (gearyItem.has<BlockyLight>()) handleLight.createBlockLight(placed.location, blockyLight!!)
    }

    //TODO Isnt this all done inside placeBlockyBlock?
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockPlaceEvent.onPlacingBlockyBlock() {
        val gearyItem = itemInHand.toNMS()?.let { itemProvider.deserializeItemStackToEntity(it, player.toGeary()) } ?: return
        val blockyBlock = gearyItem.get<BlockyBlock>() ?: return

        if (!gearyItem.has<BlockyInfo>()) return
        if (blockyBlock.blockType != BlockType.LEAF) return

        block.setBlockData(blockyBlock.getBlockyLeaf(), false)
        player.swingMainHand()
    }

    @EventHandler(ignoreCancelled = true)
    fun EntityExplodeEvent.onExplodingBlocky() {
        blockList().forEach { block ->
            val prefab = block.gearyEntity ?: return@forEach
            if (!block.isBlockyLeaf()) return@forEach
            if (prefab.has<BlockyInfo>()) handleBlockyDrops(block, null)
            block.setType(Material.AIR, false)
        }
    }
}
