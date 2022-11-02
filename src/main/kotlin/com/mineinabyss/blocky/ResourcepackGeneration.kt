package com.mineinabyss.blocky

import com.google.gson.JsonObject
import com.mineinabyss.blocky.components.core.BlockyBlock.BlockType
import com.mineinabyss.blocky.systems.BlockyTypeQuery
import com.mineinabyss.blocky.systems.BlockyTypeQuery.type
import okio.Path.Companion.toPath
import org.bukkit.Bukkit
import org.bukkit.Instrument
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.CaveVines
import org.bukkit.block.data.type.Leaves
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.block.data.type.Tripwire
import java.nio.charset.Charset

class ResourcepackGeneration {

    fun generateDefaultAssets() {
        val root = "${blockyPlugin.dataFolder.absolutePath}/assets/minecraft/blockstates".run { toPath().toFile().mkdirs(); this }
        val noteBlockFile = "${root}/note_block.json".toPath().toFile().run { createNewFile(); this }
        val tripwireFile = "${root}/tripwire.json".toPath().toFile().run { createNewFile(); this }
        //val leafFiles = leafList.map { "${root}/${it.toString().lowercase()}.json".toPath().toFile().run { createNewFile(); this } }
        val caveVineFile = "${root}/cave_vine.json".toPath().toFile().run { createNewFile(); this }

        noteBlockFile.writeText(getNoteBlockBlockStates().toString(), Charset.defaultCharset())
        tripwireFile.writeText(getTripwireBlockStates().toString(), Charset.defaultCharset())
        //leafFiles.forEach { it.writeText(getLeafBlockStates().toString(), Charset.defaultCharset()) }
        caveVineFile.writeText(getCaveVineBlockStates().toString(), Charset.defaultCharset())

        if (!blockyConfig.noteBlocks.isEnabled) noteBlockFile.delete()
        else if (!blockyConfig.tripWires.isEnabled) tripwireFile.delete()
        //else if (!blockyConfig.leafBlocks.isEnabled) leafFiles.forEach { it.delete() }
        else if (!blockyConfig.caveVineBlocks.isEnabled) caveVineFile.delete()
    }

    private fun getNoteBlockBlockStates(): JsonObject {
        val variants = JsonObject()
        val blockModel = JsonObject()
        val blockyQuery = BlockyTypeQuery.filter { it.type.blockType == BlockType.NOTEBLOCK }.map { it.type }
        blockModel.add(Bukkit.createBlockData(Material.NOTE_BLOCK).getNoteBlockData(), "minecraft:block/note_block".getModelJson())
        blockMap.filter { it.key is NoteBlock }.forEach { block ->
            val modelID = blockyQuery.firstOrNull { it.blockId == block.value }?.blockModel ?: return@forEach
            blockModel.add(block.key.getNoteBlockData(), modelID.getModelJson())
        }
        variants.add("variants", blockModel)
        return variants
    }

    private fun BlockData.getNoteBlockData(): String {
        this as NoteBlock
        return String.format(
            "instrument=%s,note=%s,powered=%s",
            getInstrument(this.instrument),
            blockMap[this],
            this.isPowered
        )
    }

    private fun getTripwireBlockStates(): JsonObject {
        val variants = JsonObject()
        val blockModel = JsonObject()
        val blockyQuery = BlockyTypeQuery.filter { it.type.blockType == BlockType.TRIPWIRE }.map { it.type }
        blockModel.add(Bukkit.createBlockData(Material.TRIPWIRE).getTripwireData(), "minecraft:block/barrier".getModelJson())
        blockMap.filter { it.key is Tripwire }.forEach { block ->
            val modelID = blockyQuery.firstOrNull { it.blockId == block.value }?.blockModel ?: return@forEach
            blockModel.add(block.key.getTripwireData(), modelID.getModelJson())
        }
        variants.add("variants", blockModel)
        return variants
    }

    private fun BlockData.getTripwireData(): String {
        this as Tripwire
        return String.format(
            "north=%s,south=%s,west=%s,east=%s,attached=%s,disarmed=%s,powered=%s",
            hasFace(BlockFace.NORTH),
            hasFace(BlockFace.SOUTH),
            hasFace(BlockFace.WEST),
            hasFace(BlockFace.EAST),
            isAttached,
            isDisarmed,
            isPowered
        )
    }

    private fun getLeafBlockStates() : JsonObject {
        val variants = JsonObject()
        val blockModel = JsonObject()
        val blockyQuery = BlockyTypeQuery.filter { it.type.blockType == BlockType.LEAF }.map { it.type }
        //blockModel.add(Bukkit.createBlockData().getNoteBlockData(), "minecraft:block/note_block".getModelJson())
        blockMap.filter { it.key is Leaves }.forEach { block ->
            val modelID = blockyQuery.firstOrNull { it.blockId == block.value }?.blockModel ?: return@forEach
            blockModel.add((block.key as Leaves).getLeafBlockStates(), modelID.getModelJson())
        }
        variants.add("variants", blockModel)
        return variants
    }

    private fun BlockData.getLeafBlockStates(): String {
        return "distance=${(this as Leaves).distance},persistent=true,waterlogged=false"
    }

    private fun getCaveVineBlockStates() : JsonObject {
        val variants = JsonObject()
        val blockModel = JsonObject()
        val blockyQuery = BlockyTypeQuery.filter { it.type.blockType == BlockType.CAVEVINE }.map { it.type }
        blockModel.add(Bukkit.createBlockData(Material.CAVE_VINES).getCaveVineBlockStates(), "minecraft:block/cave_vines".getModelJson())
        blockMap.filter { it.key is CaveVines }.forEach { block ->
            val modelID = blockyQuery.firstOrNull { it.blockId == block.value }?.blockModel ?: return@forEach
            blockModel.add(block.key.getCaveVineBlockStates(), modelID.getModelJson())
        }
        variants.add("variants", blockModel)
        return variants
    }

    private fun BlockData.getCaveVineBlockStates(): String {
        this as CaveVines
        return "age=${age},berries=$isBerries"
    }

    private fun String.getModelJson(): JsonObject {
        val content = JsonObject()
        content.addProperty("model", this)
        return content
    }

    private fun getInstrument(id: Instrument): String {
        when (id) {
            Instrument.BASS_DRUM -> return "basedrum"
            Instrument.STICKS -> return "hat"
            Instrument.SNARE_DRUM -> return "snare"
            Instrument.PIANO -> return "harp"
            Instrument.BASS_GUITAR -> return "bass"
            Instrument.FLUTE -> return "flute"
            Instrument.BELL -> return "bell"
            Instrument.GUITAR -> return "guitar"
            Instrument.CHIME -> return "chime"
            Instrument.XYLOPHONE -> return "xylophone"
            Instrument.IRON_XYLOPHONE -> return "iron_xylophone"
            Instrument.COW_BELL -> return "cow_bell"
            Instrument.DIDGERIDOO -> return "didgeridoo"
            Instrument.BIT -> return "bit"
            Instrument.BANJO -> return "banjo"
            Instrument.PLING -> return "pling"
            else -> return "hat"
        }
    }


}
