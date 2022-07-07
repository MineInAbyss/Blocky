package com.mineinabyss.blocky.components

import com.mineinabyss.blocky.helpers.getGearyEntityFromBlock
import com.mineinabyss.blocky.helpers.getPrefabFromBlock
import com.mineinabyss.blocky.helpers.isInProvidedTags
import com.mineinabyss.geary.prefabs.PrefabKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace

@Serializable
@SerialName("blocky:placable_on")
class BlockyPlacableOn(
    val placableOnFace: List<BlockFace> = BlockFace.values().filter { it.isCartesian },
    val blocks: List<Material> = emptyList(),
    val blockTags: List<String> = emptyList(),
    val blockyBlocks: List<PrefabKey> = emptyList()
)

fun Block.isPlacableOn(face: BlockFace) : Boolean {
    val placable = getGearyEntityFromBlock()?.get<BlockyPlacableOn>() ?: return false
    val against = getRelative(face)
    if (!placable.placableOnFace.contains(face)) return false
    if (against.getPrefabFromBlock() !in placable.blockyBlocks &&
        !against.isInProvidedTags(placable.blockTags) && against.type !in placable.blocks
    ) return false

    return true
}
