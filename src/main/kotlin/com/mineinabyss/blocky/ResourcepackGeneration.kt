package com.mineinabyss.blocky

import com.google.gson.JsonObject
import com.mineinabyss.blocky.components.core.BlockyBlock
import com.mineinabyss.blocky.components.core.BlockyBlock.BlockType
import com.mineinabyss.blocky.components.features.BlockyDirectional
import com.mineinabyss.blocky.helpers.BLOCKY_SLABS
import com.mineinabyss.blocky.helpers.BLOCKY_STAIRS
import com.mineinabyss.blocky.helpers.isCardinal
import com.mineinabyss.blocky.systems.BlockyBlockQuery
import com.mineinabyss.blocky.systems.BlockyBlockQuery.prefabKey
import com.mineinabyss.blocky.systems.BlockyBlockQuery.type
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.prefabs.PrefabKey
import okio.Path.Companion.toPath
import org.bukkit.Instrument
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.*
import java.io.File
import java.nio.charset.Charset

class ResourcepackGeneration {

    fun generateDefaultAssets() {
        val root = "${blockyPlugin.dataFolder.absolutePath}/assets/minecraft/blockstates".run {
            toPath().toFile().mkdirs(); this
        }
        val noteBlockFile = "${root}/note_block.json".toPath().toFile()
        val tripwireFile = "${root}/tripwire.json".toPath().toFile()
        //val leafFiles = leafList.map { "${root}/${it.toString().lowercase()}.json".toPath().toFile().run { createNewFile(); this } }
        val caveVineFile = "${root}/cave_vine.json".toPath().toFile()
        val slabFiles = BLOCKY_SLABS.map { "${root}/${it.toString().lowercase()}.json".toPath().toFile() }
        val stairFiles = BLOCKY_STAIRS.map { "${root}/${it.toString().lowercase()}.json".toPath().toFile() }

        noteBlockFile.writeJson(getNoteBlockBlockStates())
        tripwireFile.writeJson(getTripwireBlockStates())
        //leafFiles.forEach { it.writeText(getLeafBlockStates().toString(), Charset.defaultCharset()) }
        caveVineFile.writeJson(getCaveVineBlockStates())
        slabFiles.forEach { it.writeJson(getSlabBlockStates()) }
        stairFiles.forEach { it.writeJson(getStairBlockStates()) }

        if (!blockyConfig.noteBlocks.isEnabled) noteBlockFile.delete()
        if (!blockyConfig.tripWires.isEnabled) tripwireFile.delete()
        //else if (!blockyConfig.leafBlocks.isEnabled) leafFiles.forEach { it.delete() }
        if (!blockyConfig.caveVineBlocks.isEnabled) caveVineFile.delete()
        if (!blockyConfig.slabBlocks.isEnabled) slabFiles.forEach { it.delete() }
        if (!blockyConfig.stairBlocks.isEnabled) stairFiles.forEach { it.delete() }
    }

    private fun getNoteBlockBlockStates(): JsonObject {
        return JsonObject().apply {
            val blockModel = JsonObject()
            val blockyQuery = BlockyBlockQuery.filter { it.type.blockType == BlockType.NOTEBLOCK }
            blockModel.add(
                Material.NOTE_BLOCK.createBlockData().getNoteBlockData(),
                "minecraft:block/note_block".getModelJson()
            )

            blockMap.filter { it.key is NoteBlock }.forEach { block ->
                val query = blockyQuery.firstOrNull { it.type.blockId == block.value } ?: return@forEach
                query.prefabKey.getJsonProperties()?.let { blockModel.add(block.key.getNoteBlockData(), it) }
            }
            if (blockModel.keySet().isNotEmpty()) add("variants", blockModel)
        }
    }

    private fun BlockData.getNoteBlockData(): String {
        this as NoteBlock
        return String.format(
            "instrument=%s,note=%s,powered=%s",
            getInstrument(this.instrument),
            (blockMap[this]?.minus(1))?.mod(25) ?: 0,
            this.isPowered
        )
    }

    private fun getTripwireBlockStates(): JsonObject {
        return JsonObject().apply {
            val blockModel = JsonObject()
            val blockyQuery = BlockyBlockQuery.filter { it.type.blockType == BlockType.WIRE }.map { it.type }
            blockModel.add(
                Material.TRIPWIRE.createBlockData().getTripwireData(),
                "minecraft:block/barrier".getModelJson()
            )
            blockMap.filter { it.key is Tripwire }.forEach { block ->
                val modelID = blockyQuery.firstOrNull { it.blockId == block.value }?.blockModel ?: return@forEach
                blockModel.add(block.key.getTripwireData(), modelID.getModelJson())
            }
            if (blockModel.keySet().isNotEmpty()) add("variants", blockModel)
        }
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

    private fun getLeafBlockStates(): JsonObject {
        val variants = JsonObject()
        val blockModel = JsonObject()
        val blockyQuery = BlockyBlockQuery.filter { it.type.blockType == BlockType.LEAF }.map { it.type }
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

    private fun getCaveVineBlockStates(): JsonObject {
        return JsonObject().apply {
            val blockModel = JsonObject()
            val blockyQuery = BlockyBlockQuery.filter { it.type.blockType == BlockType.CAVEVINE }.map { it.type }
            blockModel.add(
                Material.CAVE_VINES.createBlockData().getCaveVineBlockStates(),
                "minecraft:block/cave_vines".getModelJson()
            )
            blockMap.filter { it.key is CaveVines }.forEach { block ->
                val modelID = blockyQuery.firstOrNull { it.blockId == block.value }?.blockModel ?: return@forEach
                blockModel.add(block.key.getCaveVineBlockStates(), modelID.getModelJson())
            }
            if (blockModel.keySet().isNotEmpty()) add("variants", blockModel)
        }
    }

    //TODO Make this not handle all materials
    private fun getSlabBlockStates(): JsonObject {
        return JsonObject().apply {
            val blockModel = JsonObject()
            val blockyQuery = BlockyBlockQuery.filter { it.type.blockType == BlockType.SLAB }.map { it.type }
            blockMap.filter { it.key is Slab }.forEach block@{ block ->
                Slab.Type.values().map { it.name.lowercase() }.forEach {
                    val modelID = blockyQuery.firstOrNull { b -> b.blockId == block.value }?.blockModel ?: return@block
                    blockModel.add("type=$it", modelID.getModelJson())
                }
            }
            if (blockModel.keySet().isNotEmpty()) this.add("variants", blockModel)
        }
    }

    private fun getStairBlockStates(): JsonObject {
        return JsonObject().apply {
            val blockModel = JsonObject()
            val blockyQuery = BlockyBlockQuery.filter { it.type.blockType == BlockType.STAIR }.map { it.type }
            blockMap.filter { it.key is Stairs }.forEach block@{ block ->
                for (facing in BlockFace.values().filter { it.isCardinal })
                    for (half in Bisected.Half.values())
                        for (shape in Stairs.Shape.values())
                            blockyQuery.firstOrNull { b -> b.blockId == block.value }?.blockModel?.let { m ->
                                blockModel.add(
                                    "facing=${facing.name.lowercase()},half=${half.name.lowercase()},shape=${shape.name.lowercase()}",
                                    m.getStairModelJson(facing, half, shape)
                                )
                            } ?: return@block
            }
            if (blockModel.keySet().isNotEmpty()) add("variants", blockModel)
        }
    }

    private fun BlockData.getCaveVineBlockStates(): String {
        this as CaveVines
        return "age=${age},berries=$isBerries"
    }

    private fun PrefabKey.getJsonProperties(): JsonObject? {
        val entity = this.toEntityOrNull() ?: return null
        val blockyBlock = entity.get<BlockyBlock>() ?: return null
        val directional = entity.get<BlockyDirectional>()

        return when {
            directional?.parentBlock?.toEntityOrNull() != null ->
                this.directionalJsonProperties(directional.parentBlock.toEntity())

            directional?.isParentBlock != false ->
                JsonObject().apply { addProperty("model", blockyBlock.blockModel) }

            else -> null
        }
    }

    private fun PrefabKey.directionalJsonProperties(parent: GearyEntity): JsonObject? {
        return JsonObject().apply {
            val childModel = this@directionalJsonProperties.toEntityOrNull()?.get<BlockyBlock>()?.blockModel
            val parentModel = parent.get<BlockyBlock>()?.blockModel
            this.addProperty("model", childModel ?: parentModel ?: return null)

            // If using the parent model, we need to add the rotation depending on the childDirection
            if (childModel == null || childModel == parentModel) {
                parent.get<BlockyDirectional>()?.let { p ->
                    when {
                        this@directionalJsonProperties == p.xBlock -> {
                            this.addProperty("x", 90)
                            this.addProperty("z", 90)
                        }

                        this@directionalJsonProperties == p.zBlock || this@directionalJsonProperties == p.eastBlock -> {
                            this.addProperty("x", 90)
                            this.addProperty("y", 90)
                        }

                        this@directionalJsonProperties == p.southBlock ->
                            this.addProperty("y", 180)

                        this@directionalJsonProperties == p.westBlock -> {
                            this.addProperty("z", 90)
                            this.addProperty("y", 270)
                        }

                        this@directionalJsonProperties == p.upBlock ->
                            this.addProperty("y", 270)

                        this@directionalJsonProperties == p.downBlock ->
                            this.addProperty("x", 180)
                    }
                }
            }
        }
    }

    private fun String.getModelJson(): JsonObject {
        return JsonObject().apply {
            addProperty("model", this@getModelJson)
        }
    }

    private fun String.getStairModelJson(facing: BlockFace, half: Bisected.Half, shape: Stairs.Shape): JsonObject {
        return JsonObject().apply {
            addProperty("model", this@getStairModelJson)
            addProperty("uvlock", true)
            when (shape) {
                in setOf(Stairs.Shape.INNER_LEFT, Stairs.Shape.OUTER_LEFT) -> {
                    when (facing) {
                        BlockFace.EAST -> {
                            if (half == Bisected.Half.BOTTOM) addProperty("y", 270)
                            else addProperty("y", 180)
                        }

                        BlockFace.NORTH -> {
                            if (half == Bisected.Half.BOTTOM) addProperty("y", 180)
                            else {
                                addProperty("x", 180)
                                addProperty("y", 270)
                            }
                        }

                        BlockFace.SOUTH -> {
                            if (half == Bisected.Half.TOP) {
                                addProperty("x", 180)
                                addProperty("y", 90)
                            } else remove("uvlock")
                        }

                        BlockFace.WEST -> {
                            if (half == Bisected.Half.BOTTOM) addProperty("y", 90)
                            else {
                                addProperty("x", 180)
                                addProperty("y", 180)
                            }
                        }

                        else -> remove("uvlock")
                    }
                }

                in setOf(Stairs.Shape.INNER_RIGHT, Stairs.Shape.OUTER_RIGHT) -> {
                    when (facing) {
                        BlockFace.EAST -> {
                            if (half == Bisected.Half.TOP) {
                                addProperty("x", 180)
                                addProperty("y", 90)
                            } else remove("uvlock")
                        }

                        BlockFace.NORTH -> {
                            if (half == Bisected.Half.BOTTOM) addProperty("x", 270)
                            else addProperty("x", 180)
                        }

                        BlockFace.SOUTH -> {
                            if (half == Bisected.Half.BOTTOM) addProperty("y", 90)
                            else {
                                addProperty("x", 180)
                                addProperty("y", 180)
                            }
                        }

                        BlockFace.WEST -> {
                            if (half == Bisected.Half.BOTTOM) addProperty("y", 180)
                            else {
                                addProperty("x", 180)
                                addProperty("y", 270)
                            }
                        }

                        else -> remove("uvlock")
                    }
                }

                Stairs.Shape.STRAIGHT -> {
                    when (facing) {
                        BlockFace.EAST ->
                            if (half == Bisected.Half.TOP) addProperty("x", 180)
                            else remove("uvlock")

                        BlockFace.NORTH -> {
                            if (half == Bisected.Half.BOTTOM) addProperty("x", 270)
                            else {
                                addProperty("x", 180)
                                addProperty("y", 270)
                            }
                        }

                        BlockFace.SOUTH -> {
                            if (half == Bisected.Half.BOTTOM) addProperty("y", 90)
                            else {
                                addProperty("x", 180)
                                addProperty("y", 90)
                            }
                        }

                        BlockFace.WEST -> {
                            addProperty("y", 180)
                            if (half == Bisected.Half.TOP) addProperty("x", 180)
                        }

                        else -> remove("uvlock")
                    }
                }

                else -> remove("uvlock")
            }
        }
    }

    private fun File.writeJson(content: JsonObject) {
        if (!this.exists()) this.createNewFile()
        if (content.keySet().isEmpty()) this.delete()
        else this.writeText(content.toString(), Charset.defaultCharset())
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
