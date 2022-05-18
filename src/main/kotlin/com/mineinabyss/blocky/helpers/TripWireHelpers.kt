package com.mineinabyss.blocky.helpers

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.blocky.blockMap
import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.blocky.components.*
import kotlinx.coroutines.delay
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Tripwire
import org.bukkit.entity.Player

fun BlockyBlock.getBlockyTripWire(): BlockData {
    return blockMap.filter { it.key is Tripwire && it.key.material == Material.TRIPWIRE && it.value == blockId }.keys.first() as Tripwire
}

fun fixClientsideUpdate(blockLoc: Location) {
    val players = blockLoc.world.getNearbyPlayers(blockLoc, 20.0)
    val chunk = blockLoc.chunk
    for (x in (chunk.x shl 4)..chunk.x + 16)
        for (z in (chunk.z shl 4)..chunk.z + 16)
            for (y in (blockLoc.y - 10).toInt()..(blockLoc.y + 10).toInt()) {
                val block = blockLoc.world.getBlockAt(x, y, z)
                if (block.type == Material.TRIPWIRE)
                    players.forEach {
                        it.sendBlockChange(block.location, block.blockData)
                    }
            }
}

fun breakTripwireBlock(block: Block, player: Player?) {
    if (!block.hasBlockyInfo || !block.isBlockyBlock) return
    block.state.update(true, false)

    if (block.hasBlockySound) block.world.playSound(block.location, block.blockySound!!.placeSound, 1.0f, 0.8f)
    if (block.hasBlockyLight) removeBlockLight(block.location)
    if (block.hasBlockyDrops) handleBlockyDrops(block, player)
    block.setType(Material.AIR, false)

    blockyPlugin.launch {
        delay(1)
        fixClientsideUpdate(block.location)
    }
}

fun isStandingInside(player: Player, block: Block): Boolean {
    val playerLocation = player.location
    val blockLocation = block.location
    return (playerLocation.blockX == blockLocation.blockX && (playerLocation.blockY == blockLocation.blockY
            || playerLocation.blockY + 1 == blockLocation.blockY)
            && playerLocation.blockZ == blockLocation.blockZ)
}


