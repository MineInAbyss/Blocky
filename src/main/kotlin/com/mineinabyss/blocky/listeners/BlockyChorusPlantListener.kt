package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.helpers.breakBlockyBlock
import io.papermc.paper.event.block.BlockBreakBlockEvent
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockGrowEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.block.BlockSpreadEvent

class BlockyChorusPlantListener : Listener {

    @EventHandler
    fun BlockGrowEvent.cancelChorusGrow() {
        if (block.type == Material.CHORUS_PLANT || block.type == Material.CHORUS_FLOWER)
            isCancelled = true
    }

    @EventHandler
    fun BlockSpreadEvent.cancelChorusGrow() {
        if (source.type == Material.CHORUS_PLANT || source.type == Material.CHORUS_FLOWER)
            isCancelled = true
    }

    @EventHandler
    fun BlockPhysicsEvent.onChorusPhysics() {
        if (block.type == Material.CHORUS_FLOWER || block.type == Material.CHORUS_PLANT) {
            if (sourceBlock.isLiquid) BlockBreakBlockEvent(block, sourceBlock, emptyList()).callEvent()
            isCancelled = true
        }
    }

    @EventHandler
    fun BlockBreakBlockEvent.onWaterCollide() {
        if (block.type == Material.CHORUS_PLANT) {
            breakBlockyBlock(block, null)
            drops.removeIf { it.type == Material.CHORUS_FRUIT }
        }
    }
}
