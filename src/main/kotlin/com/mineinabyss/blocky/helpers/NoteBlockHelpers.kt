package com.mineinabyss.blocky.helpers

import com.jeff_media.customblockdata.CustomBlockData
import com.jeff_media.morepersistentdatatypes.DataType
import com.mineinabyss.blocky.blockMap
import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.blocky.components.BlockyBlock
import com.mineinabyss.blocky.components.BlockyDirectional
import com.mineinabyss.blocky.components.VanillaNoteBlock
import com.mineinabyss.geary.datatypes.GearyEntity
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.entity.Player

fun GearyEntity.getBlockyNoteBlock(face: BlockFace): BlockData {
    val directional = get<BlockyDirectional>()
    var id = get<BlockyBlock>()?.blockId

    if (has<BlockyDirectional>()) {
        if (directional?.hasYVariant() == true && (face == BlockFace.UP || face == BlockFace.DOWN)) id =
            directional.yBlockId
        else if (directional?.hasXVariant() == true && (face == BlockFace.NORTH || face == BlockFace.SOUTH)) id =
            directional.xBlockId
        else if (directional?.hasZVariant() == true && (face == BlockFace.WEST || face == BlockFace.EAST)) id =
            directional.zBlockId
    }

    return blockMap.filter { it.key is NoteBlock && it.key.material == Material.NOTE_BLOCK && it.value == id }.keys.first() as NoteBlock
}

fun updateAndCheck(loc: Location) {
    val block = loc.block.getRelative(BlockFace.UP)
    if (block.type == Material.NOTE_BLOCK) block.state.update(true, true)
    val nextBlock = block.getRelative(BlockFace.UP)
    if (nextBlock.type == Material.NOTE_BLOCK) updateAndCheck(block.location)
}

fun updateBlockyNote(block: Block) {
    val noteBlock = block.getPrefabFromBlock()?.toEntity()?.get<VanillaNoteBlock>()?.key ?: return
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
    val noteBlock = block.getPrefabFromBlock()?.toEntity()?.get<VanillaNoteBlock>()?.key ?: return
    val map = CustomBlockData(block, blockyPlugin).get(noteBlock, DataType.asMap(DataType.BLOCK_DATA, DataType.INTEGER))
        ?: return
    val data = map.entries.first().key as NoteBlock
    val color = (map.entries.first().value / 24f).toDouble()
    val loc = block.getRelative(BlockFace.UP).location.add(0.5, 0.0, 0.5)

    player.playNote(loc, block.modifiesBlockyInstrument(), data.note)
    player.spawnParticle(Particle.NOTE, loc, 0, color, 0.0, 1.0)
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
