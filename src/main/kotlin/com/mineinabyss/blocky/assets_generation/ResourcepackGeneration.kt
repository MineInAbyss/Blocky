package com.mineinabyss.blocky.assets_generation

import com.mineinabyss.blocky.assets_generation.ResourcepackGeneration.multiVariant
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
import com.mineinabyss.idofront.messaging.broadcastVal
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
        handleCopperModelOverrides()

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
    private fun handleCopperModelOverrides() {
        CopperHelpers.COPPER_STAIRS.forEachIndexed { index, material ->
            val query = blockPrefabs.find { it.block.blockType == SetBlock.BlockType.STAIR && it.block.blockId == index + 1 } ?: return@forEachIndexed
            val model = Model.model().textures(ModelTextures.builder().properties(query.prefabKey)?.build() ?:return@forEachIndexed)
            val prefix = "block/${material.name.lowercase()}"

            resourcePack.model(model.parent(Key.key("block/stairs")).key(Key.key(prefix)).build())
            resourcePack.model(model.parent(Key.key("block/inner_stairs")).key(Key.key("${prefix}_inner")).build())
            resourcePack.model(model.parent(Key.key("block/outer_stairs")).key(Key.key("${prefix}_outer")).build())
        }

        CopperHelpers.COPPER_SLABS.forEachIndexed { index, material ->
            val query = blockPrefabs.find { it.block.blockType == SetBlock.BlockType.SLAB && it.block.blockId == index + 1 } ?: return@forEachIndexed
            val model = Model.model().textures(ModelTextures.builder().properties(query.prefabKey)?.build() ?:return@forEachIndexed)
            val prefix = "block/${material.name.lowercase()}"

            resourcePack.model(model.parent(Key.key("block/slab")).key(Key.key(prefix)).build())
            resourcePack.model(model.parent(Key.key("block/slab_top")).key(Key.key("${prefix}_top")).build())
        }

        CopperHelpers.COPPER_DOORS.forEachIndexed { index, material ->
            val query = blockPrefabs.find { it.block.blockType == SetBlock.BlockType.DOOR && it.block.blockId == index + 1 } ?: return@forEachIndexed
            val model = Model.model().textures(ModelTextures.builder().properties(query.prefabKey)?.build() ?:return@forEachIndexed)
            val prefix = "block/${material.name.lowercase()}"

            resourcePack.model(model.parent(Key.key("block/door_bottom_left")).key(Key.key("${prefix}_bottom_left")).build())
            resourcePack.model(model.parent(Key.key("block/door_bottom_left_open")).key(Key.key("${prefix}_bottom_left_open")).build())
            resourcePack.model(model.parent(Key.key("block/door_bottom_right")).key(Key.key("${prefix}_bottom_right")).build())
            resourcePack.model(model.parent(Key.key("block/door_bottom_right_open")).key(Key.key("${prefix}_bottom_right_open")).build())
            resourcePack.model(model.parent(Key.key("block/door_top_left")).key(Key.key("${prefix}_top_left")).build())
            resourcePack.model(model.parent(Key.key("block/door_top_left_open")).key(Key.key("${prefix}_top_left_open")).build())
            resourcePack.model(model.parent(Key.key("block/door_top_right")).key(Key.key("${prefix}_top_right")).build())
            resourcePack.model(model.parent(Key.key("block/door_top_right_open")).key(Key.key("${prefix}_top_right_open")).build())
        }

        CopperHelpers.COPPER_TRAPDOORS.forEachIndexed { index, material ->
            val query = blockPrefabs.find { it.block.blockType == SetBlock.BlockType.TRAPDOOR && it.block.blockId == index + 1 } ?: return@forEachIndexed
            val model = Model.model().textures(ModelTextures.builder().properties(query.prefabKey)?.build() ?:return@forEachIndexed)
            val prefix = "block/${material.name.lowercase()}"
            val parentPrefix = "block/template_trapdoor"

            resourcePack.model(model.parent(Key.key("${parentPrefix}_bottom")).key(Key.key("${prefix}_bottom")).build())
            resourcePack.model(model.parent(Key.key("${parentPrefix}_open")).key(Key.key("${prefix}_open")).build())
            resourcePack.model(model.parent(Key.key("${parentPrefix}_top")).key(Key.key("${prefix}_top")).build())
        }

        CopperHelpers.COPPER_GRATE.forEachIndexed { index, material ->
            val query = blockPrefabs.find { it.block.blockType == SetBlock.BlockType.GRATE && it.block.blockId == index + 1 } ?: return@forEachIndexed
            val model = Model.model().textures(ModelTextures.builder().properties(query.prefabKey)?.build() ?:return@forEachIndexed)
            val key = Key.key("block/${material.name.lowercase()}")

            resourcePack.model(model.parent(Key.key("block/cube_all")).key(key).build())
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
            val variant = MultiVariant.of(Variant.builder().properties(query.prefabKey)?.build()) ?: return@mapIndexed null
            blockData.propertiesAsString to variant
        }?.filterNotNull()?.toMap()?.toMutableMap() ?: mutableMapOf()

    private fun SetBlock.BlockType.blockStateKey() = when (this) {
        SetBlock.BlockType.NOTEBLOCK -> Key.key("note_block")
        SetBlock.BlockType.WIRE -> Key.key("tripwire")

        else -> Key.key("nothing")
    }

    private fun ModelTextures.Builder.properties(prefabKey: PrefabKey): ModelTextures.Builder? {
        val entity = prefabKey.toEntityOrNull() ?: return this
        val setBlock = entity.get<SetBlock>() ?: return this
        val texture = entity.get<BlockyInfo>()?.blockTexture ?: return null

        return variables(
            when (setBlock.blockType) {
                SetBlock.BlockType.STAIR, SetBlock.BlockType.SLAB -> mutableMapOf(
                    "bottom" to ModelTexture.ofKey(texture),
                    "top" to ModelTexture.ofKey(texture),
                    "side" to ModelTexture.ofKey(texture),
                )
                SetBlock.BlockType.DOOR -> mutableMapOf(
                    "bottom" to ModelTexture.ofKey(texture),
                    "top" to ModelTexture.ofKey(texture),
                )
                SetBlock.BlockType.TRAPDOOR -> mutableMapOf("texture" to ModelTexture.ofKey(texture))
                SetBlock.BlockType.GRATE -> mutableMapOf("all" to ModelTexture.ofKey(texture))
                else -> return this
            }
        )
    }

    private fun Variant.Builder.properties(prefabKey: PrefabKey): Variant.Builder? {
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
