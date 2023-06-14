package com.mineinabyss.blocky

import com.google.gson.JsonObject
import com.mineinabyss.blocky.components.core.BlockyInfo
import com.mineinabyss.blocky.components.features.blocks.BlockyDirectional
import com.mineinabyss.blocky.helpers.BLOCKY_SLABS
import com.mineinabyss.blocky.helpers.BLOCKY_STAIRS
import com.mineinabyss.blocky.systems.BlockyBlockQuery
import com.mineinabyss.blocky.systems.BlockyBlockQuery.block
import com.mineinabyss.blocky.systems.BlockyBlockQuery.prefabKey
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.gearyBlocks
import com.mineinabyss.geary.prefabs.PrefabKey
import okio.Path.Companion.toPath
import org.bukkit.Instrument
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.CaveVines
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.block.data.type.Stairs
import org.bukkit.block.data.type.Tripwire
import java.io.File
import java.nio.charset.Charset

class ResourcepackGeneration {

    fun generateDefaultAssets() {
        val root = "${blocky.plugin.dataFolder.absolutePath}/assets/minecraft/blockstates".run {
            toPath().toFile().mkdirs(); this
        }
        val noteBlockFile = "${root}/note_block.json".toPath().toFile()
        val tripwireFile = "${root}/tripwire.json".toPath().toFile()
        val caveVineFile = "${root}/cave_vine.json".toPath().toFile()
        val slabFiles = BLOCKY_SLABS.map { "${root}/${it.toString().lowercase()}.json".toPath().toFile() }
        val stairFiles = BLOCKY_STAIRS.map { "${root}/${it.toString().lowercase()}.json".toPath().toFile() }

        noteBlockFile.writeJson(getNoteBlockBlockStates())
        tripwireFile.writeJson(getTripwireBlockStates())
        caveVineFile.writeJson(getCaveVineBlockStates())
        //slabFiles.forEach { it.writeJson(getSlabBlockStates()) }
        //stairFiles.forEach { it.writeJson(getStairBlockStates()) }

        if (!blocky.config.noteBlocks.isEnabled) noteBlockFile.delete()
        if (!blocky.config.tripWires.isEnabled) tripwireFile.delete()
        if (!blocky.config.caveVineBlocks.isEnabled) caveVineFile.delete()
        if (!blocky.config.slabBlocks.isEnabled) slabFiles.forEach { it.delete() }
        if (!blocky.config.stairBlocks.isEnabled) stairFiles.forEach { it.delete() }
    }

    private fun getNoteBlockBlockStates(): JsonObject {
        return JsonObject().apply {
            val blockModel = JsonObject()
            val blockyQuery = BlockyBlockQuery.filter { it.block.blockType == SetBlock.BlockType.NOTEBLOCK }
            blockModel.add(
                Material.NOTE_BLOCK.createBlockData().getNoteBlockData(),
                "minecraft:block/note_block".getModelJson()
            )

            gearyBlocks.block2Prefab.blockMap.filter { it.key == SetBlock.BlockType.NOTEBLOCK }.values.forEach { blocks ->
                blocks.forEachIndexed { index, block ->
                    val query = blockyQuery.firstOrNull { it.block.blockId == index } ?: return@forEach
                    query.prefabKey.getJsonProperties()?.let { blockModel.add(block.getNoteBlockData(), it) }
                }
            }
            if (blockModel.keySet().isNotEmpty()) add("variants", blockModel)
        }
    }

    private fun BlockData.getNoteBlockData(): String {
        this as NoteBlock
        return String.format(
            "instrument=%s,note=%s,powered=%s",
            getInstrument(this.instrument),
            (gearyBlocks.block2Prefab.blockMap[SetBlock.BlockType.NOTEBLOCK]?.indexOf(this)?.minus(1))?.mod(25) ?: 0,
            this.isPowered
        )
    }

    private fun getTripwireBlockStates(): JsonObject {
        return JsonObject().apply {
            val blockModel = JsonObject()
            val blockyQuery = BlockyBlockQuery.filter { it.block.blockType == SetBlock.BlockType.WIRE }.map { it }
            blockModel.add(
                Material.TRIPWIRE.createBlockData().getTripwireData(),
                "minecraft:block/barrier".getModelJson()
            )
            gearyBlocks.block2Prefab.blockMap.filter { it.key == SetBlock.BlockType.WIRE }.values.forEach { blocks ->
                blocks.forEachIndexed { index, block ->
                    val query = blockyQuery.firstOrNull { it.block.blockId == index } ?: return@forEach
                    query.prefabKey.getJsonProperties()?.let { blockModel.add(block.getTripwireData(), it) }
                }
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

    private fun getCaveVineBlockStates(): JsonObject {
        return JsonObject().apply {
            val blockModel = JsonObject()
            val blockyQuery = BlockyBlockQuery.filter { it.block.blockType == SetBlock.BlockType.CAVEVINE }
            blockModel.add(
                Material.CAVE_VINES.createBlockData().getCaveVineBlockStates(),
                "minecraft:block/cave_vines".getModelJson()
            )
            gearyBlocks.block2Prefab.blockMap.filter { it.key == SetBlock.BlockType.CAVEVINE }.values.forEach { blocks ->
                blocks.forEachIndexed { index, block ->
                    val query = blockyQuery.firstOrNull { it.block.blockId == index } ?: return@forEach
                    query.prefabKey.getJsonProperties()?.let { blockModel.add(block.getCaveVineBlockStates(), it) }
                }
            }
            if (blockModel.keySet().isNotEmpty()) add("variants", blockModel)
        }
    }

    //TODO Make this not handle all materials
    /*private fun getSlabBlockStates(): JsonObject {
        return JsonObject().apply {
            val blockModel = JsonObject()
            val blockyQuery = BlockyBlockQuery.filter { it.block.blockType == SetBlock.BlockType.SLAB }
            gearyBlocks.block2Prefab.blockMap.filter { it.key == SetBlock.BlockType.SLAB }.values.forEach { blocks ->
                blocks.forEachIndexed { index, block ->
                    val query = blockyQuery.firstOrNull { it.block.blockId == index } ?: return@forEach
                    query.prefabKey.getJsonProperties()?.let { blockModel.add(block.getNoteBlockData(), it) }
                }
            }
            if (blockModel.keySet().isNotEmpty()) this.add("variants", blockModel)
        }
    }

    private fun getStairBlockStates(): JsonObject {
        return JsonObject().apply {
            val blockModel = JsonObject()
            val blockyQuery = BlockyBlockQuery.filter { it.block.blockType == SetBlock.BlockType.STAIR }
            gearyBlocks.block2Prefab.blockMap.filter { it.key == SetBlock.BlockType.STAIR }.values.forEach { blocks ->
                blocks.forEachIndexed { index, block ->
                    val query = blockyQuery.firstOrNull { it.block.blockId == index } ?: return@forEach
                    query.prefabKey.getJsonProperties()?.let { blockModel.add(block.getNoteBlockData(), it) }
                }
            }
            gearyBlocks.blockMap.filter { it.key is Stairs }.forEach block@{ block ->
                for (facing in BlockFace.values().filter { it.isCardinal })
                    for (half in Bisected.Half.values())
                        for (shape in Stairs.Shape.values())
                            blockyQuery.firstOrNull { it.block.blockId == block.value }?.entity?.get<BlockyInfo>()?.blockModel?.let { m ->
                                blockModel.add(
                                    "facing=${facing.name.lowercase()},half=${half.name.lowercase()},shape=${shape.name.lowercase()}",
                                    m.getStairModelJson(facing, half, shape)
                                )
                            } ?: return@block
            }
            if (blockModel.keySet().isNotEmpty()) add("variants", blockModel)
        }
    }*/

    private fun BlockData.getCaveVineBlockStates(): String {
        this as CaveVines
        return "age=${age},berries=$isBerries"
    }

    private fun PrefabKey.getJsonProperties(): JsonObject? {
        val entity = this.toEntityOrNull() ?: return null
        val blockyBlock = entity.get<SetBlock>() ?: return null
        val blockyInfo = entity.get<BlockyInfo>()
        val directional = entity.get<BlockyDirectional>()

        return when {
            directional?.parentBlock?.toEntityOrNull() != null ->
                this.directionalJsonProperties(directional.parentBlock.toEntity())

            directional?.isParentBlock != false ->
                JsonObject().apply { addProperty("model", blockyInfo?.blockModel) }

            else -> null
        }
    }

    private fun PrefabKey.directionalJsonProperties(parent: GearyEntity): JsonObject? {
        return JsonObject().apply {
            val childModel = this@directionalJsonProperties.toEntityOrNull()?.get<BlockyInfo>()?.blockModel
            val parentModel = parent.get<BlockyInfo>()?.blockModel
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
