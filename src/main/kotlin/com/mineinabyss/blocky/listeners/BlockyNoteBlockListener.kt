package com.mineinabyss.blocky.listeners

import com.jeff_media.customblockdata.CustomBlockData
import com.jeff_media.morepersistentdatatypes.DataType
import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.blocky.helpers.isVanillaNoteBlock
import com.mineinabyss.blocky.helpers.playBlockyNoteBlock
import com.mineinabyss.blocky.helpers.updateAndCheck
import com.mineinabyss.blocky.helpers.updateBlockyNote
import com.mineinabyss.idofront.entities.rightClicked
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.NotePlayEvent
import org.bukkit.event.player.PlayerInteractEvent

class BlockyNoteBlockListener : Listener {

    @EventHandler
    fun NotePlayEvent.cancelBlockyNotes() {
        isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun PlayerInteractEvent.onChangingNote() {
        val block = clickedBlock ?: return

        if (block.type == Material.NOTE_BLOCK) {
            if (rightClicked) isCancelled = true
            if (block.isVanillaNoteBlock()) {
                if (rightClicked) updateBlockyNote(block)
                playBlockyNoteBlock(block, player)
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun BlockPhysicsEvent.onBlockPhysics() {
        val aboveBlock = block.getRelative(BlockFace.UP)
        if (aboveBlock.type == Material.NOTE_BLOCK) {
            isCancelled = true
            updateAndCheck(block.location)
        }
        if (block.type == Material.NOTE_BLOCK) {
            isCancelled = true
            block.state.update(true, false)
            if (block.isBlockIndirectlyPowered) {
                val p = block.location.getNearbyPlayers(48.0).firstOrNull() ?: return
                playBlockyNoteBlock(block, p)
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockPlaceEvent.onPlaceNoteBlock() {
        if (blockPlaced.isVanillaNoteBlock()) {
            val map = mutableMapOf<BlockData?, Int?>()
            map[blockPlaced.blockData] = 0
            CustomBlockData(blockPlaced, blockyPlugin).set(
                NamespacedKey(blockyPlugin, Material.NOTE_BLOCK.toString().lowercase()),
                DataType.asMap(DataType.BLOCK_DATA, DataType.INTEGER),
                map
            )
        }
    }
}
