package com.mineinabyss.blocky.components

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.idofront.serialization.LocationSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location

@Serializable
@SerialName("blocky:seat_locations")
data class BlockySeatLocations(
    val seats: MutableList<@Serializable(with = LocationSerializer::class) Location> = mutableListOf()
)

val GearyEntity.blockySeatLoc get() = get<BlockySeatLocations>()
val GearyEntity.hasBlockySeatLoc get() = blockySeatLoc != null