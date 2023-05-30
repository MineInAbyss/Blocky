package com.mineinabyss.blocky.compatibility.worldedit

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent
import com.mineinabyss.blocky.components.features.BlockyLight
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.blocky.prefabMap
import com.mineinabyss.blocky.systems.BlockyBlockQuery
import com.mineinabyss.blocky.systems.BlockyBlockQuery.prefabKey
import com.sk89q.worldedit.WorldEditException
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.event.extent.EditSessionEvent
import com.sk89q.worldedit.extent.AbstractDelegateExtent
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.util.eventbus.Subscribe
import com.sk89q.worldedit.world.block.BlockStateHolder
import org.bukkit.*
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

                // Get the BlockyType of the new block
                val gearyEntity = prefabMap[blockData]?.toEntityOrNull() ?: return extent.setBlock(pos.x, pos.y, pos.z, block)

                // TODO Add more checks here as noteworthy stuff is added
                if (gearyEntity.has<BlockyLight>())
                    handleLight.createBlockLight(loc, gearyEntity.get<BlockyLight>()?.lightLevel!!)

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
