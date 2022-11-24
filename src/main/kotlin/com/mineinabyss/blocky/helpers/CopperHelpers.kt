package com.mineinabyss.blocky.helpers

import com.jeff_media.morepersistentdatatypes.DataType
import com.mineinabyss.idofront.util.toMCKey
import org.bukkit.Material
import org.bukkit.block.Block

val WAXED_COPPER_KEY = "blocky:waxed_copper".toMCKey()
var Block.isFakeWaxedCopper
    get() = (type in COPPER_SLABS || type in COPPER_STAIRS) && persistentDataContainer.has(WAXED_COPPER_KEY)
    set(value) = when {
        !value -> persistentDataContainer.remove(WAXED_COPPER_KEY)
        type in COPPER_SLABS || type in COPPER_STAIRS ->
            persistentDataContainer.set(WAXED_COPPER_KEY, DataType.BOOLEAN, true)

        else -> {}
    }

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
