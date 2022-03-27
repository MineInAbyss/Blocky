package com.mineinabyss.blocky

import com.mineinabyss.blocky.listeners.*
import com.mineinabyss.blocky.systems.BlockBreakingSystem
import com.mineinabyss.geary.papermc.dsl.gearyAddon
import com.mineinabyss.idofront.platforms.IdofrontPlatforms
import com.mineinabyss.idofront.plugin.getService
import com.mineinabyss.idofront.plugin.registerEvents
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

val blockyPlugin: BlockyPlugin by lazy { JavaPlugin.getPlugin(BlockyPlugin::class.java) }

interface BlockyContext {
    companion object : BlockyContext by getService()

    val isFAWELoaded: Boolean
        get() = blockyPlugin.server.pluginManager.isPluginEnabled("FastAsyncWorldEdit")

}

class BlockyPlugin : JavaPlugin() {
    val blocksDir = File(dataFolder, "blocks")

    override fun onLoad() {
        IdofrontPlatforms.load(this, "mineinabyss")
    }

    override fun onEnable() {
        saveDefaultConfig()
        reloadConfig()
        BlockyConfig.load()

        BlockyCommandExecutor()

        registerEvents(
            PlayerListener(),
            BlockyNoteBlockListener(),
            BlockyTripwireListener(),
            //BlockyEntityListener(),
            WorldEditListener(),
            BlockyItemFrameListener()
        )
        BlockBreakingSystem().registerListener()

        gearyAddon {
            autoScanComponents()
            loadPrefabs(blocksDir)
        }

    }
}