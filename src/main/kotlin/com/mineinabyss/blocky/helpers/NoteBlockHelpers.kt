package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.BlockyTypeQuery
import com.mineinabyss.blocky.components.BlockyBlock
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.NoteBlock

fun Block.getBlockyBlockDataFromItem(blockId: Int): BlockData {
    setType(Material.NOTE_BLOCK, false)
    val data = blockData as NoteBlock
    data.instrument = Instrument.getByType((blockId / 25 % 400).toByte()) ?: return blockData
    data.note = Note((blockId % 25))
    data.isPowered = blockId in 0..399
    blockMap.putIfAbsent(data, blockId)
    return data
}

fun Block.getPrefabFromBlock(): GearyEntity? {
    val blockyBlock = BlockyTypeQuery.firstOrNull {
        it.entity.get<BlockyBlock>()?.blockId == blockMap[blockData]
    }?.entity ?: return null

    return blockyBlock
}

fun BlockyBlock.getBlockyNoteBlockDataFromPrefab() : BlockData {
    val blockData = Bukkit.createBlockData(Material.NOTE_BLOCK)
    val data = blockData as NoteBlock
    data.instrument = Instrument.getByType((blockId / 25 % 400).toByte()) ?: return data
    data.note = Note((blockId % 25))
    data.isPowered = blockId in 0..399
    blockMap.putIfAbsent(data, blockId)
    return data
}

fun updateAndCheck(loc: Location) {
    val block = loc.add(0.0, 1.0, 0.0).block
    if (block.type == Material.NOTE_BLOCK) block.state.update(true, true)
    val nextBlock = block.location.add(0.0, 1.0, 0.0)
    if (nextBlock.block.type == Material.NOTE_BLOCK) updateAndCheck(block.location)
}