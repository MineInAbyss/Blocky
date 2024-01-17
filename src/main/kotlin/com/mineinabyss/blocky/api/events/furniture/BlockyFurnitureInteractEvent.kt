package com.mineinabyss.blocky.api.events.furniture

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class BlockyFurnitureInteractEvent(
    entity: Entity,
    val player: Player,
    val hand: EquipmentSlot,
    val itemInHand: ItemStack,
    val clickedRelativePosition: Vector?,
) : BlockyFurnitureEvent(entity), Cancellable {

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
