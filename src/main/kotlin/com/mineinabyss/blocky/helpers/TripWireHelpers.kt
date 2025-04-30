package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.api.BlockyBlocks.gearyBlocks
import com.mineinabyss.blocky.api.events.block.BlockyBlockBreakEvent
import com.mineinabyss.blocky.components.features.wire.BlockyTallWire
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.toGearyOrNull
import com.nexomc.protectionlib.ProtectionLib
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player

context(Geary)
fun SetBlock.blockyTripWire() = gearyBlocks.block2Prefab.blockMap[blockType]!![blockId]

fun breakWireBlock(block: Block, player: Player?): Boolean {
    val gearyBlock = block.toGearyOrNull() ?: return false
    if (!gearyBlock.has<SetBlock>()) return false

    player?.let {
        if (!BlockyBlockBreakEvent(block, player).callEvent()) return false
        if (!ProtectionLib.canBreak(player, block.location)) return false
        handleBlockyDrops(block, player)
    }


    //if (gearyBlock.has<BlockyLight>()) BlockLight.removeBlockLight(block.location)
    if (gearyBlock.has<BlockyTallWire>()) handleTallWire(block)

    block.type = Material.AIR

    val aboveBlock = block.getRelative(BlockFace.UP)
    if (aboveBlock.type == Material.TRIPWIRE)
        breakWireBlock(aboveBlock, null)
    return true
}

fun handleTallWire(block: Block) {
    val tallWire = block.container { decode<BlockyTallWire>() }
        ?: BlockyTallWire(block.getRelative(BlockFace.UP).location)
    tallWire.baseWire?.let {
        it.customBlockData.clear()
        it.type = Material.AIR
    }
}
