package com.mineinabyss.blocky.api.events.furniture

import com.mineinabyss.blocky.components.core.BlockyModelEngine
import com.mineinabyss.geary.papermc.access.toGeary
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList

class FurniturePlaceEvent(
    furniture: Entity,
    val player: Player
) : FurnitureEvent(furniture), Cancellable {

    val isModelEngineFurniture get() : Boolean {
        return entity.toGeary().has<BlockyModelEngine>()
    }
    private var cancelled = false

    override fun isCancelled() = cancelled

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }

}
