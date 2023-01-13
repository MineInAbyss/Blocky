package com.mineinabyss.blocky.components.core

import com.mineinabyss.idofront.serialization.LocationSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location

@Serializable
@SerialName("blocky:furniture_hitbox")
data class BlockyFurnitureHitbox(val hitbox: MutableList<@Serializable(with = LocationSerializer::class) Location> = mutableListOf())
