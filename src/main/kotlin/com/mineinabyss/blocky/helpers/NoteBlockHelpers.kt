package com.mineinabyss.blocky.helpers

import com.jeff_media.customblockdata.CustomBlockData
import com.jeff_media.morepersistentdatatypes.DataType
import com.mineinabyss.blocky.blockMap
import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.geary.datatypes.GearyEntity
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.event.block.NotePlayEvent

val NOTE_KEY = NamespacedKey(blockyPlugin, "note")
val VANILLA_NOTEBLOCK_KEY = NamespacedKey(blockyPlugin, "vanilla_note_block")

fun GearyEntity.getBlockyNoteBlock(face: BlockFace): BlockData {
    return blockMap.filter { it.key is NoteBlock && it.key.material == Material.NOTE_BLOCK && it.value == this.getDirectionalId(face) }.keys.firstOrNull() ?: return Bukkit.createBlockData(Material.NOTE_BLOCK) as NoteBlock
}

fun Block.updateNoteBlockAbove() {
    val above = getRelative(BlockFace.UP)
    above.state.update(true, true)
    if (above.getRelative(BlockFace.UP).type == Material.NOTE_BLOCK)
        above.updateNoteBlockAbove()
}

// If the blockmap doesn't contain data, it means it's a vanilla note block
val Block.isVanillaNoteBlock get() = blockData is NoteBlock && blockData !in blockMap

val Block.isBlockyNoteBlock get() = blockData in blockMap && blockData is NoteBlock

// Updates the note stored in the pdc by 1
fun Block.updateBlockyNote(): Note {
    val pdc = CustomBlockData(this, blockyPlugin)
    val note = pdc.getOrDefault(NOTE_KEY, DataType.INTEGER, 0) + 1
    pdc.set(NOTE_KEY, DataType.INTEGER, note)
    return Note(note % 25)
}

fun Block.getBlockyNote(): Note {
    val pdc = CustomBlockData(this, blockyPlugin)
    val note = pdc.get(NOTE_KEY, DataType.INTEGER) ?: 0
    return Note(note % 25)
}

//TODO This only needs to store the note, the instrument is determined based on block beneath
fun Block.playBlockyNoteBlock() {
    NotePlayEvent(this, this.getBlockyInstrument(), this.getBlockyNote()).callEvent()
}

fun Block.getBlockyInstrument(): Instrument {
    return list.firstOrNull {
        it.first in getRelative(BlockFace.DOWN).type.toString().lowercase()
    }?.second ?: Instrument.PIANO
}

val list = listOf(
    Pair("gold_block", Instrument.BELL),
    Pair("clay", Instrument.FLUTE),
    Pair("packed_ice", Instrument.CHIME),
    Pair("wool", Instrument.GUITAR),
    Pair("bone_block", Instrument.XYLOPHONE),
    Pair("iron_block", Instrument.IRON_XYLOPHONE),
    Pair("soul_sand", Instrument.COW_BELL),
    Pair("pumpkin", Instrument.DIDGERIDOO),
    Pair("emerald_block", Instrument.BIT),
    Pair("hay_bale", Instrument.BANJO),
    Pair("glowstone", Instrument.PLING),
    Pair("gold_block", Instrument.BELL),
    Pair("stone", Instrument.BASS_DRUM),
    Pair("netherrack", Instrument.BASS_DRUM),
    Pair("bedrock", Instrument.BASS_DRUM),
    Pair("observer", Instrument.BASS_DRUM),
    Pair("coral", Instrument.BASS_DRUM),
    Pair("obsidian", Instrument.BASS_DRUM),
    Pair("anchor", Instrument.BASS_DRUM),
    Pair("quartz", Instrument.BASS_DRUM),
    Pair("wood", Instrument.BASS_GUITAR),
    Pair("sand", Instrument.SNARE_DRUM),
    Pair("gravel", Instrument.SNARE_DRUM),
    Pair("concrete_powder", Instrument.SNARE_DRUM),
    Pair("soul_soil", Instrument.SNARE_DRUM),
    Pair("glass", Instrument.STICKS),
    Pair("sea_lantern", Instrument.STICKS),
    Pair("beacon", Instrument.STICKS)
)
