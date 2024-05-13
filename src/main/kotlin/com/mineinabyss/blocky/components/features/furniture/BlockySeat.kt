package com.mineinabyss.blocky.components.features.furniture

import com.mineinabyss.idofront.serialization.VectorSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.util.Vector

@Serializable
@SerialName("blocky:seat")
data class BlockySeat(val offset: @Serializable(VectorSerializer::class) Vector = Vector())
