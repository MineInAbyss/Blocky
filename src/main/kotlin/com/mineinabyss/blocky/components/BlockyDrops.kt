package com.mineinabyss.blocky.components

import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.Serializable

@Serializable
data class BlockyDrops(
    val item: SerializableItemStack? = null,
    val minAmount: Int = 1,
    val maxAmount: Int = 1,
    val exp: Int = 0,
    val affectedByFortune: Boolean = false,
    val affectedBySilkTouch: Boolean = true,
    val silkTouchedDrop: SerializableItemStack? = null
)
