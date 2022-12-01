package com.mineinabyss.blocky.helpers

import com.github.shynixn.mccoroutine.bukkit.launch
import com.jeff_media.morepersistentdatatypes.DataType
import com.mineinabyss.blocky.api.events.block.BlockyBlockBreakEvent
import com.mineinabyss.blocky.blockMap
import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.blocky.components.core.BlockyBlock
import com.mineinabyss.blocky.components.core.BlockyInfo
import com.mineinabyss.blocky.components.features.BlockyLight
import com.mineinabyss.blocky.components.features.BlockyTallWire
import com.mineinabyss.idofront.events.call
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

fun Block.fixClientsideUpdate() {
    if (!location.isWorldLoaded || !location.isChunkLoaded) return
    val players = location.world.getNearbyPlayers(location, 20.0)
    val chunk = location.chunk
    val map = mutableMapOf<Location, BlockData>()
    blockyPlugin.launch {
        delay(1)
        for (x in ((chunk.x shl 4) - 17)..chunk.x + 32)
            for (z in ((chunk.z shl 4) - 17)..chunk.x + 32)
                for (y in (location.y - 3).toInt()..(location.y + 3).toInt()) {
                    val block = location.world.getBlockAt(x, y, z)
                    if (block.type == Material.TRIPWIRE) map[block.location] = block.blockData
                }
        players.forEach { it.sendMultiBlockChange(map) }
    }
}

fun breakWireBlock(block: Block, player: Player?) {
    val gearyBlock = block.gearyEntity ?: return
    if (!gearyBlock.has<BlockyInfo>() || !gearyBlock.has<BlockyBlock>()) return

    val blockyEvent = BlockyBlockBreakEvent(block, player).run { this.call(); this }
    if (blockyEvent.isCancelled) return

    if (gearyBlock.has<BlockyLight>()) handleLight.removeBlockLight(block.location)
    if (gearyBlock.has<BlockyInfo>()) handleBlockyDrops(block, player)
    if (gearyBlock.has<BlockyTallWire>()) handleTallWire(block)

    block.type = Material.AIR
    block.fixClientsideUpdate()

    val aboveBlock = block.getRelative(BlockFace.UP)
    if (aboveBlock.type == Material.TRIPWIRE)
        breakWireBlock(aboveBlock, null)
}

fun Player.isInBlock(block: Block): Boolean {
    return block.location.let { l ->
        (location.blockX == l.blockX && (location.blockY == l.blockY || location.blockY + 1 == l.blockY) && location.blockZ == l.blockZ)
    }
}

fun handleTallWire(block: Block) {
    block.persistentDataContainer.getOrDefault(
        BlockyTallWire().getKey(),
        DataType.LOCATION,
        block.getRelative(BlockFace.UP).location
    ).block.let {
        it.customBlockData.clear()
        it.type = Material.AIR
    }
}
