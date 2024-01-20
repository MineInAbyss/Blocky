package com.mineinabyss.blocky.components.features

import com.mineinabyss.blocky.api.BlockyBlocks.isBlockyBlock
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.prefabKey
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
@SerialName("blocky:placableOn")
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

    fun isPlacableOn(block: Block, blockFace: BlockFace): Boolean {
        val against = block.getRelative(blockFace.oppositeFace)

        if ((!placableAgainstFace(block, blockFace))) return false
        if (blocks.isEmpty() && blockTags.isEmpty() && blockyBlocks.isEmpty()) return true
        if (against.prefabKey in this.blockyBlocks || against.isInProvidedTags ||
            (against.type in this.blocks && !against.isBlockyBlock)
        ) return type == AllowType.ALLOW

        return type != AllowType.ALLOW
    }

    private fun placableAgainstFace(block: Block, blockFace: BlockFace): Boolean {
        return when {
            wall && block.type.isSolid && blockFace.modY == 0 -> true
            floor && (blockFace == BlockFace.UP || block.getRelative(BlockFace.DOWN).type.isSolid) -> true
            ceiling && (blockFace == BlockFace.DOWN || block.getRelative(BlockFace.UP).type.isSolid) -> true
            else -> false
        }
    }

    // Gets a list of all the materials present in the provided tags
    private val Block.isInProvidedTags: Boolean get() {
        blockTags.forEach { tag ->
            if (Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft(tag), Material::class.java)
                    ?.isTagged(type) != true
            ) return@forEach
            else return true
        }
        return false
    }
}
