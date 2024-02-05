package com.mineinabyss.blocky.components.features.furniture

import com.mineinabyss.geary.datatypes.ComponentDefinition
import com.mineinabyss.geary.papermc.bridge.events.EventHelpers
import com.mineinabyss.geary.papermc.bridge.events.entities.OnSpawn
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("blocky:seat")
data class BlockySeat(val heightOffset: Double = 0.0) {
    companion object : ComponentDefinition by EventHelpers.defaultTo<OnSpawn>()
}
