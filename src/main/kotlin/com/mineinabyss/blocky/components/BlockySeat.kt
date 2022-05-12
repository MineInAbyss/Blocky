package com.mineinabyss.blocky.components

import com.mineinabyss.geary.datatypes.GearyEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("blocky:seat")
data class BlockySeat (
    val yaw: Float = 0F,
    val heightOffset: Double = 0.5
)

val GearyEntity.blockySeat get() = get<BlockySeat>()
val GearyEntity.hasBlockySeat get() = blockySeat != null