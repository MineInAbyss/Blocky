package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.api.BlockyBlocks.blockyBlock
import com.mineinabyss.blocky.components.core.VanillaNoteBlock
import com.mineinabyss.blocky.components.features.blocks.BlockyDirectional
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.gearyBlocks
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.messaging.broadcast
import org.bukkit.Instrument
import org.bukkit.Note
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.entity.Player
import org.bukkit.event.block.NotePlayEvent

/**
 * Gets the blockdata of a given Blocky-block from a GearyEntity
 * Note: For directional Blocky-blocks, use [getBlockyNoteBlock(org.bukkit.block.BlockFace, org.bukkit.entity.Player)] instead
 * This will just use the parent-block of the BlockyDirectional component, which is not what you want
 */
fun PrefabKey.getBlockyNoteBlock(): BlockData {
    val blockID = (this.toEntityOrNull()?.get<BlockyDirectional>()?.parentBlock?.blockyBlock ?: blockyBlock)?.blockId ?: 0
    return gearyBlocks.block2Prefab.blockMap[SetBlock.BlockType.NOTEBLOCK]!![blockID]
}
fun GearyEntity.getBlockyNoteBlock(face: BlockFace = BlockFace.NORTH, player: Player? = null): BlockData {
    val directional = GenericHelpers.getDirectionalId(this, face, player)
    return gearyBlocks.block2Prefab.blockMap[SetBlock.BlockType.NOTEBLOCK]!![directional]
}

// If the blockmap doesn't contain data, it means it's a vanilla note block
val Block.isVanillaNoteBlock get() = blockData is NoteBlock && blockData !in gearyBlocks.block2Prefab
val BlockData.isVanillaNoteBlock get() = this is NoteBlock && this !in gearyBlocks.block2Prefab

val Block.isBlockyNoteBlock get() = blockData is NoteBlock && blockData in gearyBlocks.block2Prefab
val BlockData.isBlockyNoteBlock get() = this is NoteBlock && this in gearyBlocks.block2Prefab

// Updates the note stored in the pdc by 1
fun Block.updateBlockyNote() {
    val note = ((this.persistentDataContainer.decode<VanillaNoteBlock>()?.note ?: 0) + 1) % 25
    this.persistentDataContainer.encode(VanillaNoteBlock(note))
}

fun Block.blockyNote() = Note(this.persistentDataContainer.decode<VanillaNoteBlock>()?.note ?: 0)

fun Block.blockyInstrument(): Instrument {
    return instrumentList.firstOrNull {
        getRelative(BlockFace.DOWN).type.toString().lowercase() in it.types
    }?.instrument ?: Instrument.PIANO
}

private class InstrumentMap(val instrument: Instrument, vararg types: String) {
    val types: List<String> = types.toList()
}

private val instrumentList = listOf(
    InstrumentMap(Instrument.BELL, "gold_block"),
    InstrumentMap(Instrument.FLUTE, "clay"),
    InstrumentMap(Instrument.CHIME, "packed_ice"),
    InstrumentMap(Instrument.GUITAR, "wool"),
    InstrumentMap(Instrument.XYLOPHONE, "bone_block"),
    InstrumentMap(Instrument.IRON_XYLOPHONE, "iron_block"),
    InstrumentMap(Instrument.COW_BELL, "soul_sand"),
    InstrumentMap(Instrument.DIDGERIDOO, "pumpkin"),
    InstrumentMap(Instrument.BIT, "emerald_block"),
    InstrumentMap(Instrument.BANJO, "hay_bale"),
    InstrumentMap(Instrument.PLING, "glowstone"),
    InstrumentMap(Instrument.BELL, "gold_block"),
    InstrumentMap(Instrument.BASS_DRUM, "stone", "netherrack", "bedrock", "observer", "coral", "obsidian", "anchor", "quartz"),
    InstrumentMap(Instrument.BASS_GUITAR, "wood"),
    InstrumentMap(Instrument.SNARE_DRUM, "sand", "gravel", "concrete_powder", "soul_soil"),
    InstrumentMap(Instrument.STICKS, "glass","sea_lantern", "beacon"),
)
