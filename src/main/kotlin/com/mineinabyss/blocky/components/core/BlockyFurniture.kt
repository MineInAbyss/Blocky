package com.mineinabyss.blocky.components.core

import com.mineinabyss.blocky.systems.BlockLocation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("blocky:entity")
data class BlockyEntity(
    val entityType: EntityType = EntityType.ITEM_FRAME,
    val itemFrameId: Int? = null,
    val collisionHitbox: List<BlockLocation> = listOf(),
)

enum class EntityType {
    JAVA, ITEM_FRAME
}
