package com.mineinabyss.blocky.migration.config

import com.mineinabyss.blocky.components.core.AppliedMigrations
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.encode
import org.bukkit.Chunk
import org.bukkit.NamespacedKey

class MigrationGroup(
    val key: NamespacedKey,
    val migrators: List<ChunkMigrator>
) {
    fun applyMissingMigrations(chunk: Chunk) {
        val pdc = chunk.persistentDataContainer
        val migrations = (pdc.decode<AppliedMigrations>() ?: AppliedMigrations())
        val nextMigration: Int = migrations.migrations.getOrDefault(key.toString(), 0)
        migrators.drop(nextMigration).forEach { it.migrate(chunk) }
        pdc.encode(migrations.copy(migrations = migrations.migrations + (key.toString() to migrators.size)))
    }
}
