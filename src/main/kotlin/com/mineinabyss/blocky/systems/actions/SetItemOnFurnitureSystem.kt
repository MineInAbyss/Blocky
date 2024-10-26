package com.mineinabyss.blocky.systems.actions

import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.items.asColorable
import com.mineinabyss.idofront.items.editItemMeta
import net.kyori.adventure.text.Component
import org.bukkit.entity.ItemDisplay

fun Geary.createFurnitureItemSetter() = observe<OnSet>()
    .involving<SetItem, ItemDisplay, BlockyFurniture>()
    .exec(query<ItemDisplay, BlockyFurniture, BlockyFurniture.Color?, SetItem>()) { (itemDisplay, furniture, color, setItem) ->
        val itemStack = furniture.properties.itemStack?.toItemStackOrNull() ?: setItem.item.toItemStack()
        val furnitureItem = itemStack.clone().editItemMeta {
            itemName(Component.empty())
            displayName(Component.empty())
            color?.color?.let { c -> asColorable()?.color = c }
        }
        itemDisplay.setItemStack(furnitureItem)
    }
