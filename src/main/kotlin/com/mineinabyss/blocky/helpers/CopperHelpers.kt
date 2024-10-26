package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.api.BlockyBlocks.gearyBlocks
import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.WaxedCopperBlock
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.datastore.has
import com.mineinabyss.geary.papermc.datastore.remove
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.withGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Door
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Stairs
import org.bukkit.block.data.type.TrapDoor
import org.bukkit.inventory.ItemStack

object CopperHelpers {
    fun isFeatureEnabled(blockData: BlockData): Boolean {
        return when (blockData) {
            is Stairs -> blocky.config.stairBlocks.isEnabled
            is Slab -> blocky.config.slabBlocks.isEnabled
            is Door -> blocky.config.doorBlocks.isEnabled
            is TrapDoor -> blocky.config.trapdoorBlocks.isEnabled
            else -> blocky.config.grateBlocks.isEnabled
                .takeIf { blockData.material in BLOCKY_GRATE.plus(COPPER_GRATE) }
                ?: false
        }
    }

    fun convertToFakeType(itemStack: ItemStack): ItemStack = itemStack.takeIf { itemStack.type in BLOCKY_COPPER }
        ?.takeIf { isFeatureEnabled(it.type.createBlockData()) }
        ?.withType(VANILLA_COPPER.elementAt(BLOCKY_COPPER.indexOf(itemStack.type))) ?: itemStack

    fun isFakeWaxedCopper(block: Block) = (block.type in VANILLA_COPPER) && block.container { has<WaxedCopperBlock>() }
    context(Geary) fun isFakeWaxedCopper(itemStack: ItemStack) =
        itemStack.type.name.contains("WAXED") && itemStack.type.isBlock && !isBlockyCopper(itemStack)

    fun setFakeWaxedCopper(block: Block, value: Boolean) = when {
        !value -> block.container { remove<WaxedCopperBlock>() }
        block.type in VANILLA_COPPER -> block.container { encode(WaxedCopperBlock()) }
        else -> {}
    }

    context(Geary)
    fun convertToBlockyType(itemStack: ItemStack): ItemStack =
        itemStack.toGearyOrNull()?.prefabs?.first()?.get<PrefabKey>()
            ?.let(gearyBlocks::createBlockData)
            ?.takeIf { isFeatureEnabled(it) }
            ?.let { itemStack.withType(it.material) }
            ?: itemStack

    fun isBlockyCopper(block: Block) = block.type in BLOCKY_COPPER
    fun isBlockyCopper(blockData: BlockData) = blockData.material in BLOCKY_COPPER
    fun isBlockyStair(block: Block) =
        block.withGeary { block.blockData is Stairs && block.blockData in gearyBlocks.block2Prefab }

    fun isBlockySlab(block: Block) =
        block.withGeary { block.blockData is Slab && block.blockData in gearyBlocks.block2Prefab }

    fun isBlockyDoor(block: Block) =
        block.withGeary { block.blockData is Door && block.blockData in gearyBlocks.block2Prefab }

    fun isBlockyTrapDoor(block: Block) =
        block.withGeary { block.blockData is TrapDoor && block.blockData in gearyBlocks.block2Prefab }

    fun isBlockyGrate(block: Block) =
        block.withGeary { block.type in BLOCKY_GRATE && block.blockData in gearyBlocks.block2Prefab }

    context(Geary) fun isBlockyCopper(itemStack: ItemStack) =
        isBlockyStair(itemStack) || isBlockySlab(itemStack) || isBlockyDoor(itemStack) || isBlockyTrapDoor(itemStack) || isBlockyGrate(
            itemStack
        )

    context(Geary) fun isBlockyStair(itemStack: ItemStack) =
        itemStack.decode<SetBlock>()?.blockType == SetBlock.BlockType.STAIR

    context(Geary) fun isBlockySlab(itemStack: ItemStack) =
        itemStack.decode<SetBlock>()?.blockType == SetBlock.BlockType.SLAB

    context(Geary) fun isBlockyDoor(itemStack: ItemStack) =
        itemStack.decode<SetBlock>()?.blockType == SetBlock.BlockType.DOOR

    context(Geary) fun isBlockyTrapDoor(itemStack: ItemStack) =
        itemStack.decode<SetBlock>()?.blockType == SetBlock.BlockType.TRAPDOOR

    context(Geary) fun isBlockyGrate(itemStack: ItemStack) =
        itemStack.decode<SetBlock>()?.blockType == SetBlock.BlockType.GRATE

    val BLOCKY_COPPER = setOf(
        Material.WAXED_CUT_COPPER_SLAB,
        Material.WAXED_CUT_COPPER_STAIRS,
        Material.WAXED_EXPOSED_CUT_COPPER_SLAB,
        Material.WAXED_EXPOSED_CUT_COPPER_STAIRS,
        Material.WAXED_OXIDIZED_CUT_COPPER_SLAB,
        Material.WAXED_OXIDIZED_CUT_COPPER_STAIRS,
        Material.WAXED_WEATHERED_CUT_COPPER_SLAB,
        Material.WAXED_WEATHERED_CUT_COPPER_STAIRS,
        Material.WAXED_COPPER_DOOR,
        Material.WAXED_COPPER_TRAPDOOR,
        Material.WAXED_COPPER_GRATE,
        Material.WAXED_EXPOSED_COPPER_DOOR,
        Material.WAXED_EXPOSED_COPPER_TRAPDOOR,
        Material.WAXED_EXPOSED_COPPER_GRATE,
        Material.WAXED_OXIDIZED_COPPER_DOOR,
        Material.WAXED_OXIDIZED_COPPER_TRAPDOOR,
        Material.WAXED_OXIDIZED_COPPER_GRATE,
        Material.WAXED_WEATHERED_COPPER_DOOR,
        Material.WAXED_WEATHERED_COPPER_TRAPDOOR,
        Material.WAXED_WEATHERED_COPPER_GRATE
    )

    val VANILLA_COPPER = setOf(
        Material.CUT_COPPER_SLAB, Material.CUT_COPPER_STAIRS,
        Material.EXPOSED_CUT_COPPER_SLAB, Material.EXPOSED_CUT_COPPER_STAIRS,
        Material.OXIDIZED_CUT_COPPER_SLAB, Material.OXIDIZED_CUT_COPPER_STAIRS,
        Material.WEATHERED_CUT_COPPER_SLAB, Material.WEATHERED_CUT_COPPER_STAIRS,
        Material.COPPER_DOOR, Material.COPPER_TRAPDOOR, Material.COPPER_GRATE,
        Material.EXPOSED_COPPER_DOOR, Material.EXPOSED_COPPER_TRAPDOOR, Material.EXPOSED_COPPER_GRATE,
        Material.OXIDIZED_COPPER_DOOR, Material.OXIDIZED_COPPER_TRAPDOOR, Material.OXIDIZED_COPPER_GRATE,
        Material.WEATHERED_COPPER_DOOR, Material.WEATHERED_COPPER_TRAPDOOR, Material.WEATHERED_COPPER_GRATE
    )

    val BLOCKY_SLABS = BLOCKY_COPPER.filter { it.name.endsWith("_SLAB") }.toSet()
    val BLOCKY_STAIRS = BLOCKY_COPPER.filter { it.name.endsWith("_STAIRS") }.toSet()
    val BLOCKY_DOORS = BLOCKY_COPPER.filter { it.name.endsWith("_DOOR") }.toSet()
    val BLOCKY_TRAPDOORS = BLOCKY_COPPER.filter { it.name.endsWith("_TRAPDOOR") }.toSet()
    val BLOCKY_GRATE = BLOCKY_COPPER.filter { it.name.endsWith("_GRATE") }.toSet()

    val COPPER_SLABS = VANILLA_COPPER.filter { it.name.endsWith("_SLAB") }.toSet()
    val COPPER_STAIRS = VANILLA_COPPER.filter { it.name.endsWith("_STAIRS") }.toSet()
    val COPPER_DOORS = VANILLA_COPPER.filter { it.name.endsWith("_DOOR") }.toSet()
    val COPPER_TRAPDOORS = VANILLA_COPPER.filter { it.name.endsWith("_TRAPDOOR") }.toSet()
    val COPPER_GRATE = VANILLA_COPPER.filter { it.name.endsWith("_GRATE") }.toSet()
}
