package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.blockMap
import com.mineinabyss.blocky.components.BlockyBlock
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Door
import org.bukkit.block.data.type.Gate
import org.bukkit.block.data.type.TrapDoor
import org.bukkit.block.data.type.Wall

fun BlockyBlock.getBlockyDoor(): Door {
    return blockMap.filter { it.key is Door && it.key.material == getDoorType(blockId) && it.value == blockId }.keys.first() as Door
}

fun BlockyBlock.getBlockyTrapDoor(): TrapDoor {
    return blockMap.filter { it.key is TrapDoor && it.key.material == getTrapDoorType(blockId) && it.value == blockId }.keys.first() as TrapDoor
}

fun BlockyBlock.getBlockyFenceGate(): Gate {
    return blockMap.filter { it.key is Gate && it.key.material == getFenceGate(blockId) && it.value == blockId }.keys.first() as Gate
}
fun getDoorType(i: Int) : Material? {
    return when (i) {
        1 -> Material.ACACIA_DOOR
        2 -> Material.BIRCH_DOOR
        3 -> Material.CRIMSON_DOOR
        4 -> Material.DARK_OAK_DOOR
        5 -> Material.JUNGLE_DOOR
        6 -> Material.OAK_DOOR
        7 -> Material.SPRUCE_DOOR
        8 -> Material.WARPED_DOOR
        //9 -> Material.MANGROVE_DOOR
        else -> null
    }
}

fun getTrapDoorType(i: Int) : Material? {
    return when (i) {
        1 -> Material.ACACIA_TRAPDOOR
        2 -> Material.BIRCH_TRAPDOOR
        3 -> Material.CRIMSON_TRAPDOOR
        4 -> Material.DARK_OAK_TRAPDOOR
        5 -> Material.JUNGLE_TRAPDOOR
        6 -> Material.OAK_TRAPDOOR
        7 -> Material.SPRUCE_TRAPDOOR
        8 -> Material.WARPED_TRAPDOOR
        //9 -> Material.MANGROVE_TRAPDOOR
        else -> null
    }
}

fun getFenceGate(i: Int) : Material? {
    return when (i) {
        1 -> Material.ACACIA_FENCE_GATE
        2 -> Material.BIRCH_FENCE_GATE
        3 -> Material.CRIMSON_FENCE_GATE
        4 -> Material.DARK_OAK_FENCE_GATE
        5 -> Material.JUNGLE_FENCE_GATE
        6 -> Material.OAK_FENCE_GATE
        7 -> Material.SPRUCE_FENCE_GATE
        8 -> Material.WARPED_FENCE_GATE
        //9 -> Material.MANGROVE_FENCE_GATE
        else -> null
    }
}

fun Block.isConnectedToWall() : Boolean {
    BlockFace.values().filter { it.isCartesian && it.modY == 0 && it != BlockFace.SELF }.forEach { face ->
        if (getRelative(face).blockData is Wall) return true
    }
    return false
}

