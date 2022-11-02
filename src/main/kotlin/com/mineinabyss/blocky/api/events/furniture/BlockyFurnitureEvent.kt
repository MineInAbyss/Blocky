package com.mineinabyss.blocky.api.events.furniture

import com.mineinabyss.blocky.components.core.BlockyEntity
import com.mineinabyss.blocky.components.core.BlockyModelEngine
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import org.bukkit.entity.Entity
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityEvent

open class BlockyFurnitureEvent(entity: Entity) : EntityEvent(entity) {

    val furniture get() = entity.toGearyOrNull()?.get<BlockyEntity>()

    val isModelEngineFurniture get() : Boolean {
        return entity.toGeary().has<BlockyModelEngine>()
    }

    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
