package com.mineinabyss.blocky.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("blocky:type")
data class BlockyType (
    val blockType: BlockType = BlockType.CUBE,
    val blockModelType: BlockModelType = BlockModelType.BLOCK,
)

enum class BlockType {
    CUBE, GROUND, WALL, INTERACTABLE, MISC
}

enum class BlockModelType {
    BLOCK, ENTITY
}