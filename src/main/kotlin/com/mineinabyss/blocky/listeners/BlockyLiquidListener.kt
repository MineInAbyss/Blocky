package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.components.blockyBiome
import com.mineinabyss.blocky.components.hasBlockyBiome
import com.mineinabyss.blocky.helpers.biome.setCustomBiome
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class BlockyLiquidListener : Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun PlayerInteractEvent.onPlaceLiquid() {
        val item = item?.toGearyOrNull(player) ?: return
        if (action != Action.RIGHT_CLICK_BLOCK) return
        if (hand != EquipmentSlot.HAND) return
        if (!item.hasBlockyBiome) return
        val block = clickedBlock?.getRelative(blockFace) ?: return
        setCustomBiome(item.blockyBiome!!.customName, block.location)
        block.world.refreshChunk(block.chunk.x, block.chunk.z)
        block.setBlockData(Bukkit.createBlockData(Material.GRASS_BLOCK))
        isCancelled = true
        return
    }
}
