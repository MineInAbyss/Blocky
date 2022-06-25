package com.mineinabyss.blocky

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
        saveDefaultConfig()
        reloadConfig()
        BlockyConfig.load()

        BlockyCommandExecutor()

        registerEvents(
            BlockyGenericListener(),
            BlockySoundListener(),
            BlockyItemFrameListener(),
            BlockyMiddleClickListener(),
            BlockyNMSListener(),
            WorldEditListener()
        )

        if (leafConfig.isEnabled) registerEvents(BlockyLeafListener())
        if (noteConfig.isEnabled) registerEvents(BlockyNoteBlockListener())
        if (chorusConfig.isEnabled) registerEvents(BlockyChorusPlantListener())
        if (tripwireConfig.isEnabled) registerEvents(BlockyTripwireListener())

        gearyAddon {
            autoscan("com.mineinabyss") {
                all()
            }
        }

        blockMap = createBlockMap()
        registryTagMap = createTagRegistryMap()
    }
}
