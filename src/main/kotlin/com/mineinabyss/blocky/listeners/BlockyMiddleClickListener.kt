package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.helpers.prefabKey
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.looty.LootyFactory
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryCreativeEvent

class BlockyMiddleClickListener : Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun InventoryCreativeEvent.middleClickBlockyItem() {
        if (click != ClickType.CREATIVE) return
        val player = inventory.holder as? Player ?: return
        when {
            (cursor.type in setOf(Material.NOTE_BLOCK, Material.STRING)) -> {
                val lookingAtPrefab = player.rayTraceBlocks(6.0)?.hitBlock?.prefabKey ?: return
                val existingSlot = (0..8).firstOrNull {
                    player.inventory.getItem(it)?.toGearyOrNull(player)?.get<PrefabKey>() == lookingAtPrefab
                }
                if (existingSlot != null) {
                    player.inventory.heldItemSlot = existingSlot
                    isCancelled = true
                    return
                }
                player.inventory.heldItemSlot
                cursor = LootyFactory.createFromPrefab(lookingAtPrefab) ?: return
            }
        }
    }
}
