package com.mineinabyss.blocky.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Sound

@Serializable
@SerialName("blocky:sound")
class BlockySound (
    val placeSound: Sound? = null,
    val breakSound: Sound? = null,
    val hitSound: Sound? = null,
    val stepSound: Sound? = null,
    val fallSound: Sound? = null
)
