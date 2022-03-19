package com.mineinabyss.blocky.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("blocky:block")
data class BlockyBlock (
    val blockType: BlockType,
    val blockId: Int
)

enum class BlockType {
    CUBE, GROUND, WALL
}
