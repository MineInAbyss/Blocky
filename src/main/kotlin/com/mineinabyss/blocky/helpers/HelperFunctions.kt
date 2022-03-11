package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.BlockyTypeQuery
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import org.bukkit.Instrument
import org.bukkit.Note
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.NoteBlock

fun Block.getBlockyBlockDataFromItem(blockId: Int) : BlockData {
    val data = blockData as NoteBlock
    val instrumentId = (blockId / 25).toDouble().toInt()
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

fun Block.getBlockyBlockFromBlock() : GearyEntity? {
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
        Instrument.BANJO  -> 14
        Instrument.PLING -> 15
        else -> 0
    }
    val blockId = data.note.id + instrumentId * 25
    val blockyBlock = BlockyTypeQuery.firstOrNull {
        it.entity.get<BlockyInfo>()?.modelId?.toInt() == blockId
    }?.entity ?: return null

    return blockyBlock
}