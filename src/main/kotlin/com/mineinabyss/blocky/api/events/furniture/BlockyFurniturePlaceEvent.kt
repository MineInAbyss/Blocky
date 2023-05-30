package com.mineinabyss.blocky.api.events.furniture

import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList

class BlockyFurniturePlaceEvent(
    entity: Entity,
    val player: Player
) : BlockyFurnitureEvent(entity), Cancellable {

    private var cancelled = false

    override val furniture get() = entity.toGeary().get<BlockyFurniture>()!!

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
