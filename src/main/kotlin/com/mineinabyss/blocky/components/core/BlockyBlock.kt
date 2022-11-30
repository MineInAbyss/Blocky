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
        NOTEBLOCK, WIRE, LEAF, CAVEVINE, SLAB, STAIR
    }

    @Serializable
    @SerialName("blocky:slab")
    data class Slab(
        val topModel: String,
        val bottomModel: String,
        val doubleModel: String,
    )

    @Serializable
    @SerialName("blocky:stair")
    data class Stair(
        val innerModel: String,
        val outerModel: String,
        val straightModel: String,
    )
}
