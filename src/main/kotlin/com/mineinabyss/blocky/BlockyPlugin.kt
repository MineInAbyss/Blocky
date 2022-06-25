package com.mineinabyss.blocky

import com.mineinabyss.blocky.helpers.createBlockMap
import com.mineinabyss.blocky.helpers.createTagRegistryMap
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

        // Generates a filled blockMap

        BlockyCommandExecutor()

        registerEvents(
            BlockyGenericListener(),
            BlockySoundListener(),
            BlockyNoteBlockListener(),
            BlockyTripwireListener(),
            BlockyChorusPlantListener(),
            BlockyItemFrameListener(),
            BlockyMiddleClickListener(),
            BlockyNMSListener(),
            WorldEditListener()
        )

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
