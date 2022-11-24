package com.mineinabyss.blocky.components.core

import com.mineinabyss.blocky.systems.BlockLocation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("blocky:furniture")
data class BlockyFurniture(
    val furnitureType: FurnitureType,
    val rotationType: RotationType = RotationType.VERY_STRICT,
    val collisionHitbox: List<BlockLocation> = emptyList(),
    val originOffset: BlockLocation = BlockLocation(0, 0, 0),
) {
    enum class FurnitureType {
        ARMOR_STAND, ITEM_FRAME, GLOW_ITEM_FRAME
    }

    enum class RotationType {
        NONE, STRICT, VERY_STRICT
    }

    val hasStrictRotation get() = rotationType != RotationType.NONE
}
