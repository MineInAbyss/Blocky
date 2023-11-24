package com.mineinabyss.blocky.systems.migration

import org.bukkit.Chunk

interface ChunkMigrator {
    fun migrate(chunk: Chunk)
}
