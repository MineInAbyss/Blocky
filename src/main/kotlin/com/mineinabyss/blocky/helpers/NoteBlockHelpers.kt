package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.components.BlockyBlock
import com.mineinabyss.blocky.components.BlockyDirectional
import com.mineinabyss.geary.datatypes.GearyEntity
import org.bukkit.*
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.NoteBlock

fun GearyEntity.getBlockyNoteBlockDataFromPrefab(face: BlockFace) : BlockData {
    val data = Bukkit.createBlockData(Material.NOTE_BLOCK) as NoteBlock
    val blockyBlock = get<BlockyBlock>() ?: return data
    val directional = get<BlockyDirectional>()
    var id = blockyBlock.blockId

    if (has<BlockyDirectional>()) {
        if (directional?.hasYVariant() == true && (face == BlockFace.UP || face == BlockFace.DOWN)) id = directional.yBlockId
        else if (directional?.hasXVariant() == true && (face == BlockFace.NORTH || face == BlockFace.SOUTH)) id = directional.xBlockId
        else if (directional?.hasZVariant() == true && (face == BlockFace.WEST || face == BlockFace.EAST)) id = directional.zBlockId
    }

    data.instrument = Instrument.getByType((id / 25 % 400).toByte()) ?: return data
    data.note = Note((id % 25))
    data.isPowered = id !in 0..399
    blockMap.putIfAbsent(data, id)
    return data
}

fun updateAndCheck(loc: Location) {
    val block = loc.add(0.0, 1.0, 0.0).block
    if (block.type == Material.NOTE_BLOCK) block.state.update(true, true)
    val nextBlock = block.location.add(0.0, 1.0, 0.0)
    if (nextBlock.block.type == Material.NOTE_BLOCK) updateAndCheck(block.location)
}