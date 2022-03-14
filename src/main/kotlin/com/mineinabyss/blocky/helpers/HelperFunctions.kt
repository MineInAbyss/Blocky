package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.BlockyTypeQuery
import com.mineinabyss.blocky.components.BlockType
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.blocky.components.BlockyType
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.idofront.messaging.broadcastVal
import org.bukkit.Instrument
import org.bukkit.Material
import org.bukkit.Note
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.GlowLichen
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.block.data.type.Tripwire

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
    return data
}

fun Block.getBlockyBlockFromBlock(): GearyEntity? {
    val data = blockData as? NoteBlock ?: return null
    val instrumentId = when (data.instrument) {
        Instrument.BASS_DRUM -> 0
        Instrument.PIANO -> 1
        Instrument.SNARE_DRUM -> 2
        Instrument.STICKS -> 3
        Instrument.BASS_GUITAR -> 4
        Instrument.FLUTE -> 5
        Instrument.BELL -> 6
        Instrument.GUITAR -> 7
        Instrument.CHIME -> 8
        Instrument.XYLOPHONE -> 9
        Instrument.IRON_XYLOPHONE -> 10
        Instrument.COW_BELL -> 11
        Instrument.DIDGERIDOO -> 12
        Instrument.BIT -> 13
        Instrument.BANJO -> 14
        Instrument.PLING -> 15
        else -> 0
    }
    val blockId = data.note.id + instrumentId * 25
    val blockyBlock = BlockyTypeQuery.firstOrNull {
        it.entity.get<BlockyInfo>()?.modelId?.toInt() == blockId
    }?.entity ?: return null

    return blockyBlock
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
    blockyType.blockType.broadcastVal()
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
            data.allowedFaces.broadcastVal()
            data.allowedFaces.broadcastVal()
            data.isWaterlogged.broadcastVal()



            blockData = data
        }
        else -> return blockData
    }
    return blockData
}

/*
fun Block.getBlockyDecorationBlockFromBlock(): GearyEntity? {
    val data = blockData as? Tripwire ?: return null
    val blockId = 1 + 1 * 25
    val blockyDecoration = BlockyTypeQuery.firstOrNull {
        it.entity.get<BlockyInfo>()?.modelId?.toInt() == blockId
    }?.entity ?: return null
    return blockyDecoration
}*/
