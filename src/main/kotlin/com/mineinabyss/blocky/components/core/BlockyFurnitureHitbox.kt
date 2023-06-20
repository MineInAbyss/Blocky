package com.mineinabyss.blocky.components.core

import com.mineinabyss.idofront.serialization.LocationSerializer
import com.mineinabyss.idofront.serialization.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
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
    val _baseEntity: @Serializable(UUIDSerializer::class) UUID? = null,
    val _interactionHitbox: @Serializable(UUIDSerializer::class) UUID? = null
) {
    val baseEntity: ItemDisplay? = _baseEntity?.let { Bukkit.getEntity(it) } as? ItemDisplay
    val interactionHitbox: Interaction? get() = _interactionHitbox?.let { Bukkit.getEntity(it) } as? Interaction
}
