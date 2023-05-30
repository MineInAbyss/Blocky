package com.mineinabyss.blocky.components.features

import com.mineinabyss.idofront.serialization.LocationSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.block.Block

@Serializable
@SerialName("blocky:tall_wire")
data class BlockyTallWire(val _baseWire: @Serializable(LocationSerializer::class) Location) {
    val baseWire: Block get() = _baseWire.block
}
