package com.mineinabyss.blocky.components.features.furniture

import com.mineinabyss.geary.datatypes.ComponentDefinition
import com.mineinabyss.geary.papermc.bridge.events.EventHelpers
import com.mineinabyss.geary.papermc.bridge.events.entities.OnSpawn
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("blocky:modelengine")
data class BlockyModelEngine(val modelId: String) {
    companion object : ComponentDefinition by EventHelpers.defaultTo<OnSpawn>()
}
