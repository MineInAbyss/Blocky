package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.blockMap
import com.mineinabyss.blocky.components.BlockyBlock
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.blocky.components.BlockyLight
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.CaveVines
import org.bukkit.entity.Player

fun BlockyBlock.getBlockyCaveVine() : BlockData {
    return blockMap.filter { it.key is CaveVines && it.key.material == Material.CAVE_VINES && it.value == blockId }.keys.first() as CaveVines
}

fun Block.isBlockyCaveVine() : Boolean {
    return blockMap.contains(blockData) && type == Material.CAVE_VINES
}

fun breakCaveVineBlock(block: Block, player: Player?) {
    val gearyBlock = block.getGearyEntityFromBlock() ?: return
    if (!gearyBlock.has<BlockyInfo>() || !gearyBlock.has<BlockyBlock>()) return

    if (gearyBlock.has<BlockyLight>()) handleLight.removeBlockLight(block.location)
    if (gearyBlock.has<BlockyInfo>()) handleBlockyDrops(block, player)
    block.setType(Material.AIR, false)
    if (block.getRelative(BlockFace.DOWN).type == Material.CAVE_VINES)
        breakCaveVineBlock(block.getRelative(BlockFace.DOWN), null)
}
