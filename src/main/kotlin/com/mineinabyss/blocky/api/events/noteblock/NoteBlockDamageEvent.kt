package com.mineinabyss.blocky.api.events.noteblock

import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList

class NoteBlockDamageEvent(
    noteblock: Block,
    val player: Player,
) : NoteBlockEvent(noteblock), Cancellable {

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
