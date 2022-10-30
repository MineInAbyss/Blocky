package com.mineinabyss.blocky.api.events.leafblock

import org.bukkit.block.Block
import org.bukkit.event.HandlerList
import org.bukkit.event.block.BlockEvent

open class LeafBlockEvent(leaf: Block) : BlockEvent(leaf) {
    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
