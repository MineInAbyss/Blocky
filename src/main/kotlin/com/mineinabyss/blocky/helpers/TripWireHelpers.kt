package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.api.events.block.BlockyBlockBreakEvent
import com.mineinabyss.blocky.components.features.wire.BlockyTallWire
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.gearyBlocks
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.toGearyOrNull
import com.mineinabyss.idofront.destructure.component1
import com.mineinabyss.idofront.destructure.component2
import com.mineinabyss.idofront.destructure.component3
import io.th0rgal.protectionlib.ProtectionLib
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player

fun SetBlock.getBlockyTripWire() = gearyBlocks.block2Prefab.blockMap[blockType]!![blockId]

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

fun Player.isInBlock(block: Block): Boolean {
    val expand = 0.3
    val (px, py, pz) = location
    val (x, y, z) = block.location
    return px in x - expand..x + 1 + expand &&
            py in y - 1 ..< y + 1 &&
            pz in z - expand..z + 1 + expand
}

fun handleTallWire(block: Block) {
    val tallWire = block.persistentDataContainer.decode<BlockyTallWire>()
        ?: BlockyTallWire(block.getRelative(BlockFace.UP).location)
    tallWire.baseWire?.let {
        it.customBlockData.clear()
        it.type = Material.AIR
    }
}
