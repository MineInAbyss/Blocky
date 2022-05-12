package com.mineinabyss.blocky.components

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.idofront.serialization.LocationSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location

@Serializable
@SerialName("blocky:barrier_hitbox")
class BlockyBarrierHitbox {
    val barriers: MutableList<@Serializable(with = LocationSerializer::class) Location> = mutableListOf()
}

val GearyEntity.blockyBarriers get() = get<BlockyBarrierHitbox>()
val GearyEntity.hasBlockyBarriers get() = blockyBarriers != null