package com.mineinabyss.blocky.assets_generation

import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import net.kyori.adventure.key.Key
import org.bukkit.Material
import team.unnamed.creative.blockstate.BlockState
import team.unnamed.creative.blockstate.MultiVariant
import team.unnamed.creative.blockstate.Selector
import team.unnamed.creative.blockstate.Variant
import team.unnamed.creative.serialize.minecraft.blockstate.BlockStateSerializer

object VanillaBlockstateFiles {

    fun vanillaBlockStateFile(blockType: SetBlock.BlockType, material: Material): BlockState? {
        val key = material.key()
        val (variants, multipart) = when (blockType) {
            SetBlock.BlockType.STAIR -> COPPER_STAIRS.variants() to COPPER_STAIRS.multipart()
            SetBlock.BlockType.SLAB -> COPPER_SLABS.variants() to COPPER_SLABS.multipart()
            SetBlock.BlockType.DOOR -> COPPER_DOOR.variants() to COPPER_DOOR.multipart()
            SetBlock.BlockType.TRAPDOOR -> COPPER_TRAPDOOR.variants() to COPPER_TRAPDOOR.multipart()
            SetBlock.BlockType.GRATE -> COPPER_GRATE.variants() to COPPER_GRATE.multipart()
            else -> return null
        }.let { it.first.replaceModel(material) to it.second.replaceModel(material) }

        return BlockState.of(key, variants, multipart)
    }

    private fun Map<String, MultiVariant>.replaceModel(material: Material) =
        map { it.key to it.value.let { mv -> MultiVariant.of(mv.variants().map { v -> v.replaceModel(material) }) } }.toMap()

    private fun List<Selector>.replaceModel(material: Material) =
        map { Selector.of(it.condition(), MultiVariant.of(it.variant().variants().map { v -> v.replaceModel(material) })) }

    private fun Variant.replaceModel(material: Material): Variant {
        // blocky:block/waxed_cut_copper_stairs -> ""
        // blocky:block/waxed_cut_copper_stairs_inner -> "inner"
        // blocky:block/waxed_cut_copper_slab -> ""
        // blocky:block/waxed_cut_copper -> ""
        val modelSuffix = model().asString().substringAfter("copper").substringAfter("_", "").substringAfter("_", "")

        return Variant.builder()
            .model(Key.key("blocky", "block/" + material.name.lowercase() + ("_$modelSuffix".takeIf { modelSuffix.isNotEmpty() } ?: "")))
            .x(this.x()).y(this.y()).weight(this.weight()).uvLock(this.uvLock()).build()
    }

    private val COPPER_STAIRS = BlockStateSerializer.INSTANCE.deserializeFromJsonString(
        """
        {
          "variants": {
            "facing=east,half=bottom,shape=inner_left": {
              "model": "minecraft:block/cut_copper_stairs_inner",
              "uvlock": true,
              "y": 270
            },
            "facing=east,half=bottom,shape=inner_right": {
              "model": "minecraft:block/cut_copper_stairs_inner"
            },
            "facing=east,half=bottom,shape=outer_left": {
              "model": "minecraft:block/cut_copper_stairs_outer",
              "uvlock": true,
              "y": 270
            },
            "facing=east,half=bottom,shape=outer_right": {
              "model": "minecraft:block/cut_copper_stairs_outer"
            },
            "facing=east,half=bottom,shape=straight": {
              "model": "minecraft:block/cut_copper_stairs"
            },
            "facing=east,half=top,shape=inner_left": {
              "model": "minecraft:block/cut_copper_stairs_inner",
              "uvlock": true,
              "x": 180
            },
            "facing=east,half=top,shape=inner_right": {
              "model": "minecraft:block/cut_copper_stairs_inner",
              "uvlock": true,
              "x": 180,
              "y": 90
            },
            "facing=east,half=top,shape=outer_left": {
              "model": "minecraft:block/cut_copper_stairs_outer",
              "uvlock": true,
              "x": 180
            },
            "facing=east,half=top,shape=outer_right": {
              "model": "minecraft:block/cut_copper_stairs_outer",
              "uvlock": true,
              "x": 180,
              "y": 90
            },
            "facing=east,half=top,shape=straight": {
              "model": "minecraft:block/cut_copper_stairs",
              "uvlock": true,
              "x": 180
            },
            "facing=north,half=bottom,shape=inner_left": {
              "model": "minecraft:block/cut_copper_stairs_inner",
              "uvlock": true,
              "y": 180
            },
            "facing=north,half=bottom,shape=inner_right": {
              "model": "minecraft:block/cut_copper_stairs_inner",
              "uvlock": true,
              "y": 270
            },
            "facing=north,half=bottom,shape=outer_left": {
              "model": "minecraft:block/cut_copper_stairs_outer",
              "uvlock": true,
              "y": 180
            },
            "facing=north,half=bottom,shape=outer_right": {
              "model": "minecraft:block/cut_copper_stairs_outer",
              "uvlock": true,
              "y": 270
            },
            "facing=north,half=bottom,shape=straight": {
              "model": "minecraft:block/cut_copper_stairs",
              "uvlock": true,
              "y": 270
            },
            "facing=north,half=top,shape=inner_left": {
              "model": "minecraft:block/cut_copper_stairs_inner",
              "uvlock": true,
              "x": 180,
              "y": 270
            },
            "facing=north,half=top,shape=inner_right": {
              "model": "minecraft:block/cut_copper_stairs_inner",
              "uvlock": true,
              "x": 180
            },
            "facing=north,half=top,shape=outer_left": {
              "model": "minecraft:block/cut_copper_stairs_outer",
              "uvlock": true,
              "x": 180,
              "y": 270
            },
            "facing=north,half=top,shape=outer_right": {
              "model": "minecraft:block/cut_copper_stairs_outer",
              "uvlock": true,
              "x": 180
            },
            "facing=north,half=top,shape=straight": {
              "model": "minecraft:block/cut_copper_stairs",
              "uvlock": true,
              "x": 180,
              "y": 270
            },
            "facing=south,half=bottom,shape=inner_left": {
              "model": "minecraft:block/cut_copper_stairs_inner"
            },
            "facing=south,half=bottom,shape=inner_right": {
              "model": "minecraft:block/cut_copper_stairs_inner",
              "uvlock": true,
              "y": 90
            },
            "facing=south,half=bottom,shape=outer_left": {
              "model": "minecraft:block/cut_copper_stairs_outer"
            },
            "facing=south,half=bottom,shape=outer_right": {
              "model": "minecraft:block/cut_copper_stairs_outer",
              "uvlock": true,
              "y": 90
            },
            "facing=south,half=bottom,shape=straight": {
              "model": "minecraft:block/cut_copper_stairs",
              "uvlock": true,
              "y": 90
            },
            "facing=south,half=top,shape=inner_left": {
              "model": "minecraft:block/cut_copper_stairs_inner",
              "uvlock": true,
              "x": 180,
              "y": 90
            },
            "facing=south,half=top,shape=inner_right": {
              "model": "minecraft:block/cut_copper_stairs_inner",
              "uvlock": true,
              "x": 180,
              "y": 180
            },
            "facing=south,half=top,shape=outer_left": {
              "model": "minecraft:block/cut_copper_stairs_outer",
              "uvlock": true,
              "x": 180,
              "y": 90
            },
            "facing=south,half=top,shape=outer_right": {
              "model": "minecraft:block/cut_copper_stairs_outer",
              "uvlock": true,
              "x": 180,
              "y": 180
            },
            "facing=south,half=top,shape=straight": {
              "model": "minecraft:block/cut_copper_stairs",
              "uvlock": true,
              "x": 180,
              "y": 90
            },
            "facing=west,half=bottom,shape=inner_left": {
              "model": "minecraft:block/cut_copper_stairs_inner",
              "uvlock": true,
              "y": 90
            },
            "facing=west,half=bottom,shape=inner_right": {
              "model": "minecraft:block/cut_copper_stairs_inner",
              "uvlock": true,
              "y": 180
            },
            "facing=west,half=bottom,shape=outer_left": {
              "model": "minecraft:block/cut_copper_stairs_outer",
              "uvlock": true,
              "y": 90
            },
            "facing=west,half=bottom,shape=outer_right": {
              "model": "minecraft:block/cut_copper_stairs_outer",
              "uvlock": true,
              "y": 180
            },
            "facing=west,half=bottom,shape=straight": {
              "model": "minecraft:block/cut_copper_stairs",
              "uvlock": true,
              "y": 180
            },
            "facing=west,half=top,shape=inner_left": {
              "model": "minecraft:block/cut_copper_stairs_inner",
              "uvlock": true,
              "x": 180,
              "y": 180
            },
            "facing=west,half=top,shape=inner_right": {
              "model": "minecraft:block/cut_copper_stairs_inner",
              "uvlock": true,
              "x": 180,
              "y": 270
            },
            "facing=west,half=top,shape=outer_left": {
              "model": "minecraft:block/cut_copper_stairs_outer",
              "uvlock": true,
              "x": 180,
              "y": 180
            },
            "facing=west,half=top,shape=outer_right": {
              "model": "minecraft:block/cut_copper_stairs_outer",
              "uvlock": true,
              "x": 180,
              "y": 270
            },
            "facing=west,half=top,shape=straight": {
              "model": "minecraft:block/cut_copper_stairs",
              "uvlock": true,
              "x": 180,
              "y": 180
            }
          }
        }
    """.trimIndent(), Key.key("stairs")
    )
    private val COPPER_SLABS = BlockStateSerializer.INSTANCE.deserializeFromJsonString(
        """
        {
          "variants": {
            "type=bottom": {
              "model": "minecraft:block/cut_copper_slab"
            },
            "type=double": {
              "model": "minecraft:block/cut_copper"
            },
            "type=top": {
              "model": "minecraft:block/cut_copper_slab_top"
            }
          }
        }
    """.trimIndent(), Key.key("slabs")
    )
    private val COPPER_DOOR = BlockStateSerializer.INSTANCE.deserializeFromJsonString(
        """
        {
          "variants": {
            "facing=east,half=lower,hinge=left,open=false": {
              "model": "minecraft:block/copper_door_bottom_left"
            },
            "facing=east,half=lower,hinge=left,open=true": {
              "model": "minecraft:block/copper_door_bottom_left_open",
              "y": 90
            },
            "facing=east,half=lower,hinge=right,open=false": {
              "model": "minecraft:block/copper_door_bottom_right"
            },
            "facing=east,half=lower,hinge=right,open=true": {
              "model": "minecraft:block/copper_door_bottom_right_open",
              "y": 270
            },
            "facing=east,half=upper,hinge=left,open=false": {
              "model": "minecraft:block/copper_door_top_left"
            },
            "facing=east,half=upper,hinge=left,open=true": {
              "model": "minecraft:block/copper_door_top_left_open",
              "y": 90
            },
            "facing=east,half=upper,hinge=right,open=false": {
              "model": "minecraft:block/copper_door_top_right"
            },
            "facing=east,half=upper,hinge=right,open=true": {
              "model": "minecraft:block/copper_door_top_right_open",
              "y": 270
            },
            "facing=north,half=lower,hinge=left,open=false": {
              "model": "minecraft:block/copper_door_bottom_left",
              "y": 270
            },
            "facing=north,half=lower,hinge=left,open=true": {
              "model": "minecraft:block/copper_door_bottom_left_open"
            },
            "facing=north,half=lower,hinge=right,open=false": {
              "model": "minecraft:block/copper_door_bottom_right",
              "y": 270
            },
            "facing=north,half=lower,hinge=right,open=true": {
              "model": "minecraft:block/copper_door_bottom_right_open",
              "y": 180
            },
            "facing=north,half=upper,hinge=left,open=false": {
              "model": "minecraft:block/copper_door_top_left",
              "y": 270
            },
            "facing=north,half=upper,hinge=left,open=true": {
              "model": "minecraft:block/copper_door_top_left_open"
            },
            "facing=north,half=upper,hinge=right,open=false": {
              "model": "minecraft:block/copper_door_top_right",
              "y": 270
            },
            "facing=north,half=upper,hinge=right,open=true": {
              "model": "minecraft:block/copper_door_top_right_open",
              "y": 180
            },
            "facing=south,half=lower,hinge=left,open=false": {
              "model": "minecraft:block/copper_door_bottom_left",
              "y": 90
            },
            "facing=south,half=lower,hinge=left,open=true": {
              "model": "minecraft:block/copper_door_bottom_left_open",
              "y": 180
            },
            "facing=south,half=lower,hinge=right,open=false": {
              "model": "minecraft:block/copper_door_bottom_right",
              "y": 90
            },
            "facing=south,half=lower,hinge=right,open=true": {
              "model": "minecraft:block/copper_door_bottom_right_open"
            },
            "facing=south,half=upper,hinge=left,open=false": {
              "model": "minecraft:block/copper_door_top_left",
              "y": 90
            },
            "facing=south,half=upper,hinge=left,open=true": {
              "model": "minecraft:block/copper_door_top_left_open",
              "y": 180
            },
            "facing=south,half=upper,hinge=right,open=false": {
              "model": "minecraft:block/copper_door_top_right",
              "y": 90
            },
            "facing=south,half=upper,hinge=right,open=true": {
              "model": "minecraft:block/copper_door_top_right_open"
            },
            "facing=west,half=lower,hinge=left,open=false": {
              "model": "minecraft:block/copper_door_bottom_left",
              "y": 180
            },
            "facing=west,half=lower,hinge=left,open=true": {
              "model": "minecraft:block/copper_door_bottom_left_open",
              "y": 270
            },
            "facing=west,half=lower,hinge=right,open=false": {
              "model": "minecraft:block/copper_door_bottom_right",
              "y": 180
            },
            "facing=west,half=lower,hinge=right,open=true": {
              "model": "minecraft:block/copper_door_bottom_right_open",
              "y": 90
            },
            "facing=west,half=upper,hinge=left,open=false": {
              "model": "minecraft:block/copper_door_top_left",
              "y": 180
            },
            "facing=west,half=upper,hinge=left,open=true": {
              "model": "minecraft:block/copper_door_top_left_open",
              "y": 270
            },
            "facing=west,half=upper,hinge=right,open=false": {
              "model": "minecraft:block/copper_door_top_right",
              "y": 180
            },
            "facing=west,half=upper,hinge=right,open=true": {
              "model": "minecraft:block/copper_door_top_right_open",
              "y": 90
            }
          }
        }
    """.trimIndent(), Key.key("door")
    )
    private val COPPER_TRAPDOOR = BlockStateSerializer.INSTANCE.deserializeFromJsonString(
        """
        {
          "variants": {
            "facing=east,half=bottom,open=false": {
              "model": "minecraft:block/copper_trapdoor_bottom"
            },
            "facing=east,half=bottom,open=true": {
              "model": "minecraft:block/copper_trapdoor_open",
              "y": 90
            },
            "facing=east,half=top,open=false": {
              "model": "minecraft:block/copper_trapdoor_top"
            },
            "facing=east,half=top,open=true": {
              "model": "minecraft:block/copper_trapdoor_open",
              "y": 90
            },
            "facing=north,half=bottom,open=false": {
              "model": "minecraft:block/copper_trapdoor_bottom"
            },
            "facing=north,half=bottom,open=true": {
              "model": "minecraft:block/copper_trapdoor_open"
            },
            "facing=north,half=top,open=false": {
              "model": "minecraft:block/copper_trapdoor_top"
            },
            "facing=north,half=top,open=true": {
              "model": "minecraft:block/copper_trapdoor_open"
            },
            "facing=south,half=bottom,open=false": {
              "model": "minecraft:block/copper_trapdoor_bottom"
            },
            "facing=south,half=bottom,open=true": {
              "model": "minecraft:block/copper_trapdoor_open",
              "y": 180
            },
            "facing=south,half=top,open=false": {
              "model": "minecraft:block/copper_trapdoor_top"
            },
            "facing=south,half=top,open=true": {
              "model": "minecraft:block/copper_trapdoor_open",
              "y": 180
            },
            "facing=west,half=bottom,open=false": {
              "model": "minecraft:block/copper_trapdoor_bottom"
            },
            "facing=west,half=bottom,open=true": {
              "model": "minecraft:block/copper_trapdoor_open",
              "y": 270
            },
            "facing=west,half=top,open=false": {
              "model": "minecraft:block/copper_trapdoor_top"
            },
            "facing=west,half=top,open=true": {
              "model": "minecraft:block/copper_trapdoor_open",
              "y": 270
            }
          }
        }
    """.trimIndent(), Key.key("trapdoor")
    )
    private val COPPER_GRATE = BlockStateSerializer.INSTANCE.deserializeFromJsonString(
        """
        {
          "variants": {
            "": {
              "model": "minecraft:block/copper_grate"
            }
          }
        }
    """.trimIndent(), Key.key("grate")
    )
}