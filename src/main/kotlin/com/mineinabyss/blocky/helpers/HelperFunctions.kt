package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.BlockyTypeQuery
import com.mineinabyss.blocky.components.BlockType
import com.mineinabyss.blocky.components.BlockyBlock
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import org.bukkit.Instrument
import org.bukkit.Location
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
    data.instrument = Instrument.getByType((blockId / 25 % 400).toByte()) ?: return blockData
    data.note = Note((blockId % 25))
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
fun Block.getBlockyDecorationDataFromItem(blockId: Int, blockType: BlockType): BlockData {
    val blockyBlock = BlockyTypeQuery.firstOrNull {
        it.entity.get<BlockyBlock>()?.blockId == blockId &&
        it.entity.get<BlockyBlock>()?.blockType == blockType
    }?.entity?.get<BlockyBlock>() ?: return blockData

    when (blockyBlock.blockType) {
        BlockType.GROUND -> {
            setType(Material.TRIPWIRE, false)
            val data = blockData as Tripwire
            val inAttachedRange = blockId in 33..64
            val inPoweredRange = blockId in 17..32 || blockId in 49..64
            val inDisarmedRange = blockId in 65..128
            val northRange = 2..64
            val southRange = 5..64
            val eastRange = 3..64
            val westRange = 9..64

            if (inDisarmedRange) data.isDisarmed = true
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
                if (blockId !in i..i + 1) eastRange step 2
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
        it.entity.get<BlockyBlock>()?.blockId == blockMap[blockData]
    }?.entity ?: return null

    return blockyBlock
}

fun Block.updateBlockyStates() {
    val locs: MutableList<Location> = ArrayList(5 * 5 * 5)
    for (x in -5..5) {
        for (y in -5..5) {
            for (z in -5..5) {
                locs.add(
                    Location(
                        location.world,
                        location.x + x.toDouble(),
                        location.y + y.toDouble(),
                        location.z + z.toDouble()
                    )
                )
            }
        }
    }
    locs.forEach {
        if (it.block.type == Material.STRING || it.block.type == Material.TRIPWIRE || it.block.type == Material.GLOW_LICHEN || it.block.type == Material.NOTE_BLOCK) {
            it.block.state.update(true, false)
        }
    }
}
