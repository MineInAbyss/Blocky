package com.mineinabyss.blocky.components.features.mining

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.inventory.ItemStack

/**
* Lets you define a component that affects the mining-speed of custom blocks
 * @param breakSpeedModifier The speed at which this modifies the default break speed of the block
 * @param toolTypes The types of tool this item is registered under
*/
@Serializable
@SerialName("blocky:mining")
data class BlockyMining(val toolTypes: Set<ToolType> = setOf(ToolType.ANY))

enum class ToolType {
    PICKAXE, AXE, SHOVEL, HOE, SWORD, SHEARS, ANY;

    fun contains(itemStack: ItemStack): Boolean {
        return when(this) {
            PICKAXE -> Tag.ITEMS_PICKAXES.isTagged(itemStack.type)
            AXE -> Tag.ITEMS_AXES.isTagged(itemStack.type)
            SHOVEL -> Tag.ITEMS_SHOVELS.isTagged(itemStack.type)
            HOE -> Tag.ITEMS_HOES.isTagged(itemStack.type)
            SWORD -> Tag.ITEMS_SWORDS.isTagged(itemStack.type)
            SHEARS -> itemStack.type == Material.SHEARS
            ANY -> !itemStack.isEmpty
        }
    }
}
