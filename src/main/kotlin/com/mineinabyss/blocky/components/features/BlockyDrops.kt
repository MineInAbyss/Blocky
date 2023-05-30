package com.mineinabyss.blocky.components.features

import com.mineinabyss.blocky.components.features.mining.ToolType
import com.mineinabyss.idofront.serialization.IntRangeSerializer
import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("blocky:drops")
data class BlockyDrops(
    val acceptedToolTypes: Set<ToolType> = setOf(ToolType.ANY),
    val onlyDropWithCorrectTool: Boolean = false,
    val drops: Set<BlockyDrop> = setOf(),
) {
    @Serializable
    data class BlockyDrop(
        val item: SerializableItemStack? = null,
        val amount: @Serializable(with = IntRangeSerializer::class) IntRange = 1..1,
        val exp: Int = 0,
        val affectedByFortune: Boolean = false,
        val affectedBySilkTouch: Boolean = true,
        val silkTouchedDrop: SerializableItemStack? = null
    )
}
