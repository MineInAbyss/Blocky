package com.mineinabyss.blocky.components.features

import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.serializers.PrefabKeySerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//TODO Since directional blocks are skipped, requiring BlockyBlocks component is dumb, fix
@Serializable
@SerialName("blocky:directional")
data class BlockyDirectional(
    val yBlock: @Serializable(with = PrefabKeySerializer::class) PrefabKey? = null,
    val xBlock: @Serializable(with = PrefabKeySerializer::class) PrefabKey? = null,
    val zBlock: @Serializable(with = PrefabKeySerializer::class) PrefabKey? = null,
    val parentBlock: @Serializable(with = PrefabKeySerializer::class) PrefabKey? = null
) {
    val isParentBlock: Boolean
        get() = parentBlock == null

}
