package com.mineinabyss.blocky

import com.mineinabyss.blocky.assets_generation.ResourcepackGeneration
import com.mineinabyss.blocky.compatibility.worldedit.WorldEditListener
import com.mineinabyss.blocky.compatibility.worldedit.WorldEditSupport
import com.mineinabyss.blocky.helpers.FurniturePacketHelpers
import com.mineinabyss.blocky.listeners.*
import com.mineinabyss.blocky.systems.*
import com.mineinabyss.blocky.systems.actions.*
import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.builders.cache
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.messaging.logError
import com.mineinabyss.idofront.messaging.observeLogger
import com.mineinabyss.idofront.plugin.Plugins
import com.mineinabyss.idofront.plugin.listeners
import com.sk89q.worldedit.WorldEdit
import io.papermc.paper.configuration.GlobalConfiguration
import it.unimi.dsi.fastutil.ints.IntArrayList
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import org.bukkit.block.data.BlockData
import org.bukkit.plugin.java.JavaPlugin

var prefabMap = mapOf<BlockData, PrefabKey>()

class BlockyPlugin : JavaPlugin() {
    override fun onLoad() {
        geary {
            autoscan(classLoader, "com.mineinabyss.blocky") {
                all()
            }
        }
    }

    override fun onEnable() {
        createBlockyContext()
        BlockyDatapacks.generateDatapack()

        if (Plugins.isEnabled("WorldEdit")) {
            WorldEdit.getInstance().blockFactory.register(WorldEditSupport.BlockyInputParser())
            listeners(WorldEditListener())
        }

        BlockyCommandExecutor()

        geary.run {
            createFurnitureSpawner()
            createFurnitureItemSetter()
            createFurnitureSeatSetter()
            createFurnitureMEGModelSetter()

            furnitureHitboxSetter()

            createFurnitureOutlineSystem()
        }

        listeners(
            BlockyGenericListener(),
            BlockyFurnitureListener(),
            BlockyMiddleClickListener(),
        )

        blocky.config.run {
            if (noteBlocks.isEnabled) {
                if (!GlobalConfiguration.get().blockUpdates.disableNoteblockUpdates) {
                    blocky.logger.e("Due to the disable-noteblock-updates being disabled in paper-global.yml, NoteBlock-BlockTypes have been disabled...")
                    blocky.logger.e("This setting is required for Blocky to function properly.")
                } else listeners(BlockyNoteBlockListener())
            }
            if (tripWires.isEnabled) {
                if (!GlobalConfiguration.get().blockUpdates.disableTripwireUpdates) {
                    blocky.logger.e("Due to the disable-tripwire-updates being disabled in paper-global.yml, Wire-BlockTypes have been disabled...")
                    blocky.logger.e("This setting is required for Blocky to function properly.")
                } else listeners(BlockyWireListener())

            }
            if (caveVineBlocks.isEnabled) listeners(BlockyCaveVineListener())
            if (slabBlocks.isEnabled) listeners(BlockyCopperListener.BlockySlabListener())
            if (stairBlocks.isEnabled) listeners(BlockyCopperListener.BlockyStairListener())
            if (!disableCustomSounds) listeners(BlockySoundListener())
        }

        ResourcepackGeneration().generateDefaultAssets()
    }


    fun createBlockyContext() {
        DI.remove<BlockyContext>()
        val blockyContext = object : BlockyContext {
            override val plugin = this@BlockyPlugin
            override val logger by plugin.observeLogger()
            override val config: BlockyConfig by config("config", dataFolder.toPath(), BlockyConfig())
            override val prefabQuery = geary.cache(BlockyQuery())
            override val blockQuery = geary.cache(BlockyBlockQuery())
            override val furnitureQuery = geary.cache(BlockyFurnitureQuery())
        }
        DI.add<BlockyContext>(blockyContext)
    }
}
