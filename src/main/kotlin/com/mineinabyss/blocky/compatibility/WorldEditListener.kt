package com.mineinabyss.blocky.compatibility

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent
import com.mineinabyss.blocky.blockMap
import com.mineinabyss.blocky.components.core.BlockyBlock.BlockType
import com.mineinabyss.blocky.components.features.BlockyLight
import com.mineinabyss.blocky.components.features.BlockySeat
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.blocky.systems.BlockyBlockQuery
import com.mineinabyss.blocky.systems.BlockyBlockQuery.prefabKey
import com.mineinabyss.blocky.systems.BlockyBlockQuery.type
import com.sk89q.worldedit.WorldEditException
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.event.extent.EditSessionEvent
import com.sk89q.worldedit.extent.AbstractDelegateExtent
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.util.eventbus.Subscribe
import com.sk89q.worldedit.world.block.BlockStateHolder
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener


class WorldEditListener : Listener {

    @Subscribe
    fun EditSessionEvent.onEditSession() {
        if (world == null) return

        extent = object : AbstractDelegateExtent(extent) {
            @Deprecated("Deprecated in Java")
            @Throws(WorldEditException::class)
            override fun <T : BlockStateHolder<T>?> setBlock(pos: BlockVector3, block: T): Boolean {
                val blockData = BukkitAdapter.adapt(block!!) // Block is never null, but the compiler doesn't know that
                val world = Bukkit.getWorld(world!!.name) ?: return extent.setBlock(pos.x, pos.y, pos.z, block)
                val loc = Location(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())

                // Handle removing old entities before setting new
                val oldEntity = loc.block.prefabKey?.toEntityOrNull()
                    ?: return extent.setBlock(pos.x, pos.y, pos.z, block)

                if (oldEntity.has<BlockyLight>())
                    handleLight.removeBlockLight(loc)
                if (oldEntity.has<BlockySeat>()) //TODO Consider if this should even be handled?
                    loc.block.blockyFurniture?.removeAssosiatedSeats()

                // Get the BlockyType of the new block
                val type = when {
                    blockData.material == Material.NOTE_BLOCK -> BlockType.NOTEBLOCK
                    blockData.material == Material.TRIPWIRE -> BlockType.TRIPWIRE
                    blockData.material == Material.CAVE_VINES -> BlockType.CAVEVINE
                    Tag.LEAVES.isTagged(blockData.material) -> BlockType.LEAF
                    // The new block isn't a blockyBlock so just return
                    else -> return extent.setBlock(pos.x, pos.y, pos.z, block)
                }

                val gearyEntity =
                    BlockyBlockQuery.firstOrNull { it.type.blockId == blockMap[blockData] && it.type.blockType == type }
                        ?.prefabKey?.toEntityOrNull() ?: return extent.setBlock(pos.x, pos.y, pos.z, block)

                // TODO Add more checks here as noteworthy stuff is added
                if (gearyEntity.has<BlockyLight>())
                    handleLight.createBlockLight(loc, gearyEntity.get<BlockyLight>()?.lightLevel!!)
                if (gearyEntity.has<BlockySeat>()) // This is probably never called until we go insane and support furniture in WorldEdit
                    spawnFurnitureSeat(loc, ((actor as? Player)?.location?.yaw?.minus(180) ?: 0f), gearyEntity.get<BlockySeat>()?.heightOffset ?: 0.0)

                return extent.setBlock(pos.blockX, pos.blockY, pos.blockZ, block)
            }
        }
    }

    @EventHandler //TODO This will add infinite tabcompletions probably a better way
    fun AsyncTabCompleteEvent.onTabcomplete() {
        if (!buffer.startsWith("//") || !isCommand) return
        val arg = buffer.substringAfterLast(" ").lowercase()

        completions.addAll(BlockyBlockQuery.filter {
            it.prefabKey.key.startsWith(arg) || it.prefabKey.full.startsWith(arg)
        }.map { it.prefabKey.toString() })
    }
}
