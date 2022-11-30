package com.mineinabyss.blocky.helpers

import com.github.shynixn.mccoroutine.bukkit.launch
import com.jeff_media.morepersistentdatatypes.DataType
import com.mineinabyss.blocky.blockMap
import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.geary.datatypes.GearyEntity
import kotlinx.coroutines.yield
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.entity.Player
import org.bukkit.event.block.NotePlayEvent

val NOTE_KEY = NamespacedKey(blockyPlugin, "note")
val VANILLA_NOTEBLOCK_KEY = NamespacedKey(blockyPlugin, "vanilla_note_block")

fun GearyEntity.getBlockyNoteBlock(face: BlockFace = BlockFace.NORTH, player: Player? = null): BlockData {
    return blockMap.filter { it.key is NoteBlock && it.value == this.getDirectionalId(face, player) }.keys.firstOrNull() ?: return Bukkit.createBlockData(Material.NOTE_BLOCK) as NoteBlock
}

fun Block.updateNoteBlockAbove() {
    val above = getRelative(BlockFace.UP)
    val data = above.blockData.clone()
    above.state.update(true, false)
    blockyPlugin.launch {
        yield()
        above.setBlockData(data, false)
    }

    if (above.getRelative(BlockFace.UP).type == Material.NOTE_BLOCK)
        above.updateNoteBlockAbove()
}

// If the blockmap doesn't contain data, it means it's a vanilla note block
val Block.isVanillaNoteBlock get() = blockData is NoteBlock && blockData !in blockMap
val BlockData.isVanillaNoteBlock get() = this is NoteBlock && this !in blockMap

val Block.isBlockyNoteBlock get() = blockData in blockMap && blockData is NoteBlock
val BlockData.isBlockyNoteBlock get() = this in blockMap && this is NoteBlock

// Updates the note stored in the pdc by 1
fun Block.updateBlockyNote(): Note {
    val note = this.persistentDataContainer.getOrDefault(NOTE_KEY, DataType.INTEGER, 0) + 1
    this.persistentDataContainer.set(NOTE_KEY, DataType.INTEGER, note)
    return Note(note % 25)
}

fun Block.getBlockyNote(): Note {
    val note = this.persistentDataContainer.get(NOTE_KEY, DataType.INTEGER) ?: 0
    return Note(note % 25)
}

fun Block.playBlockyNoteBlock() {
    NotePlayEvent(this, this.getBlockyInstrument(), this.getBlockyNote()).callEvent()
}

fun Block.getBlockyInstrument(): Instrument {
    return instrumentList.firstOrNull {
        it.type in getRelative(BlockFace.DOWN).type.toString().lowercase()
    }?.instrument ?: Instrument.PIANO
}

private class InstrumentMap(val type: String, val instrument: Instrument)
private val instrumentList = listOf(
    InstrumentMap("gold_block", Instrument.BELL),
    InstrumentMap("clay", Instrument.FLUTE),
    InstrumentMap("packed_ice", Instrument.CHIME),
    InstrumentMap("wool", Instrument.GUITAR),
    InstrumentMap("bone_block", Instrument.XYLOPHONE),
    InstrumentMap("iron_block", Instrument.IRON_XYLOPHONE),
    InstrumentMap("soul_sand", Instrument.COW_BELL),
    InstrumentMap("pumpkin", Instrument.DIDGERIDOO),
    InstrumentMap("emerald_block", Instrument.BIT),
    InstrumentMap("hay_bale", Instrument.BANJO),
    InstrumentMap("glowstone", Instrument.PLING),
    InstrumentMap("gold_block", Instrument.BELL),
    InstrumentMap("stone", Instrument.BASS_DRUM),
    InstrumentMap("netherrack", Instrument.BASS_DRUM),
    InstrumentMap("bedrock", Instrument.BASS_DRUM),
    InstrumentMap("observer", Instrument.BASS_DRUM),
    InstrumentMap("coral", Instrument.BASS_DRUM),
    InstrumentMap("obsidian", Instrument.BASS_DRUM),
    InstrumentMap("anchor", Instrument.BASS_DRUM),
    InstrumentMap("quartz", Instrument.BASS_DRUM),
    InstrumentMap("wood", Instrument.BASS_GUITAR),
    InstrumentMap("sand", Instrument.SNARE_DRUM),
    InstrumentMap("gravel", Instrument.SNARE_DRUM),
    InstrumentMap("concrete_powder", Instrument.SNARE_DRUM),
    InstrumentMap("soul_soil", Instrument.SNARE_DRUM),
    InstrumentMap("glass", Instrument.STICKS),
    InstrumentMap("sea_lantern", Instrument.STICKS),
    InstrumentMap("beacon", Instrument.STICKS)
)
