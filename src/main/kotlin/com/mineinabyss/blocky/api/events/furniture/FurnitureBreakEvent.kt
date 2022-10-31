package com.mineinabyss.blocky.api.events.furniture

import com.mineinabyss.blocky.components.core.BlockyModelEngine
import com.mineinabyss.geary.papermc.access.toGeary
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList

class FurnitureBreakEvent(
    furniture: Entity,
    val player: Player?
) : FurnitureEvent(furniture), Cancellable {

    val isModelEngineFurniture get() : Boolean {
        return entity.toGeary().has<BlockyModelEngine>()
    }

    override fun isCancelled(): Boolean {
        return isCancelled
    }

    override fun setCancelled(cancel: Boolean) {
        this.isCancelled = cancel
    }

    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }

}
