package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.components.*
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class BlockyGenericListener : Listener {

    @EventHandler
    fun BlockPistonExtendEvent.cancelBlockyPiston() {
        if (blocks.stream().anyMatch
            {
                it.type == Material.NOTE_BLOCK ||
                        it.type == Material.CHORUS_PLANT ||
                        it.type == Material.CHORUS_FLOWER
            }
        ) isCancelled = true
    }

    @EventHandler
    fun BlockPistonRetractEvent.cancelBlockyPiston() {
        if (blocks.stream().anyMatch
            {
                it.type == Material.NOTE_BLOCK ||
                        it.type == Material.CHORUS_PLANT ||
                        it.type == Material.CHORUS_FLOWER
            }
        ) isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerInteractEvent.onPrePlacingBlockyBlock() {
        if (action != Action.RIGHT_CLICK_BLOCK) return
        if (hand != EquipmentSlot.HAND) return

        val gearyItem = player.inventory.itemInMainHand.toGearyOrNull(player) ?: return
        val blockyBlock = gearyItem.get<BlockyBlock>() ?: return
        val blockyLight = gearyItem.get<BlockyLight>()?.lightLevel
        val blockySound = gearyItem.get<BlockySound>()
        val against = clickedBlock ?: return
        var newData = clickedBlock!!.blockData

        gearyItem.get<BlockyInfo>() ?: return
        if (blockyBlock.blockType == BlockType.GROUND) return

        if (blockyBlock.blockType == BlockType.TRANSPARENT) {
            newData = gearyItem.getBlockyTransparent(blockFace)
        } else if (blockyBlock.blockType == BlockType.CUBE) {
            newData = gearyItem.getBlockyNoteBlock(blockFace)
        }

        val placed = placeBlockyBlock(player, hand!!, item!!, against, blockFace, newData) ?: return

        if (gearyItem.has<BlockySound>()) placed.world.playSound(placed.location, blockySound!!.placeSound, 1.0f, 0.8f)
        if (gearyItem.has<BlockyLight>()) createBlockLight(placed.location, blockyLight!!)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockPlaceEvent.onPlacingBlockyBlock() {
        val gearyItem = itemInHand.toGearyOrNull(player) ?: return
        val blockyBlock = gearyItem.get<BlockyBlock>() ?: return
        val blockFace = blockAgainst.getFace(blockPlaced) ?: BlockFace.UP

        if (blockyBlock.blockType == BlockType.GROUND) return
        gearyItem.get<BlockyInfo>() ?: return
        if (blockyBlock.blockType == BlockType.TRANSPARENT) {
            block.setBlockData(gearyItem.getBlockyTransparent(blockFace), false)
        } else if (blockyBlock.blockType == BlockType.CUBE) {
            block.setBlockData(gearyItem.getBlockyNoteBlock(blockFace), false)
        }
        player.swingMainHand()
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockBreakEvent.onBreakingBlockyBlock() {
        if ((block.type != Material.CHORUS_PLANT && block.type != Material.NOTE_BLOCK) || isCancelled || !isDropItems) return

        val prefab = block.getPrefabFromBlock()?.toEntity() ?: return
        val blockyInfo = prefab.get<BlockyInfo>() ?: return
        val blockySound = prefab.get<BlockySound>()

        if (blockyInfo.isUnbreakable && player.gameMode != GameMode.CREATIVE) isCancelled = true

        isDropItems = false
        if (prefab.has<BlockySound>()) block.world.playSound(block.location, blockySound!!.breakSound, 1.0f, 0.8f)
        if (prefab.has<BlockyLight>()) removeBlockLight(block.location)
        if (prefab.has<BlockDrops>()) handleBlockyDrops(block, player)
    }
}