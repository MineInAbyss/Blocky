package com.mineinabyss.blocky.systems.actions

import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import com.mineinabyss.idofront.items.asColorable
import com.mineinabyss.idofront.items.editItemMeta
import net.kyori.adventure.text.Component
import org.bukkit.entity.ItemDisplay

fun GearyModule.createFurnitureItemSetter() = listener(
    object : ListenerQuery() {
        val display by get<ItemDisplay>()
        val furniture by get<BlockyFurniture>()
        val furnitureColor by get<BlockyFurniture.Color>().orNull()
        val setItem by source.get<SetItem>()
    }
).exec {
    val itemStack = furniture.properties.itemStack?.toItemStackOrNull() ?: setItem.item.toItemStack()
    val furnitureItem = itemStack.clone().editItemMeta {
        displayName(Component.empty())
        furnitureColor?.color?.let { asColorable()?.color = it }
    }
    display.itemStack = furnitureItem

}
