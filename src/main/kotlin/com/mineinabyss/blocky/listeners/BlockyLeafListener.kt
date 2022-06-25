package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.helpers.isBlockyLeaf
import com.mineinabyss.blocky.helpers.leafConfig
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.player.PlayerInteractEvent

class BlockyLeafListener : Listener {

    @EventHandler
    fun LeavesDecayEvent.onLeafDecay() {
        isCancelled = leafConfig.disableAllLeafDecay
    }

    @EventHandler
    fun BlockPistonExtendEvent.cancelBlockyPiston() {
        isCancelled = blocks.any { it.isBlockyLeaf() }
    }

    @EventHandler
    fun BlockPistonRetractEvent.cancelBlockyPiston() {
        isCancelled = blocks.any { it.isBlockyLeaf() }
    }

    @EventHandler
    fun PlayerInteractEvent.preBlockyLeafPlace() {

    }

    @EventHandler
    fun BlockPlaceEvent.onLeafPlace() {

    }

    @EventHandler
    fun BlockBreakEvent.onLeafBreak() {

    }


}
