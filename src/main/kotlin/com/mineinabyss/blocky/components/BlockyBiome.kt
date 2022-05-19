package com.mineinabyss.blocky.components

import com.mineinabyss.geary.datatypes.GearyEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("blocky:biome")
class BlockyBiome(
    val mcName: String,
    val customName: String,
    val temperature: Float,
    val downfall: Float,
    val fogColour: Int,
    val waterColour: Int,
    val waterFogColour: Int,
    val skyColour: Int,
    val foliageColour: Int,
    val grassColour: Int,
    val isFrozen: Boolean
)

val GearyEntity.blockyBiome get() = get<BlockyBiome>()
val GearyEntity.hasBlockyBiome get() = get<BlockyBiome>() != null
