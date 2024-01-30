package com.mineinabyss.blocky.systems

import com.mineinabyss.blocky.api.BlockyFurnitures.prefabKey
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.accessors.Pointer
import com.mineinabyss.idofront.serialization.BaseSerializableItemStack
import com.mineinabyss.idofront.serialization.toSerializable
import com.mineinabyss.idofront.time.ticks
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import java.util.*

class FurnitureTrackerSystem : RepeatingSystem(interval = 1.ticks) {
    private val Pointer.furniture by get<ItemDisplay>()

    override fun Pointer.tick() {
        val preventUpdate = furniture.toGearyOrNull()?.get<BlockyFurniture.PreventItemStackUpdate>()
        if (preventUpdate != null && !preventUpdate.forceWhenDifferentMaterial) return
        val freshItem = furniture.prefabKey?.let { gearyItems.createItem(it, furniture.itemStack) } ?: return
        // Skip updating if preventUpdate is present and forceWhenDifferentMaterial is true
        if (freshItem.type != furniture.itemStack?.type && preventUpdate != null) return
        furniture.itemStack = freshItem.toSerializable().toItemStack(furniture.itemStack ?: ItemStack.empty(), EnumSet.of(BaseSerializableItemStack.Properties.COLOR))
    }

}