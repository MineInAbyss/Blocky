package com.mineinabyss.blocky.components

import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.Serializable

@Serializable
data class BlockDrops(
    val item: SerializableItemStack,
    val minAmount: Int = 1,
    val maxAmount: Int = 1,
    val exp: Int = 0,
    val affectedByFortune: Boolean = false,
    val affectedBySilkTouch: Boolean = true,
    val silkTouchedDrop: SerializableItemStack
)