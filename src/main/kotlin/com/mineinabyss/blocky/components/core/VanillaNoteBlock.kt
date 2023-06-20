package com.mineinabyss.blocky.components.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("blocky:vanilla_note_block")
data class VanillaNoteBlock(val note: Int = 0)
