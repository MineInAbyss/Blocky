package com.mineinabyss.blocky.api.events.block

import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

class BlockyBlockInteractEvent(
    block: Block,
    val player: Player,
    val hand: EquipmentSlot,
    val itemInHand: ItemStack,
    val blockFace: BlockFace
) : BlockyBlockEvent(block), Cancellable {
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
