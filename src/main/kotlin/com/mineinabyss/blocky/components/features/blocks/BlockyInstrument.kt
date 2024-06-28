package com.mineinabyss.blocky.components.features.blocks

import com.mineinabyss.idofront.serialization.KeySerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.key.Key
import org.bukkit.Instrument

@Serializable
@SerialName("blocky:instrument")
data class BlockyInstrument(val instrument: @Serializable(KeySerializer::class) Key = Instrument.PIANO.sound!!.key())