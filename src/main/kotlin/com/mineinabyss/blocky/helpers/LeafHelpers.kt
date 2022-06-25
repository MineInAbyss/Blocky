package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.blockMap
import com.mineinabyss.blocky.components.BlockyBlock
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Leaves

fun BlockyBlock.getBlockyLeaf(): BlockData {
    return blockMap.filter { it.key is Leaves && leafList.contains(it.key.material) && it.value == blockId }.keys.first() as Leaves
}

fun Block.isBlockyLeaf(): Boolean {
    return blockMap.contains(blockData) && leafList.contains(type)
}

// Azalea before Acacia because of biome tinting
val leafList =
    listOf(
        Material.AZALEA_LEAVES,
        Material.ACACIA_LEAVES,
        Material.BIRCH_LEAVES,
        Material.DARK_OAK_LEAVES,
        Material.FLOWERING_AZALEA_LEAVES,
        Material.JUNGLE_LEAVES,
        Material.MANGROVE_LEAVES,
        Material.OAK_LEAVES,
        Material.SPRUCE_LEAVES
    )

internal fun getLeafMaterial(int: Int) : Material {
    return when (int) {
        in 1..7 -> leafList[0]
        in 8..14 -> leafList[1]
        in 15..21 -> leafList[2]
        in 22..28 -> leafList[3]
        in 29..35 -> leafList[4]
        in 36..42 -> leafList[5]
        in 43..49 -> leafList[6]
        in 50..56 -> leafList[7]
        in 57..63 -> leafList[8]
        else -> leafList[0]
    }
}

internal fun getLeafDistance(int: Int) : Int {
    if (leafConfig.shouldReserveOnePersistentLeafPerType) {
        return when {
            (int % 7) == 1 -> 2
            (int % 7) == 2 -> 3
            (int % 7) == 3 -> 4
            (int % 7) == 4 -> 5
            (int % 7) == 5 -> 6
            (int % 7) == 6 -> 7
            else -> 1
        }
    }
    else {
        return when {
            (int % 7) == 1 -> 1
            (int % 7) == 2 -> 2
            (int % 7) == 3 -> 3
            (int % 7) == 4 -> 4
            (int % 7) == 5 -> 5
            (int % 7) == 6 -> 6
            else -> 7
        }
    }
}

internal fun getBlockMapEntryForLeaf(int: Int) : Int {
    return if (leafConfig.shouldReserveOnePersistentLeafPerType) int
    else {
        when (int) {
            in 1..6 -> int
            in 7..13 -> int - 1
            in 14..20 -> int - 2
            in 21..27 -> int - 3
            in 28..34 -> int - 4
            in 35..41 -> int - 5
            in 42..48 -> int - 6
            in 49..55 -> int - 7
            in 56..62 -> int - 8
            else -> int
        }
    }
}

//TODO See if waterlogging state can also be used
