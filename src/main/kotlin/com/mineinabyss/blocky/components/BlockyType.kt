package com.mineinabyss.blocky.components

import com.mineinabyss.blocky.BlockType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("blocky:type")
data class BlockyType (
    val blockType: BlockType = BlockType.NORMAL,
    val blockModelId: Int,
)
