package com.mineinabyss.blocky

import com.jeff_media.customblockdata.CustomBlockData
import com.mineinabyss.blocky.compatibility.WorldEditListener
import com.mineinabyss.blocky.compatibility.WorldEditSupport
import com.mineinabyss.blocky.helpers.BLOCKY_SLABS
import com.mineinabyss.blocky.helpers.BLOCKY_STAIRS
import com.mineinabyss.blocky.listeners.*
import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.platforms.Platforms
import com.mineinabyss.idofront.plugin.listeners
import com.sk89q.worldedit.WorldEdit
import it.unimi.dsi.fastutil.ints.IntArrayList
import net.minecraft.resources.ResourceLocation
import org.bukkit.Bukkit
import org.bukkit.Instrument
import org.bukkit.Material
import org.bukkit.Note
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.*
import org.bukkit.plugin.java.JavaPlugin

var blockMap = mapOf<BlockData, Int>()
var registryTagMap = mapOf<ResourceLocation, IntArrayList>()

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
            //BlockyNMSListener(),
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
            }
        }
    }

    fun runStartupFunctions() {
        blockMap = createBlockMap()
        //registryTagMap = createTagRegistryMap()
        ResourcepackGeneration().generateDefaultAssets()
        MoreCreativeTabsGeneration().generateModAssets()
    }

    /*private fun createTagRegistryMap(): Map<ResourceLocation, IntArrayList> {
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
    }*/

    private fun createBlockMap(): Map<BlockData, Int> {
        val blockMap = mutableMapOf<BlockData, Int>()

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

            blockMap.putIfAbsent(tripWireData, i)
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

                blockMap.putIfAbsent(noteBlockData, j - 49)
            }
            if (!blocky.config.noteBlocks.restoreFunctionality) {
                for (j in 1..49) {
                    val noteBlockData = Material.NOTE_BLOCK.createBlockData() as NoteBlock
                    noteBlockData.instrument = Instrument.PIANO
                    noteBlockData.note = Note((j % 25))
                    noteBlockData.isPowered = j / 25 % 2 == 1

                    blockMap.putIfAbsent(noteBlockData, j + 750)
                }
            }
        }

        // Calculates cave-vine states
        if (blocky.config.caveVineBlocks.isEnabled) {
            for (m in 1..50) {
                val vineData = Material.CAVE_VINES.createBlockData() as CaveVines
                vineData.isBerries = m > 25
                vineData.age = if (m > 25) m - 25 else m
                blockMap.putIfAbsent(vineData, m)
            }
        }

        //Calculates slab states & stair states
        for (n in 1..4) {
            blockMap.putIfAbsent(BLOCKY_SLABS.elementAt(n - 1).createBlockData() as Slab, n)
            blockMap.putIfAbsent(BLOCKY_STAIRS.elementAt(n - 1).createBlockData() as Stairs, n)
        }
        return blockMap
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
