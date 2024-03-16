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
import com.mineinabyss.geary.systems.builders.cachedQuery
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
var registryTagMap = mapOf<ResourceLocation, IntArrayList>()

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

            createFurnitureOutlineSystem()
        }
        FurniturePacketHelpers.registerPacketListeners()

        listeners(
            BlockyGenericListener(),
            BlockyFurnitureListener(),
            BlockyMiddleClickListener(),
            BlockyNMSListener(),
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

        geary {
            on(GearyPhase.ENABLE) {
                runStartupFunctions()
            }
        }
    }

    fun runStartupFunctions() {
        registryTagMap = createTagRegistryMap()
        ResourcepackGeneration().generateDefaultAssets()
    }

    private fun createTagRegistryMap(): Map<ResourceLocation, IntArrayList> {

        return BuiltInRegistries.BLOCK.tags.map { pair ->
            pair.first.location to IntArrayList(pair.second.size()).apply {
                // If the tag is MINEABLE_WITH_AXE, don't add noteblock, if it's MINEABLE_WITH_PICKAXE, don't add petrified oak slab
                pair.second.filter {
                    it.value().descriptionId != when (pair.first.location) {
                        BlockTags.MINEABLE_WITH_AXE.location -> "block.minecraft.note_block"
                        BlockTags.MINEABLE_WITH_PICKAXE.location -> "block.minecraft.petrified_oak_slab"
                        else -> it.value().descriptionId
                    }
                }.forEach { add(BuiltInRegistries.BLOCK.getId(it.value())) }
            }
        }.toList().toMap()
    }


    fun createBlockyContext() {
        DI.remove<BlockyContext>()
        val blockyContext = object : BlockyContext {
            override val plugin = this@BlockyPlugin
            override val logger by plugin.observeLogger()
            override val config: BlockyConfig by config("config", dataFolder.toPath(), BlockyConfig())
            override val prefabQuery = geary.cachedQuery(BlockyQuery())
            override val blockQuery = geary.cachedQuery(BlockyBlockQuery())
            override val furnitureQuery = geary.cachedQuery(BlockyFurnitureQuery())
        }
        DI.add<BlockyContext>(blockyContext)
    }
}
