package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.components.core.VanillaNoteBlock
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.gearyBlocks
import org.bukkit.Instrument
import org.bukkit.Material
import org.bukkit.Note
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.entity.Player
import org.bukkit.event.block.NotePlayEvent

fun GearyEntity.getBlockyNoteBlock(face: BlockFace = BlockFace.NORTH, player: Player? = null): BlockData {
    val directional = GenericHelpers.getDirectionalId(this, face, player)
    return gearyBlocks.block2Prefab.blockMap[SetBlock.BlockType.NOTEBLOCK]!![directional]
}

fun Block.updateNoteBlockAbove() {
    val blockAbove = getRelative(BlockFace.UP)
    if (blockAbove.type == Material.NOTE_BLOCK)
        blockAbove.state.update(true, true)
    if (getRelative(BlockFace.UP, 2).type == Material.NOTE_BLOCK)
        blockAbove.updateNoteBlockAbove()
}

// If the blockmap doesn't contain data, it means it's a vanilla note block
val Block.isVanillaNoteBlock get() = blockData is NoteBlock && blockData !in gearyBlocks.block2Prefab
val BlockData.isVanillaNoteBlock get() = this is NoteBlock && this !in gearyBlocks.block2Prefab

val Block.isBlockyNoteBlock get() = blockData is NoteBlock && blockData in gearyBlocks.block2Prefab
val BlockData.isBlockyNoteBlock get() = this is NoteBlock && this in gearyBlocks.block2Prefab

// Updates the note stored in the pdc by 1
fun Block.updateBlockyNote(): Note {
    val note = (this.persistentDataContainer.decode<VanillaNoteBlock>()?.note ?: 0) + 1
    this.persistentDataContainer.encode(VanillaNoteBlock(note))
    return Note(note % 25)
}

fun Block.getBlockyNote(): Note {
    val note = this.persistentDataContainer.decode<VanillaNoteBlock>()?.note ?: 0
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
