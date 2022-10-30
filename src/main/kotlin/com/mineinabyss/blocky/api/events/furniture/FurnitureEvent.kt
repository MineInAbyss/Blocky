package com.mineinabyss.blocky.api.events.furniture

import org.bukkit.entity.Entity
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityEvent

open class FurnitureEvent(furniture: Entity) : EntityEvent(furniture) {
    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
