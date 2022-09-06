package com.mineinabyss.blocky.components

import com.mineinabyss.idofront.serialization.LocationSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location

@JvmInline
@Serializable
@SerialName("blocky:barrier_hitbox")
value class BlockyBarrierHitbox(val barriers: MutableList<@Serializable(with = LocationSerializer::class) Location> = mutableListOf())
