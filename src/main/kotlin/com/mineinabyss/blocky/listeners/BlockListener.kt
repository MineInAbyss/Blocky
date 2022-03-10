package com.mineinabyss.blocky.listeners

import com.mineinabyss.idofront.entities.leftClicked
import com.mineinabyss.idofront.entities.rightClicked
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.idofront.messaging.broadcastVal
import org.bukkit.Instrument
import org.bukkit.Material
import org.bukkit.Note
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.NotePlayEvent
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.math.roundToInt


class BlockListener : Listener {

    @EventHandler
    fun NotePlayEvent.cancelBlockyNotes() { isCancelled = true }

    @EventHandler
    fun PlayerInteractEvent.onChangingNote() {
        if (clickedBlock?.type == Material.NOTE_BLOCK && rightClicked) isCancelled = true
    }

    @EventHandler
    fun BlockPlaceEvent.onPlacingBlockyBlock() {
        if (block.type != Material.NOTE_BLOCK) return
        if (!itemInHand.itemMeta.hasCustomModelData()) return
        val blockId = itemInHand.itemMeta.customModelData
        val data = block.blockData as NoteBlock
        val instrumentId = (blockId / 25).toDouble().toInt()
        val noteId = instrumentId * 25

        data.instrument = when (instrumentId) {
            0 -> Instrument.PIANO
            1 -> Instrument.BASS_DRUM
            2 -> Instrument.SNARE_DRUM
            3 -> Instrument.STICKS
            4 -> Instrument.BASS_GUITAR
            5 -> Instrument.FLUTE
            6 -> Instrument.BELL
            7 -> Instrument.GUITAR
            8 -> Instrument.CHIME
            9 -> Instrument.XYLOPHONE
            10 -> Instrument.IRON_XYLOPHONE
            11 -> Instrument.COW_BELL
            12 -> Instrument.DIDGERIDOO
            13 -> Instrument.BIT
            14 -> Instrument.BANJO
            15 -> Instrument.PLING
            else -> Instrument.PIANO
        }

        data.note = Note((blockId - noteId))
        block.blockData = data
    }
}