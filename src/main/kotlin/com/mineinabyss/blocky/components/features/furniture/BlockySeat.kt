package com.mineinabyss.blocky.components.features.furniture

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("blocky:seat")
data class BlockySeat(val heightOffset: Double = 0.0)
