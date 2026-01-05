package com.mineinabyss.blocky.assets_generation

import com.mineinabyss.blocky.api.BlockyBlocks.gearyBlocks
import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.BlockyPack
import com.mineinabyss.blocky.components.features.blocks.BlockyDirectional
import com.mineinabyss.blocky.helpers.CopperHelpers
import com.mineinabyss.blocky.systems.blockPrefabs
import com.mineinabyss.blocky.systems.plantPrefabs
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.toEntityOrNull
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.prefabs.PrefabKey
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.blockstate.BlockState
import team.unnamed.creative.blockstate.MultiVariant
import team.unnamed.creative.blockstate.Variant
import team.unnamed.creative.model.Model
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackWriter

class ResourcepackGeneration(
    geary: Geary
): Geary by geary {
    private val resourcePack = ResourcePack.resourcePack()
    private val BlockData.propertiesAsString get() = this.asString.substringAfter("[").substringBeforeLast("]")


    fun generateDefaultAssets() {
        generateBlockstateFiles()
        VanillaSoundEntries.registerRequiredSounds(resourcePack)

        MinecraftResourcePackWriter.minecraft().writeToDirectory(blocky.plugin.dataFolder.resolve("pack"), resourcePack)
    }

    private fun generateBlockstateFiles() {
        blockState(SetBlock.BlockType.NOTEBLOCK)?.addTo(resourcePack)
        blockState(SetBlock.BlockType.WIRE)?.addTo(resourcePack)
        blockState(SetBlock.BlockType.CAVEVINE)?.addTo(resourcePack)
        copperBlockStates()
    }

    /**
     * Since Copper-based blocks use a different material per "block" compared to just a state.<br>
     * The logic for generating the blockstate is heavily altered and thus split into a separate method
     */
    private fun copperBlockStates() {

        fun handleCopperModel(blockType: SetBlock.BlockType, material: Material, index: Int, parent: String, suffix: String) {
            val query = blockPrefabs.find { it.block.blockType == blockType && it.block.blockId == index + 1 } ?: return
            val blockyPack = query.prefabKey.toEntityOrNull()?.get<BlockyPack>() ?: return
            val model = Model.model().textures(blockyPack.modelTextures).parent(Key.key(parent))
                .key(Key.key("blocky:block/${material.name.lowercase()}$suffix")).build()

            model.addTo(resourcePack)
        }

        fun handleCopperBlockstate(blockType: SetBlock.BlockType, material: Material, index: Int) {
            blockPrefabs.find { it.block.blockType == blockType && it.block.blockId == index + 1 } ?: return
            VanillaBlockstateFiles.vanillaBlockStateFile(blockType, material)?.addTo(resourcePack)
        }

        CopperHelpers.BLOCKY_STAIRS.forEachIndexed { index, material ->
            handleCopperBlockstate(SetBlock.BlockType.STAIR, material, index)
            handleCopperModel(SetBlock.BlockType.STAIR, material, index, "block/stairs", "")
            handleCopperModel(SetBlock.BlockType.STAIR, material, index, "block/inner_stairs", "_inner")
            handleCopperModel(SetBlock.BlockType.STAIR, material, index, "block/outer_stairs", "_outer")
        }

        CopperHelpers.BLOCKY_SLABS.forEachIndexed { index, material ->
            handleCopperBlockstate(SetBlock.BlockType.SLAB, material, index)
            handleCopperModel(SetBlock.BlockType.SLAB, material, index, "block/slab", "")
            handleCopperModel(SetBlock.BlockType.SLAB, material, index, "block/slab_top", "_top")
            handleCopperModel(SetBlock.BlockType.SLAB, material, index, "block/cube_all", "")
        }

        CopperHelpers.BLOCKY_DOORS.forEachIndexed { index, material ->
            handleCopperBlockstate(SetBlock.BlockType.DOOR, material, index)
            handleCopperModel(SetBlock.BlockType.DOOR, material, index, "block/door_bottom_left", "_bottom_left")
            handleCopperModel(SetBlock.BlockType.DOOR, material, index, "block/door_bottom_left_open", "_bottom_left_open")
            handleCopperModel(SetBlock.BlockType.DOOR, material, index, "block/door_bottom_right", "_bottom_right")
            handleCopperModel(SetBlock.BlockType.DOOR, material, index, "block/door_bottom_right_open", "_bottom_right_open")
            handleCopperModel(SetBlock.BlockType.DOOR, material, index, "block/door_top_left", "_top_left")
            handleCopperModel(SetBlock.BlockType.DOOR, material, index, "block/door_top_left_open", "_top_left_open")
            handleCopperModel(SetBlock.BlockType.DOOR, material, index, "block/door_top_right", "_top_right")
            handleCopperModel(SetBlock.BlockType.DOOR, material, index, "block/door_top_right_open", "_top_right_open")
        }

        CopperHelpers.BLOCKY_TRAPDOORS.forEachIndexed { index, material ->
            handleCopperBlockstate(SetBlock.BlockType.TRAPDOOR, material, index)
            handleCopperModel(SetBlock.BlockType.TRAPDOOR, material, index, "block/template_trapdoor_bottom", "_bottom")
            handleCopperModel(SetBlock.BlockType.TRAPDOOR, material, index, "block/template_trapdoor_open", "_open")
            handleCopperModel(SetBlock.BlockType.TRAPDOOR, material, index, "block/template_trapdoor_top", "_top")
        }

        CopperHelpers.BLOCKY_GRATE.forEachIndexed { index, material ->
            handleCopperBlockstate(SetBlock.BlockType.GRATE, material, index)
            handleCopperModel(SetBlock.BlockType.GRATE, material, index, "block/cube_all", "")
        }
    }

    private fun blockState(blockType: SetBlock.BlockType): BlockState? {
        val multiVariant = mutableMapOf<String, MultiVariant>()

        // Add the vanilla block to the blockstate file
        val (vanillaMaterial, vanillaVariant) = when (blockType) {
            SetBlock.BlockType.NOTEBLOCK -> Material.NOTE_BLOCK to (Key.key("block/note_block"))
            SetBlock.BlockType.WIRE -> Material.TRIPWIRE to Key.key("block/barrier")
            SetBlock.BlockType.CAVEVINE -> Material.CAVE_VINES to Key.key("block/cave_vines")
            SetBlock.BlockType.STAIR, SetBlock.BlockType.SLAB, SetBlock.BlockType.DOOR, SetBlock.BlockType.TRAPDOOR, SetBlock.BlockType.GRATE -> return null
        }

        multiVariant[vanillaMaterial.createBlockData().propertiesAsString] =
            MultiVariant.of(Variant.builder().model(vanillaVariant).build())

        multiVariant += blockType.multiVariant()

        val blockstateKey = Key.key(when (blockType) {
            SetBlock.BlockType.NOTEBLOCK -> "note_block"
            SetBlock.BlockType.WIRE -> "tripwire"
            SetBlock.BlockType.CAVEVINE -> "cave_vines"
            else -> return null
        })

        return BlockState.of(blockstateKey, multiVariant)
    }

    private val blockDataPrefabs = blockPrefabs.associate { it.prefabKey to gearyBlocks.createBlockData(it.prefabKey) }
        .plus(plantPrefabs.map { it.prefabKey to gearyBlocks.createBlockData(it.prefabKey) })
    private fun SetBlock.BlockType.multiVariant() =
        gearyBlocks.block2Prefab.blockMap[this]?.mapNotNull { blockData ->
            val prefabKey = blockDataPrefabs.entries.find { it.value == blockData }?.key ?: return@mapNotNull null
            val variant = MultiVariant.of(Variant.builder().customProperties(prefabKey)?.build()) ?: return@mapNotNull null
            blockData.propertiesAsString to variant
        }?.toMap()?.toMutableMap() ?: mutableMapOf()

    private fun Variant.Builder.customProperties(prefabKey: PrefabKey): Variant.Builder? {
        val entity = prefabKey.toEntityOrNull() ?: return null
        entity.has<SetBlock>() || return null
        val blockyPack = entity.get<BlockyPack>()
        val directional = entity.get<BlockyDirectional>()

        directional?.parentBlock?.toEntityOrNull()?.let { return directionalVariant(prefabKey, entity, it) }

        return when {
            directional?.isParentBlock != false && blockyPack?.model != null ->
                this.model(blockyPack.model)

            blockyPack != null -> this.model(Model.model().textures(blockyPack.modelTextures).parent(blockyPack.parentModel)
                .key(Key.key("blocky:block/${prefabKey.key}")).build().also { it.addTo(resourcePack) }.key())
            else -> return null
        }
    }

    private fun Variant.Builder.directionalVariant(prefabKey: PrefabKey, child: GearyEntity, parent: GearyEntity): Variant.Builder {
        val childModel = child.get<BlockyPack>()?.model
        val parentModel = parent.get<BlockyPack>()?.model

        return this.model(childModel ?: parentModel ?: Key.key("minecraft:block/note_block")).also {
            val parentBlock = parent.get<BlockyDirectional>() ?: return@also
            if (childModel == null || childModel == parentModel) when (prefabKey) {
                parentBlock.zBlock, parentBlock.eastBlock -> {
                    it.x(90)
                    it.y(90)
                }

                parentBlock.xBlock -> it.x(90)
                parentBlock.southBlock -> it.y(180)
                parentBlock.westBlock, parentBlock.upBlock -> it.y(270)
                parentBlock.downBlock -> it.x(180)
                else -> {}
            }
        }
    }

}
