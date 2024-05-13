package com.mineinabyss.blocky.systems.actions

import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.systems.builders.observeWithData
import com.mineinabyss.geary.systems.query.Query
import com.mineinabyss.idofront.items.asColorable
import com.mineinabyss.idofront.items.editItemMeta
import net.kyori.adventure.text.Component
import org.bukkit.entity.ItemDisplay

fun GearyModule.createFurnitureItemSetter() = observeWithData<OnSet>()
    .involving<SetItem, ItemDisplay, BlockyFurniture>()
    .exec(object : Query() {
        val itemDisplay by get<ItemDisplay>()
        val furniture by get<BlockyFurniture>()
        val color by get<BlockyFurniture.Color>().orNull()
        val setItem by get<SetItem>()
    }) {
        val itemStack = it.furniture.properties.itemStack?.toItemStackOrNull() ?: it.setItem.item.toItemStack()
        val furnitureItem = itemStack.clone().editItemMeta {
            displayName(Component.empty())
            it.color?.color?.let { c -> asColorable()?.color = c }
        }
        it.itemDisplay.itemStack = furnitureItem
    }
