package com.mineinabyss.blocky.components.features.furniture

import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.papermc.bridge.events.entities.OnSpawn
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("blocky:modelengine")
data class BlockyModelEngine(val modelId: String)
