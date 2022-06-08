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

        BlockyCommandExecutor()

        registerEvents(
            BlockyGenericListener(),
            BlockyNoteBlockListener(),
            BlockyTripwireListener(),
            BlockyChorusPlantListener(),
            BlockyItemFrameListener(),
            BlockyMiddleClickListener(),
            BlockyDoorListener(),
            WorldEditListener()
        )

        gearyAddon {
            autoscan("com.mineinabyss") {
                all()
            }
        }
        blockMap = createBlockMap()

    }
}
