package com.mineinabyss.blocky.api.events.block

import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.toGearyOrNull
import org.bukkit.block.Block
import org.bukkit.event.HandlerList
import org.bukkit.event.block.BlockEvent

open class BlockyBlockEvent(block: Block) : BlockEvent(block) {
    val blockyBlock get() = block.toGearyOrNull()?.get<SetBlock>()

    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
