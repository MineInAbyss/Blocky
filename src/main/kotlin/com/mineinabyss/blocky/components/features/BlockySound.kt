package com.mineinabyss.blocky.components.features

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("blocky:sound")
class BlockySound(
    val placeSound: String? = null,
    val breakSound: String? = null,
    val hitSound: String? = null,
    val stepSound: String? = null,
    val fallSound: String? = null
)
