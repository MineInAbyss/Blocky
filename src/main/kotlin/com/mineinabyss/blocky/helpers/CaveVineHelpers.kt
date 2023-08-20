package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.api.events.block.BlockyBlockBreakEvent
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.gearyBlocks
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.toGearyOrNull
import io.th0rgal.protectionlib.ProtectionLib
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player

object CaveVineHelpers {
    fun getBlockyCaveVine(setBlock: SetBlock) : BlockData {
        return gearyBlocks.block2Prefab.blockMap[setBlock.blockType]!![setBlock.blockId]
    }

    fun isBlockyCaveVine(block: Block) = block.type == Material.CAVE_VINES && block.blockData in gearyBlocks.block2Prefab

    fun breakCaveVineBlock(block: Block, player: Player?): Boolean {
        val gearyBlock = block.toGearyOrNull() ?: return false
        if (!gearyBlock.has<SetBlock>()) return false

        player?.let {
            if (!BlockyBlockBreakEvent(block, player).callEvent()) return false
            if (!ProtectionLib.canBreak(player, block.location)) return false
            handleBlockyDrops(block, player)
        }

        block.setType(Material.AIR, false)
        if (block.getRelative(BlockFace.DOWN).type == Material.CAVE_VINES)
            breakCaveVineBlock(block.getRelative(BlockFace.DOWN), null)
        return true
    }

}
