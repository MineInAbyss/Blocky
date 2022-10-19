package com.mineinabyss.blocky.components.features

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
@SerialName("blocky:seat")
value class BlockySeat (val heightOffset: Double = 0.5)
