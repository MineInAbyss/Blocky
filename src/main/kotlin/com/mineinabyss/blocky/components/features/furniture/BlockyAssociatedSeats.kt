package com.mineinabyss.blocky.components.features.furniture

import com.mineinabyss.blocky.helpers.mapNotNullFast
import com.mineinabyss.idofront.serialization.UUIDSerializer
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import java.util.*

@Serializable
@SerialName("blocky:associated_seats")
data class BlockyAssociatedSeats(val _seats: MutableList<@Serializable(with = UUIDSerializer::class) UUID> = mutableListOf()) {
    val seats: ObjectArrayList<Entity> get() = _seats.mapNotNullFast { Bukkit.getEntity(it) }
}
