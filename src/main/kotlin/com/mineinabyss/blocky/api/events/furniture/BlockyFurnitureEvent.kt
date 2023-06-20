package com.mineinabyss.blocky.api.events.furniture

import com.mineinabyss.blocky.api.BlockyFurnitures.baseFurniture
import com.mineinabyss.blocky.api.BlockyFurnitures.interactionEntity
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import org.bukkit.entity.Entity
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityEvent

open class BlockyFurnitureEvent(entity: Entity) : EntityEvent(entity) {

    open val furniture get() = baseEntity.toGearyOrNull()?.get<BlockyFurniture>()

    val baseEntity get() = (entity as? Interaction)?.baseFurniture ?: entity as ItemDisplay
    val interactionEntity = baseEntity.interactionEntity

    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
