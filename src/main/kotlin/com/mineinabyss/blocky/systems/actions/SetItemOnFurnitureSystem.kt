package com.mineinabyss.blocky.systems.actions

import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.systems.builders.observeWithData
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.items.asColorable
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.typealiases.BukkitEntity
import net.kyori.adventure.text.Component
import org.bukkit.entity.ItemDisplay

fun GearyModule.createFurnitureItemSetter() = observeWithData<OnSet>()
    .exec(query<BukkitEntity, SetItem, BlockyFurniture/*, BlockyFurniture.Color*/>()) { (entity, item, furniture/*, color*/) ->
        val itemDisplay = entity as? ItemDisplay ?: return@exec
        val itemStack = furniture.properties.itemStack?.toItemStackOrNull() ?: item.item.toItemStack()
        val furnitureItem = itemStack.clone().editItemMeta {
            displayName(Component.empty())
            //color.color?.let { asColorable()?.color = it }
        }
        itemDisplay.itemStack = furnitureItem
    }
