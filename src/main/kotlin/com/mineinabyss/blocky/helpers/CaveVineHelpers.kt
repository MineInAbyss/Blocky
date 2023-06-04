package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.api.BlockyBlocks.gearyEntity
import com.mineinabyss.blocky.api.events.block.BlockyBlockBreakEvent
import com.mineinabyss.blocky.blockMap
import com.mineinabyss.blocky.components.core.BlockyBlock
import com.mineinabyss.blocky.components.features.BlockyLight
import io.th0rgal.protectionlib.ProtectionLib
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.CaveVines
import org.bukkit.entity.Player

fun BlockyBlock.getBlockyCaveVine() : BlockData {
    return blockMap.filter { it.key is CaveVines && it.key.material == Material.CAVE_VINES && it.value == blockId }.keys.first() as CaveVines
}

val Block.isBlockyCaveVine: Boolean get() =
    type == Material.CAVE_VINES && blockData in blockMap

fun breakCaveVineBlock(block: Block, player: Player?): Boolean {
    val gearyBlock = block.gearyEntity ?: return false
    if (!gearyBlock.has<BlockyBlock>()) return false

    player?.let {
        if (!BlockyBlockBreakEvent(block, player).callEvent()) return false
        if (!ProtectionLib.canBreak(player, block.location)) return false
    }

    if (gearyBlock.has<BlockyLight>()) handleLight.removeBlockLight(block.location)
    handleBlockyDrops(block, player)
    block.setType(Material.AIR, false)
    if (block.getRelative(BlockFace.DOWN).type == Material.CAVE_VINES)
        breakCaveVineBlock(block.getRelative(BlockFace.DOWN), null)
    return true
}
