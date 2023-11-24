package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.systems.migration.Migrations
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent

class BlockyChunkMigrationListener(
    private val migrations: Migrations
): Listener {
    @EventHandler
    fun ChunkLoadEvent.onChunkLoad() {
        migrations.migrations.forEach {
            it.applyMissingMigrations(chunk)
        }
    }
}
