package com.mineinabyss.blocky.migration.config

import org.bukkit.Chunk

interface ChunkMigrator {
    fun migrate(chunk: Chunk)
}
