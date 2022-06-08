package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.blockMap
import com.mineinabyss.blocky.components.BlockyBlock
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Door
import org.bukkit.block.data.type.Gate
import org.bukkit.block.data.type.TrapDoor

fun BlockyBlock.getBlockyDoor(): BlockData {
    return blockMap.filter { it.key is Door && it.key.material == getDoorType(blockId) && it.value == blockId }.keys.first() as Door
}

fun BlockyBlock.getBlockyTrapDoor(): BlockData {
    return blockMap.filter { it.key is TrapDoor && it.key.material == getTrapDoorType(blockId) && it.value == blockId }.keys.first() as TrapDoor
}

fun BlockyBlock.getBlockyFenceGate(): BlockData {
    return blockMap.filter { it.key is Gate && it.key.material == getFenceGate(blockId) && it.value == blockId }.keys.first() as Gate
}
