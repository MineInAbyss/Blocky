package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.BlockyTypeQuery
import com.mineinabyss.blocky.components.BlockType
import com.mineinabyss.blocky.components.BlockyBlock
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.GlowLichen
import org.bukkit.block.data.type.Tripwire
import org.bukkit.entity.Player

val blockMap: MutableMap<BlockData, Int> = mutableMapOf()




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

    blockData = when (blockyBlock.blockType) {
        BlockType.GROUND -> {
            setType(Material.TRIPWIRE, false)
            blockyBlock.getBlockyTripWireDataFromPrefab() ?: return blockData
        }
        BlockType.WALL -> {
            setType(Material.GLOW_LICHEN, false)
            blockyBlock.getBlockyGlowLichenDataFromPrefab()
        }
        else -> return blockData
    }
    blockMap.putIfAbsent(blockData, blockId)
    return blockData
}

fun BlockyBlock.getBlockyTripWireDataFromPrefab() : BlockData? {
    var blockData = Bukkit.createBlockData(Material.TRIPWIRE)
    val data = blockData as? Tripwire ?: return null
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
    return blockData
}

fun BlockyBlock.getBlockyGlowLichenDataFromPrefab() : BlockData {
    var blockData = Bukkit.createBlockData(Material.GLOW_LICHEN)
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
    return blockData
}

val REPLACEABLE_BLOCKS =
    listOf(
        Material.SNOW, Material.VINE, Material.GRASS, Material.TALL_GRASS, Material.SEAGRASS, Material.FERN,
        Material.LARGE_FERN
    )

fun isStandingInside(player: Player, block: Block): Boolean {
    val playerLocation = player.location
    val blockLocation = block.location
    return (playerLocation.blockX == blockLocation.blockX && (playerLocation.blockY == blockLocation.blockY
            || playerLocation.blockY + 1 == blockLocation.blockY)
            && playerLocation.blockZ == blockLocation.blockZ)
}


