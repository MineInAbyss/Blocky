package com.mineinabyss.blocky.api.events.furniture

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList

class BlockyFurnitureDamageAbortEvent(
    entity: Entity,
    val player: Player
) : BlockyFurnitureEvent(entity) {

    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }

}
