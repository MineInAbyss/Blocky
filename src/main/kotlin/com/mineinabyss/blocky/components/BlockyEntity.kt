package com.mineinabyss.blocky.components

import com.mineinabyss.geary.prefabs.PrefabKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("blocky:entity")
class BlockyEntity(
    val entityType: EntityType = EntityType.JAVA,
    val entityPrefab: PrefabKey,
    val collisionRadius: Int = 0,
)

enum class EntityType {
    MODEL_ENGINE, JAVA
}
