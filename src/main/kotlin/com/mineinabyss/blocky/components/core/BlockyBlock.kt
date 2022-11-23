package com.mineinabyss.blocky.components.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("blocky:block")
data class BlockyBlock (
    val blockType: BlockType,
    val blockId: Int,
    val blockModel: String? = null,
) {
    enum class BlockType {
        NOTEBLOCK, WIRE, LEAF, CAVEVINE
    }
}
