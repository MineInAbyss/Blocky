package com.mineinabyss.blocky

import com.jeff_media.customblockdata.CustomBlockData
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.blocky.listeners.*
import com.mineinabyss.geary.addon.GearyLoadPhase
import com.mineinabyss.geary.addon.autoscan
import com.mineinabyss.geary.papermc.dsl.gearyAddon
import com.mineinabyss.idofront.platforms.IdofrontPlatforms
import com.mineinabyss.idofront.plugin.getService
import com.mineinabyss.idofront.plugin.registerEvents
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
import org.bukkit.block.data.MultipleFacing
import org.bukkit.block.data.type.CaveVines
import org.bukkit.block.data.type.Leaves
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.block.data.type.Tripwire
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
        CustomBlockData.registerListener(blockyPlugin)

        registerEvents(
            BlockyGenericListener(),
            BlockyItemFrameListener(),
            BlockyMiddleClickListener(),
            BlockyNMSListener(),
            WorldEditListener()
        )

        //TODO Currently relies on Mobzy, perhaps copy the spawning stuff into blocky
        if (server.pluginManager.isPluginEnabled("Mobzy")) registerEvents(BlockyModelEngineListener())
        if (leafConfig.isEnabled) registerEvents(BlockyLeafListener())
        if (noteConfig.isEnabled) registerEvents(BlockyNoteBlockListener())
        if (chorusConfig.isEnabled) registerEvents(BlockyChorusPlantListener())
        if (tripwireConfig.isEnabled) registerEvents(BlockyTripwireListener())
        if (caveVineConfig.isEnabled) registerEvents(BlockyCaveVineListener())
        if (!BlockyConfig.data.disableCustomSounds) registerEvents(BlockySoundListener())

        gearyAddon {
            autoscan("com.mineinabyss") {
                all()
            }
            startup {
                GearyLoadPhase.ENABLE {
                    blockMap = createBlockMap()
                    registryTagMap = createTagRegistryMap()
                    ResourcepackGeneration().generateDefaultAssets()
                }
            }
        }
    }

    private fun createTagRegistryMap(): Map<ResourceLocation, IntArrayList> {
        val map = Registry.BLOCK.tags.map { pair ->
            pair.first.location to IntArrayList(pair.second.size()).apply {
                // If the tag is MINEABLE_WITH_AXE, don't add noteblock and chorus plant
                if (pair.first.location == BlockTags.MINEABLE_WITH_AXE.location) {
                    pair.second.filter {
                        val itemName = Item.BY_BLOCK[it.value()].toString()
                        itemName != "note_block" && itemName != "chorus_plant"
                    }.forEach { add(Registry.BLOCK.getId(it.value())) }
                } else pair.second.forEach { add(Registry.BLOCK.getId(it.value())) }
            }
        }.toList().toMap()

        return map
    }

    private fun createBlockMap(): Map<BlockData, Int> {
        val blockMap = mutableMapOf<BlockData, Int>()

        // Calculates tripwire states
        if (tripwireConfig.isEnabled) for (i in 0..127) {
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
        if (noteConfig.isEnabled) for (j in 0..799) {
            val noteBlockData = Bukkit.createBlockData(Material.NOTE_BLOCK) as NoteBlock
            if (j >= 399) noteBlockData.instrument = Instrument.getByType((j / 50 % 400).toByte()) ?: continue
            else noteBlockData.instrument = Instrument.getByType((j / 25 % 400).toByte()) ?: continue
            noteBlockData.note = Note((j % 25))
            noteBlockData.isPowered = j !in 0..399

            blockMap.putIfAbsent(noteBlockData, j)
        }

        // Calculates chorus plant states
        if (chorusConfig.isEnabled) for (k in 0..63) {
            val chorusData = Bukkit.createBlockData(Material.CHORUS_PLANT) as MultipleFacing
            if (k and 1 == 1) chorusData.setFace(BlockFace.NORTH, true)
            if (k shr 1 and 1 == 1) chorusData.setFace(BlockFace.EAST, true)
            if (k shr 2 and 1 == 1) chorusData.setFace(BlockFace.SOUTH, true)
            if (k shr 3 and 1 == 1) chorusData.setFace(BlockFace.WEST, true)
            if (k shr 4 and 1 == 1) chorusData.setFace(BlockFace.UP, true)
            if (k shr 5 and 1 == 1) chorusData.setFace(BlockFace.DOWN, true)

            blockMap.putIfAbsent(chorusData, k)
        }

        // Calculates leaf states
        // Should waterlog be used aswell?
        if (leafConfig.isEnabled) for (l in 1..63) {
            val leafData = Bukkit.createBlockData(getLeafMaterial(l)) as Leaves
            val distance = getLeafDistance(l)
            if (distance == 1 && leafConfig.shouldReserveOnePersistentLeafPerType) continue // Skip if one leaf is reserved

            leafData.isPersistent = true
            leafData.distance = distance
            // Due to map using material before distance the Int is scued by 1 if set to reserve 1 state
            blockMap.putIfAbsent(leafData, getBlockMapEntryForLeaf(l))
        }

        // Calculates cave-vine states
        if (caveVineConfig.isEnabled) {
            for (m in 1..50) {
                val vineData = Bukkit.createBlockData(Material.CAVE_VINES) as CaveVines
                vineData.isBerries = m > 25
                vineData.age = if (m > 25) m - 25 else m
                blockMap.putIfAbsent(vineData, m)
            }
        }
        return blockMap
    }
}
