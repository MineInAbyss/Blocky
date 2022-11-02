package com.mineinabyss.blocky.components.features

import com.mineinabyss.idofront.serialization.IntRangeSerializer
import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.Serializable

@Serializable
data class BlockyDrops(
    val item: SerializableItemStack? = null,
    val amount: @Serializable(with = IntRangeSerializer::class) IntRange = 1..1,
    val exp: Int = 0,
    val affectedByFortune: Boolean = false,
    val affectedBySilkTouch: Boolean = true,
    val silkTouchedDrop: SerializableItemStack? = null
)
