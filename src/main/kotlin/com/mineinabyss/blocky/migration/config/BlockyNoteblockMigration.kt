package com.mineinabyss.blocky.migration.config

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


class BlockyNoteblockMigration : BlockyMigration {
    override fun migrate(chunk: Chunk) {
        val pdc = chunk.persistentDataContainer
        // start worldedit session
        chunk.chunkSnapshot

        // Create a WorldEdit EditSession

        // Create a WorldEdit EditSession
        val weWorld: World = BukkitAdapter.adapt(chunk.world)
        val editSession = WorldEdit.getInstance().newEditSession(weWorld)

        editSession.use {
            val chunkRegion = CuboidRegion(
                weWorld,
                BlockVector3.at(chunk.x * 16, chunk.world.minHeight, chunk.z * 16),
                BlockVector3.at(chunk.x * 16 + 15, chunk.world.maxHeight, chunk.z * 16 + 15)
            )

            val noteblock = BlockTypes.NOTE_BLOCK!!
            val note = noteblock.getProperty<Int>("note")

            it.replaceBlocks(
                chunkRegion,
                BlockTypeMask(editSession, BlockTypes.NOTE_BLOCK),
                object : Pattern by noteblock {
                    override fun applyBlock(position: BlockVector3): BaseBlock {
                        val pitch = position.getBlock(editSession).getState(note)
                        println("Block at $position is a noteblock with pitch $pitch")
                        // get noteblock pitch
                        return noteblock.applyBlock(position)
                    }
                }
            )
        }
    }
}
