package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.helpers.playBlockyNoteBlock
import com.mineinabyss.blocky.helpers.updateAndCheck
import com.mineinabyss.blocky.helpers.updateBlockyNote
import com.mineinabyss.idofront.entities.leftClicked
import com.mineinabyss.idofront.entities.rightClicked
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.block.NotePlayEvent
import org.bukkit.event.player.PlayerInteractEvent

class BlockyNoteBlockListener : Listener {

    @EventHandler
    fun NotePlayEvent.cancelBlockyNotes() {
        isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.onChangingNote() {
        val block = clickedBlock ?: return

        if (block.type == Material.NOTE_BLOCK) {
            if (rightClicked) {
                isCancelled = true
                updateBlockyNote(block)
                playBlockyNoteBlock(block, player)

            } else if (leftClicked && player.gameMode != GameMode.CREATIVE) {
                isCancelled = true
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
            //TODO Note is played repeatedly if the redstpne is powered say below
            if (block.isBlockIndirectlyPowered) {
                val p = block.location.getNearbyPlayers(48.0).firstOrNull() ?: return
                playBlockyNoteBlock(block, p)
            }
        }
    }
}