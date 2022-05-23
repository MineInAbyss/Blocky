package com.mineinabyss.blocky.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("blocky:light")
data class BlockyLight (
    val lightLevel: Int = 15
)
