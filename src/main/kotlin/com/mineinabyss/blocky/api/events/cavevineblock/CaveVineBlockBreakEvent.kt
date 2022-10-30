package com.mineinabyss.blocky.api.events.cavevineblock

import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList

class CaveVineBlockBreakEvent(
    caveVine: Block,
    val player: Player,
) : CaveVineBlockEvent(caveVine), Cancellable {

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
