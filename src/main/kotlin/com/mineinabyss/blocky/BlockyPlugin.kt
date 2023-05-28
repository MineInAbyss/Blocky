package com.mineinabyss.blocky

import com.jeff_media.customblockdata.CustomBlockData
import com.mineinabyss.blocky.api.BlockyBlocks.isBlockyBlock
import com.mineinabyss.blocky.compatibility.breaker.BlockyBlockProvider
import com.mineinabyss.blocky.compatibility.worldedit.WorldEditListener
import com.mineinabyss.blocky.compatibility.worldedit.WorldEditSupport
import com.mineinabyss.blocky.components.core.BlockyBlock
import com.mineinabyss.blocky.components.features.BlockyDirectional
import com.mineinabyss.blocky.helpers.BLOCKY_SLABS
import com.mineinabyss.blocky.helpers.BLOCKY_STAIRS
import com.mineinabyss.blocky.listeners.*
import com.mineinabyss.blocky.systems.BlockyBlockQuery
import com.mineinabyss.blocky.systems.BlockyBlockQuery.prefabKey
import com.mineinabyss.blocky.systems.BlockyBlockQuery.type
import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.idofront.platforms.Platforms
import com.mineinabyss.idofront.plugin.listeners
import com.sk89q.worldedit.WorldEdit
import eu.asangarin.breaker.Breaker
import it.unimi.dsi.fastutil.ints.IntArrayList
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.world.item.Item
import org.bukkit.Bukkit
import org.bukkit.Instrument
import org.bukkit.Material
import org.bukkit.Note
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.*
import org.bukkit.plugin.java.JavaPlugin

var blockMap = mapOf<BlockData, Int>()
var prefabMap = mapOf<BlockData, PrefabKey>()
var registryTagMap = mapOf<ResourceLocation, IntArrayList>()
val breaker by lazy { Bukkit.getPluginManager().getPlugin("Breaker") as Breaker }
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

        //server.pluginManager.registerEvents(test(), this)
        geary {
            autoscan(classLoader, "com.mineinabyss.blocky") {
                all()
            }
            on(GearyPhase.ENABLE) {
                runStartupFunctions()
                if (Bukkit.getPluginManager().isPluginEnabled("Breaker")) {
                    breaker.blockProviders.register(BlockyBlockProvider)
                }
            }
        }
    }

    fun runStartupFunctions() {
        blockMap = createBlockMap()
        prefabMap = createPrefabMap()
        registryTagMap = createTagRegistryMap()
        ResourcepackGeneration().generateDefaultAssets()
        MoreCreativeTabsGeneration().generateModAssets()
    }

    private fun createPrefabMap(): Map<BlockData, PrefabKey> {

        return mutableMapOf<BlockData, PrefabKey>().run { ->
            BlockyBlockQuery.filter { it.prefabKey.isBlockyBlock }.forEach { scope ->
                scope.prefabKey.toEntityOrNull()?.let { entity ->
                    entity.get<BlockyBlock>()?.let { blockyBlock ->
                        broadcast(blockyBlock.blockType)
                        val blockData = blockMap.entries.filter {
                            when (blockyBlock.blockType) {
                                BlockyBlock.BlockType.NOTEBLOCK -> it.key is NoteBlock
                                BlockyBlock.BlockType.WIRE -> it.key is Tripwire
                                BlockyBlock.BlockType.CAVEVINE -> it.key is CaveVinesPlant
                                BlockyBlock.BlockType.SLAB -> it.key is Slab
                                BlockyBlock.BlockType.STAIR -> it.key is Stairs
                                // TODO This apparently is needed otherwise "WhenExpression is not exhaustive"
                                else -> return@forEach
                            }
                        }.firstOrNull { it.value == blockyBlock.blockId }?.key ?: return@forEach

                        val prefabKey = BlockyBlockQuery.filter { it.type.blockType == blockyBlock.blockType }
                            .firstOrNull { queryScope ->
                                when {
                                    queryScope.entity.has<BlockyDirectional>() -> {
                                        queryScope.entity.get<BlockyDirectional>().let { directional ->
                                            ((directional?.yBlock?.toEntityOrNull() ?: queryScope.entity)
                                                .get<BlockyBlock>()?.blockId == blockMap[blockData] ||
                                                    (directional?.xBlock?.toEntityOrNull() ?: queryScope.entity)
                                                        .get<BlockyBlock>()?.blockId == blockMap[blockData] ||
                                                    (directional?.zBlock?.toEntityOrNull() ?: queryScope.entity)
                                                        .get<BlockyBlock>()?.blockId == blockMap[blockData])
                                                    && queryScope.type.blockType == blockyBlock.blockType
                                        }
                                    }

                                    queryScope.type.blockType == BlockyBlock.BlockType.SLAB ->
                                        BLOCKY_SLABS.elementAt(blockyBlock.blockId - 1) == blockData.material

                                    queryScope.type.blockType == BlockyBlock.BlockType.STAIR ->
                                        BLOCKY_STAIRS.elementAt(queryScope.type.blockId - 1) == blockData.material

                                    else -> queryScope.type.blockId == blockMap[blockData] && queryScope.type.blockType == blockyBlock.blockType
                                }
                            }?.prefabKey

                        put(blockData, prefabKey ?: return@forEach)
                    }
                }
            }
            this
        }
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

    private fun createBlockMap(): Map<BlockData, Int> {
        return mutableMapOf<BlockData, Int>().apply {
            // Calculates tripwire states
            if (blocky.config.tripWires.isEnabled) for (i in 0..127) {
                val tripWireData = Material.TRIPWIRE.createBlockData() as Tripwire
                if (i and 1 == 1) tripWireData.setFace(BlockFace.NORTH, true)
                if (i shr 1 and 1 == 1) tripWireData.setFace(BlockFace.EAST, true)
                if (i shr 2 and 1 == 1) tripWireData.setFace(BlockFace.SOUTH, true)
                if (i shr 3 and 1 == 1) tripWireData.setFace(BlockFace.WEST, true)
                if (i shr 4 and 1 == 1) tripWireData.isPowered = true
                if (i shr 5 and 1 == 1) tripWireData.isDisarmed = true
                if (i shr 6 and 1 == 1) tripWireData.isAttached = true

                putIfAbsent(tripWireData, i)
            }

            // Calculates noteblock states
            // We do 25-825 to skip PIANO at first
            if (blocky.config.noteBlocks.isEnabled) {
                for (j in 50..799) {
                    //val id = if (blocky.config.noteBlocks.restoreNormalFunctionality && j <= 50) j + 799 else j
                    val noteBlockData = Material.NOTE_BLOCK.createBlockData() as NoteBlock
                    noteBlockData.instrument = Instrument.getByType((j / 50 % 400).toByte()) ?: continue

                    noteBlockData.note = Note((j % 25))
                    noteBlockData.isPowered = j / 25 % 2 == 1

                    putIfAbsent(noteBlockData, j - 49)
                }
                if (!blocky.config.noteBlocks.restoreFunctionality) {
                    for (j in 1..49) {
                        val noteBlockData = Material.NOTE_BLOCK.createBlockData() as NoteBlock
                        noteBlockData.instrument = Instrument.PIANO
                        noteBlockData.note = Note((j % 25))
                        noteBlockData.isPowered = j / 25 % 2 == 1

                        putIfAbsent(noteBlockData, j + 750)
                    }
                }
            }

            // Calculates cave-vine states
            if (blocky.config.caveVineBlocks.isEnabled) {
                for (m in 1..50) {
                    val vineData = Material.CAVE_VINES.createBlockData() as CaveVines
                    vineData.isBerries = m > 25
                    vineData.age = if (m > 25) m - 25 else m
                    putIfAbsent(vineData, m)
                }
            }

            //Calculates slab states & stair states
            for (n in 1..4) {
                putIfAbsent(BLOCKY_SLABS.elementAt(n - 1).createBlockData() as Slab, n)
                putIfAbsent(BLOCKY_STAIRS.elementAt(n - 1).createBlockData() as Stairs, n)
            }
        }
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
