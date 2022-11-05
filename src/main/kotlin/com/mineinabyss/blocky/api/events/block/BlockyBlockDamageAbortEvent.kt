package com.mineinabyss.blocky.api.events.block

import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList

class BlockyBlockDamageAbortEvent(
    block: Block,
    val player: Player,
) : BlockyBlockEvent(block) {

    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }

}
