package com.mineinabyss.blocky.listeners

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.mineinabyss.blocky.helpers.updateAndCheck
import com.mineinabyss.idofront.entities.rightClicked
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.block.NotePlayEvent
import org.bukkit.event.player.PlayerInteractEvent

val protocolManager: ProtocolManager = ProtocolLibrary.getProtocolManager()

class BlockyNoteBlockListener : Listener {

    @EventHandler
    fun NotePlayEvent.cancelBlockyNotes() { isCancelled = true }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
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
}