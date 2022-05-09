package com.mineinabyss.blocky.components

import com.mineinabyss.idofront.serialization.SerializableItemStack
import com.mineinabyss.idofront.serialization.toSerializable
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

@Serializable
data class BlockDrops(
    val item: SerializableItemStack = ItemStack(Material.AIR).toSerializable(),
    val minAmount: Int = 1,
    val maxAmount: Int = 1,
    val exp: Int = 0,
    val affectedByFortune: Boolean = false,
    val affectedBySilkTouch: Boolean = true,
    val silkTouchedDrop: SerializableItemStack = ItemStack(Material.AIR).toSerializable()
)