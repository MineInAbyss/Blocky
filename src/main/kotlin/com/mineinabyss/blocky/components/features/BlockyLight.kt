package com.mineinabyss.blocky.components.features

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
@SerialName("blocky:light")
value class BlockyLight (val lightLevel: Int = 15)
