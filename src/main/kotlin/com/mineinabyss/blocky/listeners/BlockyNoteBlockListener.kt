package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.components.BlockType
import com.mineinabyss.blocky.components.BlockyBlock
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.blocky.components.BlockyLight
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.idofront.entities.rightClicked
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.player.PlayerInteractEvent


class BlockyNoteBlockListener : Listener {

    @EventHandler
    fun NotePlayEvent.cancelBlockyNotes() {
        isCancelled = true
    }

    @EventHandler
    fun PlayerInteractEvent.onChangingNote() {
        if (clickedBlock?.type == Material.NOTE_BLOCK && rightClicked) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockPhysicsEvent.onBlockPhysics() {
        val aboveBlock = block.location.add(0.0, 1.0, 0.0).block
        if (aboveBlock.type == Material.NOTE_BLOCK) {
            isCancelled = true
            updateAndCheck(block.location)
        }
        if (block.type == Material.NOTE_BLOCK) {
            isCancelled = true
            block.state.update(true, false)
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.onPrePlacingBlockyBlock() {
        val gearyItem = player.inventory.itemInMainHand.toGearyOrNull(player) ?: return
        val blockyBlock = gearyItem.get<BlockyBlock>() ?: return
        val blockyInfo = gearyItem.get<BlockyInfo>() ?: return
        val blockyLight = gearyItem.get<BlockyLight>()!!.lightLevel
        if (action != Action.RIGHT_CLICK_BLOCK) return

        val against = clickedBlock ?: return
        val placed =
            placeBlockyBlock(player, hand!!, item!!, against, blockFace, blockyBlock.getBlockyNoteBlockDataFromPrefab())
                ?: return
        placed.world.playSound(placed.location, blockyInfo.placeSound, 1.0f,  0.8f)
        if (gearyItem.has<BlockyLight>()) createBlockLight(placed.location, blockyLight)
        isCancelled = true

    }

    @EventHandler
    fun BlockPlaceEvent.onPlacingBlockyBlock() {
        val gearyItem = itemInHand.toGearyOrNull(player) ?: return
        val blockyBlock = gearyItem.get<BlockyBlock>() ?: return
        val blockyInfo = gearyItem.get<BlockyInfo>() ?: return

        if (blockyBlock.blockType == BlockType.CUBE) {
            block.setBlockData(block.getBlockyBlockDataFromItem(blockyBlock.blockId), false)
        } else if (blockyBlock.blockType == BlockType.GROUND && blockAgainst.getFace(blockPlaced) == BlockFace.UP) {
            block.setType(Material.TRIPWIRE, false)
            block.setBlockData(block.getBlockyDecorationDataFromItem(blockyBlock.blockId, blockyBlock.blockType), true)
            blockAgainst.state.update(true, false)
            blockPlaced.state.update(true, false)
            return
        } else if (blockyBlock.blockType == BlockType.WALL && blockAgainst.getFace(blockPlaced) != BlockFace.UP) {
            block.setType(Material.GLOW_LICHEN, false)
            block.setBlockData(block.getBlockyDecorationDataFromItem(blockyBlock.blockId, blockyBlock.blockType), true)
        }
        block.setBlockData(block.blockData, false)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockBreakEvent.onBreakingBlockyBlock() {
        if (block.type != Material.NOTE_BLOCK || isCancelled || !isDropItems) return

        val prefab = block.getPrefabFromBlock() ?: return
        val blockyInfo = prefab.get<BlockyInfo>() ?: return

        if (blockyInfo.isUnbreakable && player.gameMode != GameMode.CREATIVE) isCancelled = true

        block.world.playSound(block.location, blockyInfo.breakSound, 1.0f, 0.8f)
        isDropItems = false
        handleBlockyDrops(block, player)
        removeBlockLight(block.location)
    }
}