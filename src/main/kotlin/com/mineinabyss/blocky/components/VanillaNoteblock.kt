package com.mineinabyss.blocky.components

import com.mineinabyss.blocky.blockyPlugin
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.NamespacedKey

@Serializable
@SerialName("blocky:vanilla_noteblock")
class VanillaNoteBlock(
    val key: @Contextual NamespacedKey = NamespacedKey(blockyPlugin, "vanilla_noteblock")
)