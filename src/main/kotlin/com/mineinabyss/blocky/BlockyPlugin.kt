package com.mineinabyss.blocky

import com.mineinabyss.blocky.helpers.createBlockMap
import com.mineinabyss.blocky.listeners.*
import com.mineinabyss.geary.addon.autoscan
import com.mineinabyss.geary.papermc.dsl.gearyAddon
import com.mineinabyss.idofront.platforms.IdofrontPlatforms
import com.mineinabyss.idofront.plugin.getService
import com.mineinabyss.idofront.plugin.registerEvents
import org.bukkit.block.data.BlockData
import org.bukkit.plugin.java.JavaPlugin

val blockyPlugin: BlockyPlugin by lazy { JavaPlugin.getPlugin(BlockyPlugin::class.java) }
var blockMap: Map<BlockData, Int> = emptyMap()

interface BlockyContext {
    companion object : BlockyContext by getService()

    val isFAWELoaded: Boolean
        get() = blockyPlugin.server.pluginManager.isPluginEnabled("FastAsyncWorldEdit")

}

class BlockyPlugin : JavaPlugin() {

    override fun onLoad() {
        IdofrontPlatforms.load(this, "mineinabyss")
    }

    override fun onEnable() {
        saveDefaultConfig()
        reloadConfig()
        BlockyConfig.load()

        // Generates a filled blockMap
        blockMap = createBlockMap()

        BlockyCommandExecutor()

        registerEvents(
            PlayerListener(),
            BlockyGenericListener(),
            BlockyNoteBlockListener(),
            BlockyTripwireListener(),
            BlockyChorusPlantListener(),
            BlockyItemFrameListener(),
            WorldEditListener()
        )

        gearyAddon {
            autoscan("com.mineinabyss") {
                all()
            }
        }

    }
}