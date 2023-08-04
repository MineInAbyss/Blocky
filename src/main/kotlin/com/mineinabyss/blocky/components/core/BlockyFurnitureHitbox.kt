package com.mineinabyss.blocky.components.core

import com.mineinabyss.idofront.serialization.LocationSerializer
import com.mineinabyss.idofront.serialization.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location
import java.util.*

/**
 * A hitbox for a piece of furniture.
 * @param hitbox A list of locations for Barrier-hitboxes relative to the origin of the furniture.
 * @param baseEntity The UUID of the baseEntity this Interaction entity is attached to.
 * @param interactionHitbox The UUID of the Interaction entity this furniture is attached to.
 */
@Serializable
@SerialName("blocky:furniture_hitbox")
data class BlockyFurnitureHitbox(
    val hitbox: MutableList<@Serializable(with = LocationSerializer::class) Location> = mutableListOf(),
    val baseEntity: @Serializable(UUIDSerializer::class) UUID? = null,
    val interactionHitbox: @Serializable(UUIDSerializer::class) UUID? = null
)
