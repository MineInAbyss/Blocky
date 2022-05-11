package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.blockMap
import com.mineinabyss.blocky.components.*
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
    val blockBelow = blockLoc.block.getRelative(BlockFace.DOWN)
    val blockAbove = blockLoc.block.getRelative(BlockFace.UP)
    var loc = blockLoc.add(5.0, 0.0, 5.0)
    val players = blockLoc.world.getNearbyPlayers(blockLoc, 20.0)

    if (blockBelow.type == Material.TRIPWIRE) {
        players.forEach {
            it.sendBlockChange(blockBelow.location, blockBelow.blockData)
        }
    }
    if (blockAbove.type == Material.TRIPWIRE) {
        players.forEach {
            it.sendBlockChange(blockAbove.location, blockAbove.blockData)
        }
    }

    for (i in 0..8) {
        for (j in 0..8) {
            if (loc.block.type == Material.TRIPWIRE) {
                players.forEach {
                    it.sendBlockChange(loc, blockLoc.block.blockData)
                }
            }
            loc = loc.subtract(0.0, 0.0, 1.0)
        }
        loc = loc.add(-1.0, 0.0, 9.0)
    }
}

fun breakTripwireBlock(block: Block, player: Player) {
    val blockyWire = block.getPrefabFromBlock()?.toEntity() ?: return
    val blockySound = blockyWire.get<BlockySound>()
    blockyWire.get<BlockyInfo>() ?: return
    block.state.update(true, false)

    block.setType(Material.AIR, false)
    if (blockyWire.has<BlockySound>()) block.world.playSound(block.location, blockySound!!.placeSound, 1.0f, 0.8f)
    if (blockyWire.has<BlockyLight>()) removeBlockLight(block.location)
    if (blockyWire.has<BlockDrops>()) handleBlockyDrops(block, player)
}

fun isStandingInside(player: Player, block: Block): Boolean {
    val playerLocation = player.location
    val blockLocation = block.location
    return (playerLocation.blockX == blockLocation.blockX && (playerLocation.blockY == blockLocation.blockY
            || playerLocation.blockY + 1 == blockLocation.blockY)
            && playerLocation.blockZ == blockLocation.blockZ)
}


