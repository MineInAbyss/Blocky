package com.mineinabyss.blocky.components

import com.mineinabyss.idofront.items.editItemMeta
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

@Serializable
@SerialName("blocky:type")
data class BlockyType (
    val blockType: BlockType = BlockType.NORMAL,
    val blockModel: Int
) {
    val blockItemStack
        get() = ItemStack(Material.NOTE_BLOCK).editItemMeta {
            setCustomModelData(blockModel)
        }
}

enum class BlockType {
    NORMAL, INTERACTABLE, MISC
}