package com.mineinabyss.blocky.components

import com.mineinabyss.blocky.helpers.getPrefabFromBlock
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.idofront.serialization.SerializableItemStack
import com.mineinabyss.idofront.serialization.toSerializable
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

@Serializable
data class BlockyDrops(
    val item: SerializableItemStack = ItemStack(Material.AIR).toSerializable(),
    val minAmount: Int = 1,
    val maxAmount: Int = 1,
    val exp: Int = 0,
    val affectedByFortune: Boolean = false,
    val affectedBySilkTouch: Boolean = true,
    val silkTouchedDrop: SerializableItemStack = ItemStack(Material.AIR).toSerializable()
)

val GearyEntity.blockyDrops get() = get<BlockyDrops>()
val GearyEntity.hasBlockyDrops get() = has<BlockyDrops>()

val Block.blockyDrops get() = getPrefabFromBlock()?.toEntity()?.get<BlockyDrops>()
val Block.hasBlockyDrops get() = blockyDrops != null