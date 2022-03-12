package com.mineinabyss.blocky.components

import com.mineinabyss.geary.prefabs.PrefabKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("blocky:entity")
class BlockyEntity(
    val prefab: PrefabKey
)