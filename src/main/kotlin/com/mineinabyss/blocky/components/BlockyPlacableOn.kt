package com.mineinabyss.blocky.components

import com.mineinabyss.geary.prefabs.PrefabKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Material

@Serializable
@SerialName("blocky:placable_on")
class BlockyPlacableOn(
    val blocks: List<Material> = emptyList(),
    val blockTags: List<String> = emptyList(),
    val blockyBlocks: List<PrefabKey> = emptyList()
)
