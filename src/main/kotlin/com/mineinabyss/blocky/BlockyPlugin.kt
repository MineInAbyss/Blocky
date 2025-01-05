package com.mineinabyss.blocky

import com.jeff_media.customblockdata.CustomBlockData
import com.mineinabyss.blocky.assets_generation.ResourcepackGeneration
import com.mineinabyss.blocky.listeners.*
import com.mineinabyss.blocky.systems.*
import com.mineinabyss.blocky.systems.actions.createFurnitureItemSetter
import com.mineinabyss.blocky.systems.actions.createFurnitureMEGModelSetter
import com.mineinabyss.blocky.systems.actions.createFurnitureSeatSetter
import com.mineinabyss.blocky.systems.actions.furnitureHitboxSetter
import com.mineinabyss.geary.papermc.configure
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.messaging.observeLogger
import com.mineinabyss.idofront.plugin.listeners
import io.papermc.paper.configuration.GlobalConfiguration
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.plugin.java.JavaPlugin

class BlockyPlugin : JavaPlugin() {
    override fun onLoad() {
        gearyPaper.configure {
            install(BlockyAddon)
        }
    }

    override fun onEnable() {
        createBlockyContext()

        (Bukkit.getWorlds().first() as CraftWorld).handle.dataStorage

        gearyPaper.worldManager.global.apply {
            createFurnitureOutlineSystem()
            createFurnitureSpawner()
            createFurnitureItemSetter()
            createFurnitureSeatSetter()
            createFurnitureMEGModelSetter()
            furnitureHitboxSetter()
        }

        BlockyBrigadierCommands.registerCommands()

        listeners(
            BlockyGenericListener(),
            BlockyFurnitureListener(),
            BlockyMiddleClickListener(),
            BlockySoundListener(),
            BlockyCopperListener()
        )
        CustomBlockData.registerListener(this)

        blocky.config.run {
            if (noteBlocks.isEnabled) {
                if (!GlobalConfiguration.get().blockUpdates.disableNoteblockUpdates) {
                    blocky.logger.e("Due to the disable-noteblock-updates being disabled in paper-global.yml, NoteBlock-BlockTypes have been disabled...")
                    blocky.logger.e("This setting is required for Blocky to function properly.")
                } else {
                    listeners(BlockyNoteBlockListener())
                    if (noteBlocks.restoreVanillaFunctionality) listeners(VanillaNoteBlockListener())
                }
            }
            if (tripWires.isEnabled) {
                if (!GlobalConfiguration.get().blockUpdates.disableTripwireUpdates) {
                    blocky.logger.e("Due to the disable-tripwire-updates being disabled in paper-global.yml, Wire-BlockTypes have been disabled...")
                    blocky.logger.e("This setting is required for Blocky to function properly.")
                } else listeners(BlockyWireListener())

            }
            if (caveVineBlocks.isEnabled) listeners(BlockyCaveVineListener())
        }

        ResourcepackGeneration(gearyPaper.worldManager.global).generateDefaultAssets()
    }


    fun createBlockyContext() {
        DI.remove<BlockyContext>()
        // TODO update to use per world syntax when geary adds it
        val geary = gearyPaper.worldManager.global
        val blockyContext = object : BlockyContext {
            override val plugin = this@BlockyPlugin
            override val logger by plugin.observeLogger()
            override val config: BlockyConfig by config("config", dataFolder.toPath(), BlockyConfig())
            override val prefabQuery = geary.cache(::BlockyQuery)
            override val blockQuery = geary.cache(::BlockyBlockQuery)
            override val furnitureQuery = geary.cache(::BlockyFurnitureQuery)
        }
        DI.add<BlockyContext>(blockyContext)
    }
}
