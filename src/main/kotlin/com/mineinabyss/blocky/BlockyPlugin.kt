package com.mineinabyss.blocky

import com.jeff_media.customblockdata.CustomBlockData
import com.mineinabyss.blocky.compatibility.WorldEditListener
import com.mineinabyss.blocky.compatibility.WorldEditSupport
import com.mineinabyss.blocky.helpers.BLOCKY_SLABS
import com.mineinabyss.blocky.helpers.BLOCKY_STAIRS
import com.mineinabyss.blocky.listeners.*
import com.mineinabyss.geary.addon.GearyLoadPhase
import com.mineinabyss.geary.addon.autoscan
import com.mineinabyss.geary.papermc.dsl.gearyAddon
import com.mineinabyss.idofront.config.IdofrontConfig
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.platforms.Platforms
import com.mineinabyss.idofront.plugin.Plugins
import com.mineinabyss.idofront.plugin.Services
import com.mineinabyss.idofront.plugin.listeners
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import it.unimi.dsi.fastutil.ints.IntArrayList
import net.minecraft.core.Registry
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

val blockyPlugin: BlockyPlugin by lazy { JavaPlugin.getPlugin(BlockyPlugin::class.java) }
var blockMap = mapOf<BlockData, Int>()
var registryTagMap = mapOf<ResourceLocation, IntArrayList>()

interface BlockyContext {
    companion object : BlockyContext by Services.get()
}

class BlockyPlugin : JavaPlugin() {
    lateinit var config: IdofrontConfig<BlockyConfig>
    override fun onLoad() {
        Platforms.load(this, "mineinabyss")
    }

    override fun onEnable() {
        config = config("config") { fromPluginPath(loadDefault = true) }

        BlockyCommandExecutor()
        CustomBlockData.registerListener(blockyPlugin)

        if (Plugins.isEnabled<WorldEditPlugin>()) {
            WorldEdit.getInstance().blockFactory.register(WorldEditSupport.BlockyInputParser())
            listeners(WorldEditListener())
        }

        listeners(
            BlockyGenericListener(),
            BlockyFurnitureListener(),
            BlockyMiddleClickListener(),
            BlockyNMSListener(),
        )

        blockyConfig.run {
            // Until reworked deprecate leaf blocks
            //if (leafBlocks.isEnabled) listeners(BlockyLeafListener())
            if (noteBlocks.isEnabled) listeners(BlockyNoteBlockListener())
            if (tripWires.isEnabled) listeners(BlockyWireListener())
            if (caveVineBlocks.isEnabled) listeners(BlockyCaveVineListener())
            if (slabBlocks.isEnabled) listeners(BlockyCopperListener.BlockySlabListener())
            if (stairBlocks.isEnabled) listeners(BlockyCopperListener.BlockyStairListener())
            if (!disableCustomSounds) listeners(BlockySoundListener())
        }

        gearyAddon {
            autoscan("com.mineinabyss") {
                all()
            }
            startup {
                GearyLoadPhase.ENABLE {
                    runStartupFunctions()
                }
            }
        }
    }

    fun runStartupFunctions() {
        blockMap = createBlockMap()
        registryTagMap = createTagRegistryMap()
        ResourcepackGeneration().generateDefaultAssets()
    }

    private fun createTagRegistryMap(): Map<ResourceLocation, IntArrayList> {
        val map = Registry.BLOCK.tags.map { pair ->
            pair.first.location to IntArrayList(pair.second.size()).apply {
                // If the tag is MINEABLE_WITH_AXE, don't add noteblock
                if (pair.first.location == BlockTags.MINEABLE_WITH_AXE.location) {
                    pair.second.filter {
                        Item.BY_BLOCK[it.value()].toString() != "note_block"
                    }.forEach { add(Registry.BLOCK.getId(it.value())) }
                } else pair.second.forEach { add(Registry.BLOCK.getId(it.value())) }
            }
        }.toList().toMap()

        return map
    }

    private fun createBlockMap(): Map<BlockData, Int> {
        val blockMap = mutableMapOf<BlockData, Int>()

        // Calculates tripwire states
        if (blockyConfig.tripWires.isEnabled) for (i in 0..127) {
            val tripWireData = Bukkit.createBlockData(Material.TRIPWIRE) as Tripwire
            if (i and 1 == 1) tripWireData.setFace(BlockFace.NORTH, true)
            if (i shr 1 and 1 == 1) tripWireData.setFace(BlockFace.EAST, true)
            if (i shr 2 and 1 == 1) tripWireData.setFace(BlockFace.SOUTH, true)
            if (i shr 3 and 1 == 1) tripWireData.setFace(BlockFace.WEST, true)
            if (i shr 4 and 1 == 1) tripWireData.isPowered = true
            if (i shr 5 and 1 == 1) tripWireData.isDisarmed = true
            if (i shr 6 and 1 == 1) tripWireData.isAttached = true

            blockMap.putIfAbsent(tripWireData, i)
        }

        // Calculates noteblock states
        // We do 25-825 to skip PIANO at first
        if (blockyConfig.noteBlocks.isEnabled) {
            for (j in 51..799) {
                //val id = if (blockyConfig.noteBlocks.restoreNormalFunctionality && j <= 50) j + 799 else j
                val noteBlockData = Bukkit.createBlockData(Material.NOTE_BLOCK) as NoteBlock
                noteBlockData.instrument = Instrument.getByType((j / 50 % 400).toByte()) ?: continue

                noteBlockData.note = Note((j % 25))
                noteBlockData.isPowered = j / 25 % 2 == 1

                blockMap.putIfAbsent(noteBlockData, j - 50)
            }
            if (!blockyConfig.noteBlocks.restoreFunctionality) {
                for (j in 1..50) {
                    val noteBlockData = Bukkit.createBlockData(Material.NOTE_BLOCK) as NoteBlock
                    noteBlockData.instrument = Instrument.PIANO
                    noteBlockData.note = Note((j % 25))
                    noteBlockData.isPowered = j / 25 % 2 == 1

                    blockMap.putIfAbsent(noteBlockData, j + 750)
                }
            }
        }

        // Calculates leaf states
        // Should waterlog be used aswell?
        /*if (blockyConfig.leafBlocks.isEnabled) for (l in 1..63) {
            val leafData = Bukkit.createBlockData(getLeafMaterial(l)) as Leaves
            val distance = getLeafDistance(l)
            if (distance == 1 && blockyConfig.leafBlocks.shouldReserveOnePersistentLeafPerType) continue // Skip if one leaf is reserved

            leafData.isPersistent = true
            leafData.distance = distance
            // Due to map using material before distance the Int is scued by 1 if set to reserve 1 state
            blockMap.putIfAbsent(leafData, getBlockMapEntryForLeaf(l))
        }*/

        // Calculates cave-vine states
        if (blockyConfig.caveVineBlocks.isEnabled) {
            for (m in 1..50) {
                val vineData = Bukkit.createBlockData(Material.CAVE_VINES) as CaveVines
                vineData.isBerries = m > 25
                vineData.age = if (m > 25) m - 25 else m
                blockMap.putIfAbsent(vineData, m)
            }
        }

        //Calculates slab states & stair states
        for (n in 1..4) {
            blockMap.putIfAbsent(Bukkit.createBlockData(BLOCKY_SLABS.elementAt(n - 1)) as Slab, n)
            blockMap.putIfAbsent(Bukkit.createBlockData(BLOCKY_STAIRS.elementAt(n - 1)) as Stairs, n)
        }
        return blockMap
    }
}
