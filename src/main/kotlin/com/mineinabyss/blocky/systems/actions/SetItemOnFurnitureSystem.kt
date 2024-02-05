package com.mineinabyss.blocky.systems.actions

import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.idofront.items.editItemMeta
import net.kyori.adventure.text.Component
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.inventory.meta.PotionMeta

class SetItemOnFurnitureSystem : GearyListener() {
    private val Pointers.display by get<ItemDisplay>().on(target)
    private val Pointers.color by get<BlockyFurniture.Color>().orNull().on(target)
    private val Pointers.furniture by get<BlockyFurniture>().on(target)
    private val Pointers.setItem by get<SetItem>().on(source)

    override fun Pointers.handle() {
        val color = color?.color
        val itemStack = furniture.properties.itemStack?.toItemStackOrNull() ?: setItem.item.toItemStack()
        val furnitureItem = itemStack.clone().editItemMeta {
            displayName(Component.empty())
            if (color != null) {
                (this as? LeatherArmorMeta)?.setColor(color)
                    ?: (this as? PotionMeta)?.setColor(color)
                    ?: (this as? MapMeta)?.setColor(color) ?: return@editItemMeta
            }
        }
        display.itemStack = furnitureItem
    }
}
