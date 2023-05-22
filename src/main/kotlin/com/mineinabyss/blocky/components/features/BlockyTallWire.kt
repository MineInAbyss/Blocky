package com.mineinabyss.blocky.components.features

import com.mineinabyss.blocky.blocky
import com.mineinabyss.idofront.util.toMCKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.NamespacedKey

@Serializable
@SerialName("blocky:tall_wire")
class BlockyTallWire {
    val TALL_WIRE_KEY: String = NamespacedKey(blocky.plugin, "tall_wire").toString()
    fun getKey() = TALL_WIRE_KEY.toMCKey()
}
