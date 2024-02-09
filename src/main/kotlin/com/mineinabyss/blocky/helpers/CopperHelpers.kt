package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.components.core.WaxedCopperBlock
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.datastore.has
import com.mineinabyss.geary.papermc.datastore.remove
import org.bukkit.Material
import org.bukkit.block.Block

object CopperHelpers {
    fun isFakeWaxedCopper(block: Block) = (block.type in COPPER_SLABS || block.type in COPPER_STAIRS) && block.persistentDataContainer.has<WaxedCopperBlock>()
    fun setFakeWaxedCopper(block: Block, value: Boolean) = when {
        !value -> block.persistentDataContainer.remove<WaxedCopperBlock>()
        block.type in COPPER_SLABS || block.type in COPPER_STAIRS ->
            block.persistentDataContainer.encode(WaxedCopperBlock)
        else -> {}
    }
    fun isBlockyCopper(block: Block) = block.type in blockyCopperMaterial

    private val blockyCopperMaterial = setOf(
        Material.WAXED_CUT_COPPER_SLAB, Material.WAXED_CUT_COPPER_STAIRS,
        Material.WAXED_EXPOSED_CUT_COPPER_SLAB, Material.WAXED_EXPOSED_CUT_COPPER_STAIRS,
        Material.WAXED_OXIDIZED_CUT_COPPER_SLAB, Material.WAXED_OXIDIZED_CUT_COPPER_STAIRS,
        Material.WAXED_WEATHERED_CUT_COPPER_SLAB, Material.WAXED_WEATHERED_CUT_COPPER_STAIRS,
    )

    private val nonBlockyCopperMaterial = setOf(
        Material.CUT_COPPER_SLAB, Material.CUT_COPPER_STAIRS,
        Material.EXPOSED_CUT_COPPER_SLAB, Material.EXPOSED_CUT_COPPER_STAIRS,
        Material.OXIDIZED_CUT_COPPER_SLAB, Material.OXIDIZED_CUT_COPPER_STAIRS,
        Material.WEATHERED_CUT_COPPER_SLAB, Material.WEATHERED_CUT_COPPER_STAIRS,
    )

    val BLOCKY_SLABS: Set<Material>
        get() = blockyCopperMaterial.filter { "SLAB" in it.name }.toSet()

    val BLOCKY_STAIRS: Set<Material>
        get() = blockyCopperMaterial.filter { "STAIRS" in it.name }.toSet()

    val COPPER_SLABS: Set<Material>
        get() = nonBlockyCopperMaterial.filter { "SLAB" in it.name }.toSet()

    val COPPER_STAIRS: Set<Material>
        get() = nonBlockyCopperMaterial.filter { "STAIRS" in it.name }.toSet()
}
