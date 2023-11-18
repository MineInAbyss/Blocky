package com.mineinabyss.blocky.migration.config

import org.bukkit.Chunk

interface BlockyMigration {
    fun migrate(chunk: Chunk)
}
