package com.mineinabyss.blocky

import com.google.gson.JsonObject
import com.mineinabyss.blocky.components.BlockType
import com.mineinabyss.blocky.helpers.leafList
import com.mineinabyss.blocky.systems.blockyModelQuery
import okio.Path.Companion.toPath
import org.bukkit.Instrument
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.MultipleFacing
import org.bukkit.block.data.type.CaveVines
import org.bukkit.block.data.type.Leaves
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.block.data.type.Tripwire
import java.nio.charset.Charset

class ResourcepackGeneration {

    fun generateDefaultAssets() {
        val root = "${blockyPlugin.dataFolder.absolutePath}/assets/minecraft/blockstates"
        val noteBlockFile = "${root}/note_block.json".toPath().toFile()
        val tripwireFile = "${root}/tripwire.json".toPath().toFile()
        val chorusPlantFile = "${root}/chorus_plant.json".toPath().toFile()
        val leafFiles = leafList.map { "${root}/${it}.json".toPath().toFile() }
        val caveVineFile = "${root}/cave_vine.json".toPath().toFile()

        noteBlockFile.writeText(getNoteBlockBlockStates().toString(), Charset.defaultCharset())
        tripwireFile.writeText(getTripwireBlockStates().toString(), Charset.defaultCharset())
        chorusPlantFile.writeText(getChorusPlantBlockStates().toString(), Charset.defaultCharset())
        leafFiles.forEach { it.writeText(getLeafBlockStates().toString(), Charset.defaultCharset()) }
        caveVineFile.writeText(getCaveVineBlockStates().toString(), Charset.defaultCharset())
    }

    private fun getNoteBlockBlockStates(): JsonObject {
        val variants = JsonObject()
        val blockModel = JsonObject()
        val blockyQuery = blockyModelQuery.filter { it != null && it.blockType == BlockType.CUBE }
        blockMap.filter { it.key is NoteBlock }.forEach { block ->
            //TODO Find out why this query returns null
            val modelID = blockyQuery.firstOrNull { it?.blockId == block.value }?.blockModel ?: return@forEach
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
            blockMap[this]?.rem(25)!!,
            this.isPowered
        )
    }

    private fun getTripwireBlockStates(): JsonObject {
        val variants = JsonObject()
        val blockModel = JsonObject()
        val blockyQuery = blockyModelQuery.filter { it != null && it.blockType == BlockType.GROUND }
        blockMap.filter { it.key is Tripwire }.forEach { block ->
            //TODO Find out why this query returns null
            val modelID = blockyQuery.firstOrNull { it?.blockId == block.value }?.blockModel ?: return@forEach
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

    private fun getChorusPlantBlockStates() : JsonObject {
        val variants = JsonObject()
        val blockModel = JsonObject()
        val blockyQuery = blockyModelQuery.filter { it != null && it.blockType == BlockType.TRANSPARENT }
        blockMap.filter { it.key is MultipleFacing }.forEach { block ->
            //TODO Find out why this query returns null
            val modelID = blockyQuery.firstOrNull { it?.blockId == block.value }?.blockModel ?: return@forEach
            blockModel.add(block.key.getChorusPlantData(), modelID.getModelJson())
        }
        variants.add("variants", blockModel)
        return variants
    }

    private fun BlockData.getChorusPlantData() : String {
        this as MultipleFacing
        return String.format(
            "%north=%s,south=%s,west=%s,east=%s,up=%s,down=%s",
            this.hasFace(BlockFace.NORTH),
            this.hasFace(BlockFace.SOUTH),
            this.hasFace(BlockFace.WEST),
            this.hasFace(BlockFace.EAST),
            this.hasFace(BlockFace.UP),
            this.hasFace(BlockFace.DOWN)
        )
    }

    private fun getLeafBlockStates() : JsonObject {
        val variants = JsonObject()
        val blockModel = JsonObject()
        val blockyQuery = blockyModelQuery.filter { it != null && it.blockType == BlockType.LEAF }
        blockMap.filter { it.key is Leaves }.forEach { block ->
            //TODO Find out why this query returns null
            val modelID = blockyQuery.firstOrNull { it?.blockId == block.value }?.blockModel ?: return@forEach
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
        val blockyQuery = blockyModelQuery.filter { it != null && it.blockType == BlockType.CAVEVINE }
        blockMap.filter { it.key is CaveVines }.forEach { block ->
            //TODO Find out why this query returns null
            val modelID = blockyQuery.firstOrNull { it?.blockId == block.value }?.blockModel ?: return@forEach
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
