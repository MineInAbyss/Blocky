package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.BlockyTypeQuery
import com.mineinabyss.blocky.components.BlockType
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.blocky.components.BlockyType
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import org.bukkit.Instrument
import org.bukkit.Material
import org.bukkit.Note
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.GlowLichen
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.block.data.type.Tripwire

val blockMap: MutableMap<BlockData, Int> = mutableMapOf()


fun Block.getBlockyBlockDataFromItem(blockId: Int): BlockData {
    setType(Material.NOTE_BLOCK, false)
    val data = blockData as NoteBlock
    val instrumentId = blockId / 25
    val noteId = instrumentId * 25

    data.instrument = when (instrumentId) {
        0 -> Instrument.BASS_DRUM
        1 -> Instrument.PIANO
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
    blockMap.putIfAbsent(data, blockId)
    return data
}

/**
 * Calculates the correct BlockState-data for the custom-block tied to this item.
 *
 * [BlockType.GROUND] -> Allows for 64 blockstates via TripWires.
 *
 * [BlockType.WALL] -> Allows for 16 blockstates via Glow Lichen.
*/
fun Block.getBlockyDecorationDataFromItem(blockId: Int): BlockData {
    val blockyType = BlockyTypeQuery.firstOrNull {
        it.entity.get<BlockyInfo>()?.modelId?.toInt() == blockId
    }?.entity?.get<BlockyType>() ?: return blockData

    when (blockyType.blockType) {
        BlockType.GROUND -> {
            setType(Material.TRIPWIRE, false)
            val data = blockData as Tripwire

            val inAttachedRange = blockId in 33..64
            val inPoweredRange = blockId in 17..32 || blockId in 49..64
            val northRange = 2..64
            val southRange = 5..64
            val eastRange = 3..64
            val westRange = 9..64

            data.isDisarmed = true
            if (inAttachedRange) data.isAttached = true
            if (inPoweredRange) data.isPowered = true
            if (blockId in northRange step 2) data.setFace(BlockFace.NORTH, true)

            for (i in westRange) {
                if (blockId !in i..i + 7) westRange step 8
                else data.setFace(BlockFace.WEST, true)
            }

            for (i in southRange) {
                if (blockId !in i..i + 4) southRange step 4
                else data.setFace(BlockFace.SOUTH, true)
            }

            for (i in eastRange) {
                if (blockId in i..i + 1) eastRange step 2
                else data.setFace(BlockFace.EAST, true)
            }
            blockData = data
        }
        BlockType.WALL -> {
            setType(Material.GLOW_LICHEN, false)
            val data = blockData as GlowLichen
            val upRange = 17..32
            val northRange = 2..32
            val southRange = 5..32
            val eastRange = 3..32
            val westRange = 9..32

            data.isWaterlogged = true
            if (blockId in upRange) data.setFace(BlockFace.UP, true)
            if (blockId in northRange step 2) data.setFace(BlockFace.NORTH, true)

            for (i in westRange) {
                if (blockId !in i..i + 7) westRange step 8
                else data.setFace(BlockFace.WEST, true)
            }

            for (i in southRange) {
                if (blockId !in i..i + 4) southRange step 4
                else data.setFace(BlockFace.SOUTH, true)
            }

            for (i in eastRange) {
                if (blockId in i..i + 1) eastRange step 2
                else data.setFace(BlockFace.EAST, true)
            }
            blockData = data
        }
        else -> return blockData
    }
    blockMap.putIfAbsent(blockData, blockId)
    return blockData
}

fun Block.getPrefabFromBlock(): GearyEntity? {
    val blockyBlock = BlockyTypeQuery.firstOrNull {
        it.entity.get<BlockyInfo>()?.modelId?.toInt() == blockMap[blockData]
    }?.entity ?: return null

    return blockyBlock
}
