package com.mineinabyss.blocky.components.core

import com.mineinabyss.idofront.serialization.LocationSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location

@Serializable
@SerialName("blocky:barrier_hitbox")
data class BlockyBarrierHitbox(val barriers: MutableList<@Serializable(with = LocationSerializer::class) Location> = mutableListOf())
