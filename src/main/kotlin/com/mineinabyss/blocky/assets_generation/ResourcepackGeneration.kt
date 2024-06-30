package com.mineinabyss.blocky.assets_generation

import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.BlockyInfo
import com.mineinabyss.blocky.components.features.blocks.BlockyDirectional
import com.mineinabyss.blocky.helpers.CopperHelpers
import com.mineinabyss.blocky.systems.blockPrefabs
import com.mineinabyss.blocky.systems.plantPrefabs
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.gearyBlocks
import com.mineinabyss.geary.prefabs.PrefabKey
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.blockstate.BlockState
import team.unnamed.creative.blockstate.MultiVariant
import team.unnamed.creative.blockstate.Variant
import team.unnamed.creative.model.Model
import team.unnamed.creative.model.ModelTexture
import team.unnamed.creative.model.ModelTextures
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackWriter
import team.unnamed.creative.sound.SoundEntry
import team.unnamed.creative.sound.SoundEvent
import team.unnamed.creative.sound.SoundRegistry

object ResourcepackGeneration {

    private val resourcePack = ResourcePack.resourcePack()
    fun generateDefaultAssets() {
        resourcePack.models().clear()
        resourcePack.blockState(blockState(SetBlock.BlockType.NOTEBLOCK))
        resourcePack.blockState(blockState(SetBlock.BlockType.WIRE))

        copperBlockStates()

        registerRequiredSounds()

        MinecraftResourcePackWriter.minecraft().writeToDirectory(blocky.plugin.dataFolder.resolve("pack"), resourcePack)
    }

    private fun registerRequiredSounds() {
        if (blocky.config.disableCustomSounds) return

        val soundRegistry = resourcePack.soundRegistry("minecraft") ?: SoundRegistry.soundRegistry("minecraft", emptyList())

        SoundRegistry.soundRegistry(soundRegistry.namespace(), soundRegistry.sounds().plus(listOf(
            SoundEvent.soundEvent(Key.key("minecraft:block.stone.place"), true, null,  listOf()),
            SoundEvent.soundEvent(Key.key("minecraft:block.stone.break"), true, null,  listOf()),
            SoundEvent.soundEvent(Key.key("minecraft:block.stone.hit"), true, null,  listOf()),
            SoundEvent.soundEvent(Key.key("minecraft:block.stone.fall"), true, null,  listOf()),
            SoundEvent.soundEvent(Key.key("minecraft:block.stone.step"), true, null,  listOf()),
            SoundEvent.soundEvent(Key.key("minecraft:block.wood.place"), true, null,  listOf()),
            SoundEvent.soundEvent(Key.key("minecraft:block.wood.break"), true, null,  listOf()),
            SoundEvent.soundEvent(Key.key("minecraft:block.wood.hit"), true, null,  listOf()),
            SoundEvent.soundEvent(Key.key("minecraft:block.wood.fall"), true, null,  listOf()),
            SoundEvent.soundEvent(Key.key("minecraft:block.wood.step"), true, null,  listOf())
        ))).let(resourcePack::soundRegistry)

        val blockyRegistry = resourcePack.soundRegistry("blocky") ?: SoundRegistry.soundRegistry("blocky", emptyList())
        SoundRegistry.soundRegistry(blockyRegistry.namespace(), blockyRegistry.sounds().plus(listOf(
            SoundEvent.soundEvent(Key.key("blocky:block.stone.place"), false, "subtitles.block.generic.place", stoneDig),
            SoundEvent.soundEvent(Key.key("blocky:block.stone.break"), false, "subtitles.block.generic.break", stoneDig),
            SoundEvent.soundEvent(Key.key("blocky:block.stone.hit"), false, "subtitles.block.generic.hit", stoneStep),
            SoundEvent.soundEvent(Key.key("blocky:block.stone.fall"), false, "subtitles.block.generic.fall", stoneStep),
            SoundEvent.soundEvent(Key.key("blocky:block.stone.step"), false, "subtitles.block.generic.step", stoneStep),
            SoundEvent.soundEvent(Key.key("blocky:block.wood.place"), false, "subtitles.block.generic.place", woodDig),
            SoundEvent.soundEvent(Key.key("blocky:block.wood.break"), false, "subtitles.block.generic.break", woodDig),
            SoundEvent.soundEvent(Key.key("blocky:block.wood.hit"), false, "subtitles.block.generic.hit", woodStep),
            SoundEvent.soundEvent(Key.key("blocky:block.wood.fall"), false, "subtitles.block.generic.fall", woodStep),
            SoundEvent.soundEvent(Key.key("blocky:block.wood.step"), false, "subtitles.block.generic.step", woodStep)
        ))).let(resourcePack::soundRegistry)
    }

    private val stoneDig = listOf(
        SoundEntry.soundEntry().key(Key.key("dig/stone1")).build(),
        SoundEntry.soundEntry().key(Key.key("dig/stone2")).build(),
        SoundEntry.soundEntry().key(Key.key("dig/stone3")).build(),
        SoundEntry.soundEntry().key(Key.key("dig/stone4")).build()
    )
    private val stoneStep = listOf(
        SoundEntry.soundEntry().key(Key.key("step/stone1")).build(),
        SoundEntry.soundEntry().key(Key.key("step/stone2")).build(),
        SoundEntry.soundEntry().key(Key.key("step/stone3")).build(),
        SoundEntry.soundEntry().key(Key.key("step/stone4")).build(),
        SoundEntry.soundEntry().key(Key.key("step/stone5")).build(),
        SoundEntry.soundEntry().key(Key.key("step/stone6")).build(),
    )
    private val woodDig = listOf(
        SoundEntry.soundEntry().key(Key.key("dig/wood1")).build(),
        SoundEntry.soundEntry().key(Key.key("dig/wood2")).build(),
        SoundEntry.soundEntry().key(Key.key("dig/wood3")).build(),
        SoundEntry.soundEntry().key(Key.key("dig/wood4")).build()
    )
    private val woodStep = listOf(
        SoundEntry.soundEntry().key(Key.key("step/wood1")).build(),
        SoundEntry.soundEntry().key(Key.key("step/wood2")).build(),
        SoundEntry.soundEntry().key(Key.key("step/wood3")).build(),
        SoundEntry.soundEntry().key(Key.key("step/wood4")).build(),
        SoundEntry.soundEntry().key(Key.key("step/wood5")).build(),
        SoundEntry.soundEntry().key(Key.key("step/wood6")).build(),
    )

    private val BlockData.propertiesAsString get() = this.asString.substringAfter("[").substringBeforeLast("]")
    private fun copperBlockStates() {

        fun handleModel(blockType: SetBlock.BlockType, material: Material, index: Int, parent: String, suffix: String) {
            val query = blockPrefabs.find { it.block.blockType == blockType && it.block.blockId == index + 1 } ?: return
            val model = Model.model().textures(ModelTextures.builder().customProperties(query.prefabKey).build())
                .parent(Key.key(parent)).key(Key.key("blocky:block/${material.name.lowercase()}$suffix"))
                .build()

            resourcePack.model(model)
        }

        fun handleBlockState(blockType: SetBlock.BlockType, material: Material, index: Int) {
            blockPrefabs.find { it.block.blockType == blockType && it.block.blockId == index + 1 } ?: return
            resourcePack.blockState(VanillaBlockstateFiles.vanillaBlockStateFile(blockType, material) ?: return)
        }

        CopperHelpers.BLOCKY_STAIRS.forEachIndexed { index, material ->
            handleBlockState(SetBlock.BlockType.STAIR, material, index)
            handleModel(SetBlock.BlockType.STAIR, material, index, "block/stairs", "")
            handleModel(SetBlock.BlockType.STAIR, material, index, "block/inner_stairs", "_inner")
            handleModel(SetBlock.BlockType.STAIR, material, index, "block/outer_stairs", "_outer")
        }

        CopperHelpers.BLOCKY_SLABS.forEachIndexed { index, material ->
            handleBlockState(SetBlock.BlockType.SLAB, material, index)
            handleModel(SetBlock.BlockType.SLAB, material, index, "block/slab", "")
            handleModel(SetBlock.BlockType.SLAB, material, index, "block/slab_top", "_top")
        }

        CopperHelpers.BLOCKY_DOORS.forEachIndexed { index, material ->
            handleBlockState(SetBlock.BlockType.DOOR, material, index)
            handleModel(SetBlock.BlockType.DOOR, material, index, "block/door_bottom_left", "_bottom_left")
            handleModel(SetBlock.BlockType.DOOR, material, index, "block/door_bottom_left_open", "_bottom_left_open")
            handleModel(SetBlock.BlockType.DOOR, material, index, "block/door_bottom_right", "_bottom_right")
            handleModel(SetBlock.BlockType.DOOR, material, index, "block/door_bottom_right_open", "_bottom_right_open")
            handleModel(SetBlock.BlockType.DOOR, material, index, "block/door_top_left", "_top_left")
            handleModel(SetBlock.BlockType.DOOR, material, index, "block/door_top_left_open", "_top_left_open")
            handleModel(SetBlock.BlockType.DOOR, material, index, "block/door_top_right", "_top_right")
            handleModel(SetBlock.BlockType.DOOR, material, index, "block/door_top_right_open", "_top_right_open")
        }

        CopperHelpers.BLOCKY_TRAPDOORS.forEachIndexed { index, material ->
            handleBlockState(SetBlock.BlockType.TRAPDOOR, material, index)
            handleModel(SetBlock.BlockType.TRAPDOOR, material, index, "block/template_trapdoor_bottom", "_bottom")
            handleModel(SetBlock.BlockType.TRAPDOOR, material, index, "block/template_trapdoor_open", "_open")
            handleModel(SetBlock.BlockType.TRAPDOOR, material, index, "block/template_trapdoor_top", "_top")
        }

        CopperHelpers.BLOCKY_GRATE.forEachIndexed { index, material ->
            handleBlockState(SetBlock.BlockType.GRATE, material, index)
            handleModel(SetBlock.BlockType.GRATE, material, index, "block/cube_all", "")
        }
    }

    private fun blockState(blockType: SetBlock.BlockType): BlockState {
        val multiVariant = blockType.multiVariant()

        // Add the vanilla block to the blockstate file
        val (vanillaMaterial, vanillaVariant) = when (blockType) {
            SetBlock.BlockType.NOTEBLOCK -> Material.NOTE_BLOCK to (Key.key("block/note_block"))
            SetBlock.BlockType.WIRE -> Material.TRIPWIRE to Key.key("block/barrier")
            else -> Material.AIR to Key.key("nothing")
        }

        multiVariant[vanillaMaterial.createBlockData().propertiesAsString] =
            MultiVariant.of(Variant.builder().model(vanillaVariant).build())

        return BlockState.of(blockType.blockStateKey(), multiVariant)
    }

    private fun SetBlock.BlockType.multiVariant() =
        gearyBlocks.block2Prefab.blockMap[this]?.mapIndexed { index, blockData ->
            val query = blockPrefabs.find { gearyBlocks.createBlockData(it.prefabKey) == blockData }
                ?: plantPrefabs.find { gearyBlocks.createBlockData(it.prefabKey) == blockData }
                ?: return@mapIndexed null
            val variant = MultiVariant.of(Variant.builder().customProperties(query.prefabKey)?.build()) ?: return@mapIndexed null
            blockData.propertiesAsString to variant
        }?.filterNotNull()?.toMap()?.toMutableMap() ?: mutableMapOf()

    private fun SetBlock.BlockType.blockStateKey(index: Int = 0) = when (this) {
        SetBlock.BlockType.NOTEBLOCK -> Key.key("note_block")
        SetBlock.BlockType.WIRE -> Key.key("tripwire")
        SetBlock.BlockType.STAIR -> Key.key(CopperHelpers.BLOCKY_STAIRS.elementAt(index).name.lowercase())
        SetBlock.BlockType.SLAB -> Key.key(CopperHelpers.BLOCKY_SLABS.elementAt(index).name.lowercase())
        SetBlock.BlockType.DOOR -> Key.key(CopperHelpers.BLOCKY_DOORS.elementAt(index).name.lowercase())
        SetBlock.BlockType.TRAPDOOR -> Key.key(CopperHelpers.BLOCKY_TRAPDOORS.elementAt(index).name.lowercase())
        SetBlock.BlockType.GRATE -> Key.key(CopperHelpers.BLOCKY_GRATE.elementAt(index).name.lowercase())

        else -> Key.key("nothing")
    }

    private fun ModelTextures.Builder.customProperties(prefabKey: PrefabKey): ModelTextures.Builder {
        val entity = prefabKey.toEntityOrNull() ?: return this
        val setBlock = entity.get<SetBlock>() ?: return this
        val info = entity.get<BlockyInfo>() ?: return this

        info.blockTexture?.let {
            val texture = ModelTexture.ofKey(it)
            return variables(
                when (setBlock.blockType) {
                    SetBlock.BlockType.STAIR -> mutableMapOf("bottom" to texture, "top" to texture, "side" to texture)
                    SetBlock.BlockType.SLAB -> mutableMapOf("bottom" to texture, "top" to texture, "side" to texture)
                    SetBlock.BlockType.DOOR -> mutableMapOf("bottom" to texture, "top" to texture)
                    SetBlock.BlockType.TRAPDOOR -> mutableMapOf("texture" to texture)
                    SetBlock.BlockType.GRATE -> mutableMapOf("all" to texture)
                    else -> return this
                }
            )
        }

        return variables(info.blockTextures?.map { it.key to ModelTexture.ofKey(it.value) }?.toMap() ?: return this)
    }

    private fun Variant.Builder.customProperties(prefabKey: PrefabKey): Variant.Builder? {
        val entity = prefabKey.toEntityOrNull() ?: return null
        entity.has<SetBlock>() || return null
        val blockyInfo = entity.get<BlockyInfo>()
        val directional = entity.get<BlockyDirectional>()

        return when {
            directional?.parentBlock?.toEntityOrNull() != null ->
                this.directionalVariant(prefabKey, directional.parentBlock.toEntity())

            directional?.isParentBlock != false && blockyInfo?.blockModel != null ->
                this.model(blockyInfo.blockModel)

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

}
