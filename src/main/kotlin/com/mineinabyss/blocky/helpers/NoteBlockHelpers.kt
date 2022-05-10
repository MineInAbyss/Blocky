package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.blockMap
import com.mineinabyss.blocky.components.BlockyBlock
import com.mineinabyss.blocky.components.BlockyDirectional
import com.mineinabyss.geary.datatypes.GearyEntity
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.NoteBlock

fun GearyEntity.getBlockyNoteBlock(face: BlockFace) : BlockData {
    val directional = get<BlockyDirectional>()
    var id = get<BlockyBlock>()?.blockId

    if (has<BlockyDirectional>()) {
        if (directional?.hasYVariant() == true && (face == BlockFace.UP || face == BlockFace.DOWN)) id = directional.yBlockId
        else if (directional?.hasXVariant() == true && (face == BlockFace.NORTH || face == BlockFace.SOUTH)) id = directional.xBlockId
        else if (directional?.hasZVariant() == true && (face == BlockFace.WEST || face == BlockFace.EAST)) id = directional.zBlockId
    }

    return blockMap.filter { it.key is NoteBlock && it.key.material == Material.NOTE_BLOCK && it.value == id }.keys.first() as NoteBlock
}

fun updateAndCheck(loc: Location) {
    val block = loc.add(0.0, 1.0, 0.0).block
    if (block.type == Material.NOTE_BLOCK) block.state.update(true, true)
    val nextBlock = block.location.add(0.0, 1.0, 0.0)
    if (nextBlock.block.type == Material.NOTE_BLOCK) updateAndCheck(block.location)
}