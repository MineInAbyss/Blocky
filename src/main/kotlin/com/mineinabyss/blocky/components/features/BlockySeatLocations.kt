package com.mineinabyss.blocky.components.features

import com.mineinabyss.idofront.serialization.LocationSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location

@Serializable
@SerialName("blocky:seat_locations")
data class BlockySeatLocations(val seats: MutableList<@Serializable(with = LocationSerializer::class) Location> = mutableListOf())
