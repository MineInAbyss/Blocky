package com.mineinabyss.blocky.api.events.cavevineblock

import org.bukkit.block.Block
import org.bukkit.event.HandlerList
import org.bukkit.event.block.BlockEvent

open class CaveVineBlockEvent(caveVine: Block) : BlockEvent(caveVine) {

    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
