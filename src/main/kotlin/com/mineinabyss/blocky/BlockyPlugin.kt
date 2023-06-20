package com.mineinabyss.blocky

//import com.mineinabyss.blocky.compatibility.breaker.BlockyBlockProvider
//import eu.asangarin.breaker.Breaker
import com.jeff_media.customblockdata.CustomBlockData
import com.mineinabyss.blocky.compatibility.worldedit.WorldEditListener
import com.mineinabyss.blocky.compatibility.worldedit.WorldEditSupport
import com.mineinabyss.blocky.listeners.*
import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.platforms.Platforms
import com.mineinabyss.idofront.plugin.listeners
import com.sk89q.worldedit.WorldEdit
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
//val breaker by lazy { Bukkit.getPluginManager().getPlugin("Breaker") as? Breaker }
class BlockyPlugin : JavaPlugin() {
    override fun onLoad() {
        Platforms.load(this, "mineinabyss")
    }

    override fun onEnable() {

        createBlockyContext()

        CustomBlockData.registerListener(blocky.plugin)

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
            if (noteBlocks.isEnabled) listeners(BlockyNoteBlockListener())
            if (tripWires.isEnabled) listeners(BlockyWireListener())
            if (caveVineBlocks.isEnabled) listeners(BlockyCaveVineListener())
            if (slabBlocks.isEnabled) listeners(BlockyCopperListener.BlockySlabListener())
            if (stairBlocks.isEnabled) listeners(BlockyCopperListener.BlockyStairListener())
            if (!disableCustomSounds) listeners(BlockySoundListener())
        }

        geary {
            autoscan(classLoader, "com.mineinabyss.blocky") {
                all()
            }
            on(GearyPhase.ENABLE) {
                runStartupFunctions()
                /*if (Bukkit.getPluginManager().isPluginEnabled("Breaker")) {
                    breaker?.blockProviders?.register(BlockyBlockProvider)
                }*/
            }
        }
    }

    fun runStartupFunctions() {
        registryTagMap = createTagRegistryMap()
        ResourcepackGeneration().generateDefaultAssets()
        MoreCreativeTabsGeneration().generateModAssets()
    }

    private fun createTagRegistryMap(): Map<ResourceLocation, IntArrayList> {

        return BuiltInRegistries.BLOCK.tags.map { pair ->
            pair.first.location to IntArrayList(pair.second.size()).apply {
                // If the tag is MINEABLE_WITH_AXE, don't add noteblock
                if (pair.first.location == BlockTags.MINEABLE_WITH_AXE.location) {
                    pair.second.filter {
                        Item.BY_BLOCK[it.value()].toString() != "note_block"
                    }.forEach { add(BuiltInRegistries.BLOCK.getId(it.value())) }
                } else pair.second.forEach { add(BuiltInRegistries.BLOCK.getId(it.value())) }
            }
        }.toList().toMap()
    }


    fun createBlockyContext() {
        DI.remove<BlockyContext>()
        val blockyContext = object : BlockyContext {
            override val plugin = this@BlockyPlugin
            override val config: BlockyConfig by config("config") { fromPluginPath(loadDefault = true) }
        }
        DI.add<BlockyContext>(blockyContext)
    }
}
