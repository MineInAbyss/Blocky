package com.mineinabyss.blocky.components.features

import com.mineinabyss.blocky.helpers.isBlockyBlock
import com.mineinabyss.blocky.helpers.prefabKey
import com.mineinabyss.geary.prefabs.PrefabKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.block.BlockFace

@Serializable
@SerialName("blocky:placable_on")
class BlockyPlacableOn(
    val floor: Boolean = true,
    val wall: Boolean = true,
    val ceiling: Boolean = true,
    val type: AllowType = AllowType.ALLOW,
    val blocks: List<Material> = emptyList(),
    val blockTags: List<String> = emptyList(),
    val blockyBlocks: List<PrefabKey> = emptyList()
) {

    enum class AllowType {
        ALLOW,
        DENY
    }

    fun isPlacableOn(block: Block, face: BlockFace): Boolean {
        val against = block.getRelative(face.oppositeFace)

        if ((!checkFace(against, face))) return false
        if (blocks.isEmpty() && blockTags.isEmpty() && blockyBlocks.isEmpty()) return true
        if (against.prefabKey in this.blockyBlocks ||
            against.isInProvidedTags(this.blockTags) ||
            (against.type in this.blocks && !against.isBlockyBlock)
        ) return type == AllowType.ALLOW

        return type != AllowType.ALLOW
    }

    private fun checkFace(block: Block, blockFace: BlockFace): Boolean {
        return when (blockFace) {
            BlockFace.UP -> this@BlockyPlacableOn.floor
            BlockFace.DOWN -> this@BlockyPlacableOn.ceiling
            else -> this@BlockyPlacableOn.wall || block.getRelative(BlockFace.DOWN).isSolid
        }
    }

    // Gets a list of all the materials present in the provided tags
    private fun Block.isInProvidedTags(list: List<String>): Boolean {
        list.forEach { tag ->
            if (Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft(tag), Material::class.java)
                    ?.isTagged(type) != true
            ) return@forEach
            else return true
        }
        return false
    }
}
