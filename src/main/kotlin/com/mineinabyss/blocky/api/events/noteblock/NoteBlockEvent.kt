package com.mineinabyss.blocky.api.events.noteblock

import org.bukkit.block.Block
import org.bukkit.event.HandlerList
import org.bukkit.event.block.BlockEvent

open class NoteBlockEvent(noteblock: Block) : BlockEvent(noteblock) {
    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
