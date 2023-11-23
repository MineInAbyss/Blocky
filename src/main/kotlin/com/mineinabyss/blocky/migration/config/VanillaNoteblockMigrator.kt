package com.mineinabyss.blocky.migration.config

import com.fastasyncworldedit.core.util.TaskManager
import com.mineinabyss.blocky.components.core.VanillaNoteBlock
import com.mineinabyss.blocky.helpers.persistentDataContainer
import com.mineinabyss.geary.papermc.datastore.encode
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.function.mask.BlockTypeMask
import com.sk89q.worldedit.function.pattern.Pattern
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.world.World
import com.sk89q.worldedit.world.block.BaseBlock
import com.sk89q.worldedit.world.block.BlockTypes
import org.bukkit.Chunk


class VanillaNoteblockMigrator : ChunkMigrator {
    override fun migrate(chunk: Chunk) {
        // start worldedit session
        chunk.chunkSnapshot

        // Create a WorldEdit EditSession

        // Create a WorldEdit EditSession
        val weWorld: World = BukkitAdapter.adapt(chunk.world)
        val editSession = WorldEdit.getInstance().newEditSession(weWorld)

        TaskManager.taskManager().taskNowAsync {
            val noteblockPitches: MutableList<Pair<BlockVector3, Int>> = mutableListOf()
            editSession?.use {
                val chunkRegion = CuboidRegion(
                    weWorld,
                    BlockVector3.at(chunk.x * 16, chunk.world.minHeight, chunk.z * 16),
                    BlockVector3.at(chunk.x * 16 + 15, chunk.world.maxHeight, chunk.z * 16 + 15)
                )

                val noteblock = BlockTypes.NOTE_BLOCK!!
                val note = noteblock.getProperty<Int>("note")

                it.replaceBlocks(chunkRegion, BlockTypeMask(it, BlockTypes.NOTE_BLOCK)) { position ->
                    val pitch = position.getBlock(it).getState(note)
                    noteblockPitches += position to pitch
                    noteblock.applyBlock(position)
                }

                TaskManager.taskManager().taskNowMain {
                    noteblockPitches.forEach { (block, pitch) ->
                        chunk.world.getBlockAt(block.x, block.y, block.z)
                            .persistentDataContainer
                            .encode(VanillaNoteBlock(pitch))
                    }
                }
            }
        }
    }
}
