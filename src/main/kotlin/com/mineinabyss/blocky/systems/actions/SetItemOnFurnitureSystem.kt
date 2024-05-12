package com.mineinabyss.blocky.systems.actions

import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.systems.builders.observeWithData
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.items.asColorable
import com.mineinabyss.idofront.items.editItemMeta
import net.kyori.adventure.text.Component
import org.bukkit.entity.ItemDisplay

fun GearyModule.createFurnitureItemSetter() = observeWithData<SetItem>()
    .involving(query<BlockyFurniture, BlockyFurniture.Color, ItemDisplay>())
    .exec { (furniture, color, entity) ->
        val itemStack = furniture.properties.itemStack?.toItemStackOrNull() ?: event.item.toItemStack()
        val furnitureItem = itemStack.clone().editItemMeta {
            displayName(Component.empty())
            color.color?.let { asColorable()?.color = it }
        }
        entity.itemStack = furnitureItem
    }
