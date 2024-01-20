package com.mineinabyss.blocky.compatibility.worldedit

import ca.spottedleaf.dataconverter.converters.datatypes.DataType
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.blocky.prefabMap
import com.mineinabyss.blocky.systems.BlockyBlockQuery
import com.mineinabyss.blocky.systems.BlockyBlockQuery.prefabKey
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.prefabKey
import com.mineinabyss.idofront.nms.nbt.WrappedPDC
import com.sk89q.worldedit.WorldEditException
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.entity.BaseEntity
import com.sk89q.worldedit.entity.Entity
import com.sk89q.worldedit.event.extent.EditSessionEvent
import com.sk89q.worldedit.extent.AbstractDelegateExtent
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.util.eventbus.Subscribe
import com.sk89q.worldedit.world.block.BlockStateHolder
import net.minecraft.nbt.CompoundTag
import org.bukkit.*
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer


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
                loc.block.prefabKey?.toEntityOrNull() ?: return extent.setBlock(pos.x, pos.y, pos.z, block)
                // Get the BlockyType of the new block
                prefabMap[blockData]?.toEntityOrNull() ?: return extent.setBlock(pos.x, pos.y, pos.z, block)

                return extent.setBlock(pos.blockX, pos.blockY, pos.blockZ, block)
            }
        }
    }

    @EventHandler //TODO This will add infinite tabcompletions probably a better way
    fun AsyncTabCompleteEvent.onTabcomplete() {
        if (!buffer.startsWith("//") || !isCommand) return
        val arg = buffer.substringAfterLast(" ").lowercase()

        completions.addAll(BlockyBlockQuery.toList { it }.filter {
            it.prefabKey.key.startsWith(arg) || it.prefabKey.full.startsWith(arg)
        }.map { it.prefabKey.toString() })
    }
}
