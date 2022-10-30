package com.mineinabyss.blocky.api.events.wireblock

import org.bukkit.block.Block
import org.bukkit.event.HandlerList
import org.bukkit.event.block.BlockEvent

open class WireBlockEvent(wire: Block) : BlockEvent(wire) {
    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
