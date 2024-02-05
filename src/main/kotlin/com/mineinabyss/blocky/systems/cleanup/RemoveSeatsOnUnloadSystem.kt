package com.mineinabyss.blocky.systems.cleanup

import com.mineinabyss.blocky.components.features.furniture.BlockySeat
import com.mineinabyss.geary.papermc.tracking.entities.events.GearyEntityRemoveFromWorldEvent
import org.bukkit.entity.ItemDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class RemoveSeatsOnUnloadSystem : Listener {
    @EventHandler
    fun GearyEntityRemoveFromWorldEvent.onRemoveSeats() {
        val display = entity as? ItemDisplay ?: return
        if (gearyEntity.has<BlockySeat>()) {
//            FurnitureHelpers.clearFurnitureSeats(display)
        }
    }
}
