package com.mineinabyss.blocky.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Sound

@Serializable
@SerialName("blocky:sound")
class BlockySound (
    val placeSound: Sound,
    val breakSound: Sound
)