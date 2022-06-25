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
import org.bukkit.entity.Player

fun GearyEntity.getBlockyNoteBlock(face: BlockFace): BlockData {
    val id = getDirectionalId(face)
    return blockMap.filter { it.key is NoteBlock && it.key.material == Material.NOTE_BLOCK && it.value == id }.keys.first() as NoteBlock
}

fun Block.isBlockyNoteBlock() : Boolean = blockMap.contains(blockData) && type == Material.NOTE_BLOCK

fun Block.updateNoteBlockAbove() {
    val above = getRelative(BlockFace.UP)
    above.state.update(true, true)
    if (above.getRelative(BlockFace.UP).type == Material.NOTE_BLOCK)
        above.updateNoteBlockAbove()
}

fun Block.isVanillaNoteBlock(): Boolean {
    return blockData == Bukkit.createBlockData(Material.NOTE_BLOCK)
}

fun updateBlockyNote(block: Block) {
    val noteBlock = NamespacedKey(blockyPlugin, Material.NOTE_BLOCK.toString().lowercase())
    val pdc = CustomBlockData(block, blockyPlugin)
    val map = pdc.get(noteBlock, DataType.asMap(DataType.BLOCK_DATA, DataType.INTEGER)) ?: return
    val data = map.entries.first().key as NoteBlock
    val i = map.entries.first().value + 1

    data.note = Note((i % 25))

    map.clear()
    map[data] = i
    pdc.set(noteBlock, DataType.asMap(DataType.BLOCK_DATA, DataType.INTEGER), map)
}

fun playBlockyNoteBlock(block: Block, player: Player) {
    val noteBlock = NamespacedKey(blockyPlugin, Material.NOTE_BLOCK.toString().lowercase())
    val map = CustomBlockData(block, blockyPlugin).get(noteBlock, DataType.asMap(DataType.BLOCK_DATA, DataType.INTEGER)) ?: return
    val data = map.entries.first().key as NoteBlock
    val color = (map.entries.first().value / 24f).toDouble()
    val loc = block.getRelative(BlockFace.UP).location.add(0.5, 0.0, 0.5)

    player.location.getNearbyPlayers(50.0).forEach {p ->
        p.playNote(loc, block.modifiesBlockyInstrument(), data.note)
        p.spawnParticle(Particle.NOTE, loc, 0, color, 0.0, 1.0)
    }
}

fun Block.modifiesBlockyInstrument(): Instrument {
    val b = getRelative(BlockFace.DOWN)
    return list.filter { b.type.toString().lowercase().contains(it.first) }
        .toList().firstOrNull()?.second ?: Instrument.PIANO
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
