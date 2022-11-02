package com.mineinabyss.blocky.components.features

import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.serializers.PrefabKeySerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("blocky:directional")
class BlockyDirectional(
    val yBlock: @Serializable(with = PrefabKeySerializer::class) PrefabKey,
    val xBlock: @Serializable(with = PrefabKeySerializer::class) PrefabKey,
    val zBlock: @Serializable(with = PrefabKeySerializer::class) PrefabKey,
)
