package com.mineinabyss.blocky

import com.mineinabyss.blocky.assets_generation.MoreCreativeTabsGeneration
import com.mineinabyss.blocky.assets_generation.ResourcepackGeneration
import com.mineinabyss.blocky.compatibility.worldedit.WorldEditListener
import com.mineinabyss.blocky.compatibility.worldedit.WorldEditSupport
import com.mineinabyss.blocky.helpers.FurniturePacketHelpers
import com.mineinabyss.blocky.listeners.*
import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.messaging.logError
import com.mineinabyss.idofront.plugin.listeners
import com.sk89q.worldedit.WorldEdit
import io.papermc.paper.configuration.GlobalConfiguration
import it.unimi.dsi.fastutil.ints.IntArrayList
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.world.item.Item
import org.bukkit.Bukkit
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

        if (Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
            WorldEdit.getInstance().blockFactory.register(WorldEditSupport.BlockyInputParser())
            listeners(WorldEditListener())
        }

        BlockyCommandExecutor()

        listeners(
            BlockyGenericListener(),
            BlockyFurnitureListener(),
            BlockyMiddleClickListener(),
            BlockyNMSListener(),
        )

        blocky.config.run {
            if (noteBlocks.isEnabled) {
                listeners(BlockyNoteBlockListener())
                if (!GlobalConfiguration.get().blockUpdates.disableNoteblockUpdates) {
                    listeners(BlockyNoteBlockListener.BlockyNoteBlockPhysicsListener())
                    logError("It is heavily recommended to toggle the disable-noteblock-updates setting in paper-global.yml.")
                    logError("Otherwise Blocky will listen to some events that fire alot and might degrade server performance.")
                }
            }
            if (tripWires.isEnabled) {
                listeners(BlockyWireListener())
                if (!GlobalConfiguration.get().blockUpdates.disableTripwireUpdates) {
                    listeners(BlockyWireListener.BlockyWirePhysicsListener())
                    logError("It is heavily recommended to toggle the disable-tripwire-updates setting in paper-global.yml.")
                    logError("Otherwise Blocky will listen to some events that fire alot and might degrade server performance.")
                }
            }
            if (caveVineBlocks.isEnabled) listeners(BlockyCaveVineListener())
            if (slabBlocks.isEnabled) listeners(BlockyCopperListener.BlockySlabListener())
            if (stairBlocks.isEnabled) listeners(BlockyCopperListener.BlockyStairListener())
            if (!disableCustomSounds) listeners(BlockySoundListener())
        }

        geary{
            on(GearyPhase.ENABLE) {
                runStartupFunctions()
            }
        }

    }

    fun runStartupFunctions() {
        registryTagMap = createTagRegistryMap()
        ResourcepackGeneration().generateDefaultAssets()
        MoreCreativeTabsGeneration().generateModAssets()
        FurniturePacketHelpers.registerPacketListeners()
    }

    private fun createTagRegistryMap(): Map<ResourceLocation, IntArrayList> {

        return BuiltInRegistries.BLOCK.tags.map { pair ->
            pair.first.location to IntArrayList(pair.second.size()).apply {
                // If the tag is MINEABLE_WITH_AXE, don't add noteblock, if it's MINEABLE_WITH_PICKAXE, don't add petrified oak slab
                when (pair.first.location) {
                    BlockTags.MINEABLE_WITH_AXE.location -> {
                        pair.second.filter {
                            Item.BY_BLOCK[it.value()].toString() != "note_block"
                        }.forEach { add(BuiltInRegistries.BLOCK.getId(it.value())) }
                    }
                    BlockTags.MINEABLE_WITH_PICKAXE.location -> {
                        pair.second.filter {
                            Item.BY_BLOCK[it.value()].toString() != "petrified_oak_slab"
                        }.forEach { add(BuiltInRegistries.BLOCK.getId(it.value())) }
                    }
                    else -> pair.second.forEach { add(BuiltInRegistries.BLOCK.getId(it.value())) }
                }
            }
        }.toList().toMap()
    }


    fun createBlockyContext() {
        DI.remove<BlockyContext>()
        val blockyContext = object : BlockyContext {
            override val plugin = this@BlockyPlugin
            override val config: BlockyConfig by config("config", dataFolder.toPath(), BlockyConfig())
        }
        DI.add<BlockyContext>(blockyContext)
    }
}
