package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.components.blockyBiome
import com.mineinabyss.blocky.components.hasBlockyBiome
import com.mineinabyss.blocky.helpers.biome.setCustomBiome
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class BlockyLiquidListener : Listener {

    @EventHandler
    fun PlayerInteractEvent.onPlaceLiquid() {
        val item = item?.toGearyOrNull(player) ?: return
        if (action != Action.RIGHT_CLICK_BLOCK) return
        if (!item.hasBlockyBiome) return
        val block = clickedBlock?.getRelative(blockFace) ?: return
        setCustomBiome(item.blockyBiome!!.customName, block.location)
    }
}
