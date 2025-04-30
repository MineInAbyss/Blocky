package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.api.events.block.BlockyBlockBreakEvent
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.blocks.BlockTracking
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.toGearyOrNull
import com.nexomc.protectionlib.ProtectionLib
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object CaveVineHelpers {
    val defaultBlockData = Material.CAVE_VINES.createBlockData()

    context(Geary)
    fun blockyCaveVine(setBlock: SetBlock): BlockData {
        return getAddon(BlockTracking).block2Prefab.blockMap[setBlock.blockType]!![setBlock.blockId]
    }

    fun isBlockyCaveVine(block: Block) = block.type == Material.CAVE_VINES && block.blockData in block.world.toGeary()
        .getAddon(BlockTracking).block2Prefab

    context(Geary)
    fun isBlockyCaveVine(itemStack: ItemStack) = itemStack.decode<SetBlock>()?.blockType == SetBlock.BlockType.CAVEVINE

    fun breakCaveVineBlock(block: Block, player: Player?): Boolean = with(block.world.toGeary()) {
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
