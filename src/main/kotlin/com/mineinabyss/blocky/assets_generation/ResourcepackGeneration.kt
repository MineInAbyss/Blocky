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

    val resourcePack = ResourcePack.resourcePack()
    fun generateDefaultAssets() {
        resourcePack.blockState(blockState(SetBlock.BlockType.NOTEBLOCK))
        resourcePack.blockState(blockState(SetBlock.BlockType.WIRE))
        MinecraftResourcePackWriter.minecraft().writeToDirectory(blocky.plugin.dataFolder.resolve("pack"), resourcePack)
    }

    private fun blockState(blockType: SetBlock.BlockType): BlockState {
        val multiVariant = gearyBlocks.block2Prefab.blockMap[blockType]?.mapIndexed { index, blockData ->
            val query = blockPrefabs.firstOrNull { it.block.blockId == index } ?: return@mapIndexed null
            blockData.toStringData() to (MultiVariant.of(Variant.builder().properties(query.prefabKey)?.build())
                ?: return@mapIndexed null)
        }?.filterNotNull()?.toMap()?.toMutableMap() ?: mutableMapOf()
        return BlockState.of(blockType.blockStateKey(), multiVariant)
    }

    private fun SetBlock.BlockType.blockStateKey() = when (this) {
        SetBlock.BlockType.NOTEBLOCK -> Key.key("noteblock")
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
        return when (this.material) {
            Material.NOTE_BLOCK -> this.noteBlockData()
            Material.TRIPWIRE -> this.tripwireData()
            else -> ""
        }
    }

    private fun BlockData.noteBlockData(): String {
        this as NoteBlock
        return String.format(
            "instrument=%s,note=%s,powered=%s",
            getInstrument(this.instrument),
            (gearyBlocks.block2Prefab.blockMap[SetBlock.BlockType.NOTEBLOCK]?.indexOf(this)?.minus(1))?.mod(25) ?: 0,
            this.isPowered
        )
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


    private fun BlockData.tripwireData(): String {
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


}
