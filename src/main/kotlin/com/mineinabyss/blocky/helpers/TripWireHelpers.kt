package com.mineinabyss.blocky.helpers

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.blocky.blockMap
import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.blocky.components.BlockyBlock
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.blocky.components.BlockyLight
import com.mineinabyss.blocky.components.BlockySound
import kotlinx.coroutines.delay
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Tripwire
import org.bukkit.entity.Player

fun BlockyBlock.getBlockyTripWire(): BlockData {
    return blockMap.filter { it.key is Tripwire && it.key.material == Material.TRIPWIRE && it.value == blockId }.keys.first() as Tripwire
}

fun fixClientsideUpdate(blockLoc: Location) {
    val players = blockLoc.world.getNearbyPlayers(blockLoc, 20.0)
    val chunk = blockLoc.chunk
    val map = mutableMapOf<Location, BlockData>()
    for (x in ((chunk.x shl 4)-17)..chunk.x + 32)
        for (z in ((chunk.z shl 4)-17)..chunk.x + 32)
            for (y in (blockLoc.y - 3).toInt()..(blockLoc.y + 3).toInt()) {
                val block = blockLoc.world.getBlockAt(x, y, z)
                if (block.type == Material.TRIPWIRE) map[block.location] = block.blockData
            }
    players.forEach { it.sendMultiBlockChange(map) }
}

fun breakTripwireBlock(block: Block, player: Player?) {
    val gearyBlock = block.getGearyEntityFromBlock() ?: return
    if (!gearyBlock.has<BlockyInfo>() || !gearyBlock.has<BlockyBlock>()) return
    block.state.update(true, false)

    if (gearyBlock.has<BlockySound>()) block.world.playSound(block.location, gearyBlock.get<BlockySound>()!!.placeSound, 1.0f, 0.8f)
    if (gearyBlock.has<BlockyLight>()) removeBlockLight(block.location)
    if (gearyBlock.has<BlockyInfo>()) handleBlockyDrops(block, player)
    block.setType(Material.AIR, false)
    blockyPlugin.launch {
        delay(1)
        fixClientsideUpdate(block.location)
    }
    if (block.getRelative(BlockFace.UP).type == Material.TRIPWIRE)
        breakTripwireBlock(block.getRelative(BlockFace.UP), null)
}

fun isStandingInside(player: Player, block: Block): Boolean {
    val playerLocation = player.location
    val blockLocation = block.location
    return (playerLocation.blockX == blockLocation.blockX && (playerLocation.blockY == blockLocation.blockY
            || playerLocation.blockY + 1 == blockLocation.blockY)
            && playerLocation.blockZ == blockLocation.blockZ)
}
