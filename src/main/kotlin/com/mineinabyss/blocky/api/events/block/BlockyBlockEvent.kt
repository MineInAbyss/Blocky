package com.mineinabyss.blocky.api.events.block

import com.mineinabyss.blocky.components.core.BlockyBlock
import com.mineinabyss.blocky.helpers.gearyEntity
import org.bukkit.block.Block
import org.bukkit.event.HandlerList
import org.bukkit.event.block.BlockEvent

open class BlockyBlockEvent(block: Block) : BlockEvent(block) {
    val blockyBlock get() = block.gearyEntity?.get<BlockyBlock>()

    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
