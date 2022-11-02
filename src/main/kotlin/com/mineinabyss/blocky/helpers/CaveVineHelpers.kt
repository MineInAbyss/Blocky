package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.api.events.cavevineblock.CaveVineBlockBreakEvent
import com.mineinabyss.blocky.blockMap
import com.mineinabyss.blocky.components.core.BlockyBlock
import com.mineinabyss.blocky.components.core.BlockyInfo
import com.mineinabyss.blocky.components.features.BlockyLight
import com.mineinabyss.idofront.events.call
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
    val gearyBlock = block.gearyEntity ?: return
    if (!gearyBlock.has<BlockyInfo>() || !gearyBlock.has<BlockyBlock>()) return

    val caveVineEvent = CaveVineBlockBreakEvent(block, player).run { this.call(); this }
    if (caveVineEvent.isCancelled) return

    if (gearyBlock.has<BlockyLight>()) handleLight.removeBlockLight(block.location)
    if (gearyBlock.has<BlockyInfo>()) handleBlockyDrops(block, player)
    block.setType(Material.AIR, false)
    if (block.getRelative(BlockFace.DOWN).type == Material.CAVE_VINES)
        breakCaveVineBlock(block.getRelative(BlockFace.DOWN), null)
}
