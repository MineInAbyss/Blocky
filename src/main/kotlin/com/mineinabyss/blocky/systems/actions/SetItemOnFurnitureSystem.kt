package com.mineinabyss.blocky.systems.actions

import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.systems.builders.observeWithData
import com.mineinabyss.geary.systems.query.Query
import com.mineinabyss.idofront.items.asColorable
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.typealiases.BukkitEntity
import net.kyori.adventure.text.Component
import org.bukkit.entity.ItemDisplay

fun GearyModule.createFurnitureItemSetter() = observeWithData<SetItem>()
    .exec(object : Query() {
        val entity by get<BukkitEntity>()
        val furniture by get<BlockyFurniture>()
        val color by get<BlockyFurniture.Color>().orNull()
    }) {
        val itemDisplay = it.entity as? ItemDisplay ?: return@exec
        val itemStack = it.furniture.properties.itemStack?.toItemStackOrNull() ?: event.item.toItemStack()
        val furnitureItem = itemStack.clone().editItemMeta {
            displayName(Component.empty())
            it.color?.color?.let { c -> asColorable()?.color = c }
        }
        itemDisplay.itemStack = furnitureItem
    }
