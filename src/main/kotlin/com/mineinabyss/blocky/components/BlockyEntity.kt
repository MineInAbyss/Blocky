package com.mineinabyss.blocky.components

import com.mineinabyss.blocky.systems.BlockLocation
import com.mineinabyss.geary.prefabs.PrefabKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("blocky:entity")
data class BlockyEntity(
    val entityType: EntityType = EntityType.ITEM_FRAME,
    val entityPrefab: PrefabKey? = null,
    val itemFrameId: Int? = null,
    val collisionHitbox: List<BlockLocation> = listOf(),
    val canBeInAir: Boolean = false,
    val canBeRotated: Boolean = true
)

enum class EntityType {
    MODEL_ENGINE, JAVA, ITEM_FRAME
}
