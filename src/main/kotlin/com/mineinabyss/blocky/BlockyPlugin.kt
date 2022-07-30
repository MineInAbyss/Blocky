package com.mineinabyss.blocky

import com.jeff_media.customblockdata.CustomBlockData
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.blocky.listeners.*
import com.mineinabyss.geary.addon.autoscan
import com.mineinabyss.geary.papermc.dsl.gearyAddon
import com.mineinabyss.idofront.platforms.IdofrontPlatforms
import com.mineinabyss.idofront.plugin.getService
import com.mineinabyss.idofront.plugin.registerEvents
import it.unimi.dsi.fastutil.ints.IntArrayList
import net.minecraft.resources.ResourceLocation
import org.bukkit.block.data.BlockData
import org.bukkit.plugin.java.JavaPlugin

val blockyPlugin: BlockyPlugin by lazy { JavaPlugin.getPlugin(BlockyPlugin::class.java) }
var blockMap = mapOf<BlockData, Int>()
var registryTagMap = mapOf<ResourceLocation, IntArrayList>()

interface BlockyContext {
    companion object : BlockyContext by getService()

}

class BlockyPlugin : JavaPlugin() {

    override fun onLoad() {
        IdofrontPlatforms.load(this, "mineinabyss")
    }

    override fun onEnable() {
        generateDefaultAssets()
        saveDefaultConfig()
        reloadConfig()
        BlockyConfig.load()

        BlockyCommandExecutor()
        CustomBlockData.registerListener(blockyPlugin)

        registerEvents(
            BlockyGenericListener(),
            BlockyItemFrameListener(),
            BlockyMiddleClickListener(),
            BlockyNMSListener(),
            WorldEditListener()
        )

        //TODO Currently relies on Mobzy, perhaps copy the spawning stuff into blocky
        if (server.pluginManager.isPluginEnabled("Mobzy")) registerEvents(BlockyModelEngineListener())
        if (leafConfig.isEnabled) registerEvents(BlockyLeafListener())
        if (noteConfig.isEnabled) registerEvents(BlockyNoteBlockListener())
        if (chorusConfig.isEnabled) registerEvents(BlockyChorusPlantListener())
        if (tripwireConfig.isEnabled) registerEvents(BlockyTripwireListener())
        if (caveVineConfig.isEnabled) registerEvents(BlockyCaveVineListener())
        if (!BlockyConfig.data.disableCustomSounds) registerEvents(BlockySoundListener())

        gearyAddon {
            autoscan("com.mineinabyss") {
                all()
            }
        }

        blockMap = createBlockMap()
        registryTagMap = createTagRegistryMap()
    }
}

private fun generateDefaultAssets() {
    blockyPlugin.saveResource("assets/minecraft/blockstates/note_block.json", true)
    blockyPlugin.saveResource("assets/space/blockstates/tripwire.json", true)
    blockyPlugin.saveResource("assets/space/blockstates/chorus_plant.json", true)
    blockyPlugin.saveResource("assets/minecraft/sounds.json", true)
}
