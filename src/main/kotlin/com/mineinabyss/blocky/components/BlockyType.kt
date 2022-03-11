package com.mineinabyss.blocky.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("blocky:type")
data class BlockyType (
    val blockType: BlockType = BlockType.NORMAL,
    val blockModelType: BlockModelType = BlockModelType.VANILLA,
)

enum class BlockType {
    NORMAL, PASSTHROUGH, INTERACTABLE, MISC
}

enum class BlockModelType {
    VANILLA, MODELENGINE
}