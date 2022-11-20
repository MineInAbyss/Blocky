package com.mineinabyss.blocky.components.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("blocky:modelengine")
data class BlockyModelEngine(
    val modelId: String,
)
