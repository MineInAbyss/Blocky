package com.mineinabyss.blocky

import com.mineinabyss.blocky.listeners.BlockListener
import com.mineinabyss.blocky.listeners.PlayerListener
import com.mineinabyss.idofront.platforms.IdofrontPlatforms
import com.mineinabyss.idofront.plugin.getService
import com.mineinabyss.idofront.plugin.registerEvents
import org.bukkit.plugin.java.JavaPlugin

val blockyPlugin: BlockyPlugin by lazy { JavaPlugin.getPlugin(BlockyPlugin::class.java) }

interface BlockyContext {
    companion object : BlockyContext by getService()

    //val db: Database
}

class BlockyPlugin : JavaPlugin() {
    override fun onLoad() {
        IdofrontPlatforms.load(this, "mineinabyss")
    }

    override fun onEnable() {
        saveDefaultConfig()
        BlockyConfig.load()
        registerEvents(PlayerListener(), BlockListener())
        /*registerService<BlockyContext>(object : BlockyContext {
            override val db = Database.connect("jdbc:sqlite:" + dataFolder.path + "/data.db", "org.sqlite.JDBC")
        })*/

        /*transaction(BlockyContext.db) {
            addLogger(StdOutSqlLogger)

            SchemaUtils.createMissingTablesAndColumns(Blocks, Interactables)
        }*/

        BlockyCommandExecutor
    }
}