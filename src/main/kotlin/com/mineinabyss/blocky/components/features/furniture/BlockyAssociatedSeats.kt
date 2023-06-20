package com.mineinabyss.blocky.components.features.furniture

import com.mineinabyss.idofront.serialization.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import java.util.*

@Serializable
@SerialName("blocky:associated_seats")
data class BlockyAssociatedSeats(val _seats: MutableList<@Serializable(with = UUIDSerializer::class) UUID> = mutableListOf()) {
    val seats: List<Entity> get() = _seats.mapNotNull { Bukkit.getEntity(it) }
}
