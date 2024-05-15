package com.mineinabyss.blocky.assets_generation

import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.BlockyInfo
import com.mineinabyss.blocky.components.features.blocks.BlockyDirectional
import com.mineinabyss.blocky.systems.blockPrefabs
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.gearyBlocks
import com.mineinabyss.geary.prefabs.PrefabKey
import net.kyori.adventure.key.Key
import org.bukkit.Instrument
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.block.data.type.Tripwire
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.blockstate.BlockState
import team.unnamed.creative.blockstate.MultiVariant
import team.unnamed.creative.blockstate.Variant
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackWriter

class ResourcepackGeneration {

    private val resourcePack = ResourcePack.resourcePack()
    fun generateDefaultAssets() {
        resourcePack.blockState(blockState(SetBlock.BlockType.NOTEBLOCK))
        resourcePack.blockState(blockState(SetBlock.BlockType.WIRE))
        MinecraftResourcePackWriter.minecraft().writeToDirectory(blocky.plugin.dataFolder.resolve("pack"), resourcePack)
    }

    private fun blockState(blockType: SetBlock.BlockType): BlockState {
        val multiVariant = gearyBlocks.block2Prefab.blockMap[blockType]?.mapIndexed { index, blockData ->
            val query = blockPrefabs.firstOrNull { it.block.blockId == index } ?: return@mapIndexed null
            val variant = MultiVariant.of(Variant.builder().properties(query.prefabKey)?.build()) ?: return@mapIndexed null
            blockData.toStringData() to variant
        }?.filterNotNull()?.toMap()?.toMutableMap() ?: mutableMapOf()

        // Add the vanilla block to the blockstate file
        val (vanillaMaterial, vanillaVariant) = when (blockType) {
            SetBlock.BlockType.NOTEBLOCK -> Material.NOTE_BLOCK to (Key.key("block/note_block"))
            SetBlock.BlockType.WIRE -> Material.TRIPWIRE to Key.key("block/barrier")
            else -> Material.AIR to Key.key("nothing")
        }

        multiVariant[vanillaMaterial.createBlockData().toStringData()] =
            MultiVariant.of(Variant.builder().model(vanillaVariant).build())

        return BlockState.of(blockType.blockStateKey(), multiVariant)
    }

    private fun SetBlock.BlockType.blockStateKey() = when (this) {
        SetBlock.BlockType.NOTEBLOCK -> Key.key("note_block")
        SetBlock.BlockType.WIRE -> Key.key("tripwire")
        else -> Key.key("nothing")
    }

    private fun Variant.Builder.properties(prefabKey: PrefabKey): Variant.Builder? {
        val entity = prefabKey.toEntityOrNull() ?: return null
        entity.has<SetBlock>() || return null
        val blockyInfo = entity.get<BlockyInfo>()
        val directional = entity.get<BlockyDirectional>()

        return when {
            directional?.parentBlock?.toEntityOrNull() != null ->
                this.directionalVariant(prefabKey, directional.parentBlock.toEntity())

            directional?.isParentBlock != false ->
                this.model(blockyInfo?.blockModel)

            else -> null
        }
    }

    private fun Variant.Builder.directionalVariant(prefabKey: PrefabKey, parent: GearyEntity): Variant.Builder {
        val childModel = prefabKey.toEntityOrNull()?.get<BlockyInfo>()?.blockModel
        val parentModel = parent.get<BlockyInfo>()?.blockModel

        return this.model(childModel ?: parentModel ?: Key.key("minecraft:block/note_block")).also {
            val parentBlock = parent.get<BlockyDirectional>() ?: return@also
            if (childModel == null || childModel == parentModel) when (prefabKey) {
                parentBlock.zBlock, parentBlock.eastBlock -> {
                    it.x(90)
                    it.y(90)
                }

                parentBlock.xBlock -> it.x(90)
                parentBlock.southBlock -> it.y(180)
                parentBlock.westBlock -> it.y(270)
                parentBlock.upBlock -> it.y(270)
                parentBlock.downBlock -> it.x(180)
                else -> {}
            }
        }
    }

    private fun BlockData.toStringData(): String {
        return when (this) {
            is NoteBlock -> this.noteBlockData()
            is Tripwire -> this.tripwireData()
            else -> ""
        }
    }

    private fun NoteBlock.noteBlockData(): String {
        return String.format(
            "instrument=%s,note=%s,powered=%s",
            getInstrument(this.instrument),
            gearyBlocks.block2Prefab.blockMap[SetBlock.BlockType.NOTEBLOCK]?.indexOf(this)?.mod(25) ?: 0,
            this.isPowered
        )
    }

    private fun getInstrument(instrument: Instrument): String {
        return when (instrument) {
            Instrument.BASS_DRUM -> "basedrum"
            Instrument.PIANO -> "harp"
            Instrument.SNARE_DRUM -> "snare"
            Instrument.STICKS -> "hat"
            Instrument.BASS_GUITAR -> "bass"
            else -> instrument.name.lowercase()
        }
    }


    private fun Tripwire.tripwireData(): String {
        return "north=${hasFace(BlockFace.NORTH)},south=${hasFace(BlockFace.SOUTH)},west=${hasFace(BlockFace.WEST)},east=${hasFace(BlockFace.EAST)},attached=$isAttached,disarmed=$isDisarmed,powered=$isPowered"
    }


}
