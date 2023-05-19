package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.api.BlockyFurnitures.prefabKey
import com.mineinabyss.blocky.helpers.BLOCKY_SLABS
import com.mineinabyss.blocky.helpers.BLOCKY_STAIRS
import com.mineinabyss.blocky.helpers.deserializeItemStackToEntity
import com.mineinabyss.blocky.helpers.prefabKey
import com.mineinabyss.blocky.itemProvider
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import org.bukkit.FluidCollisionMode
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
            (cursor.type in mutableSetOf(Material.NOTE_BLOCK, Material.STRING, Material.CAVE_VINES, Material.BARRIER, Material.ITEM_FRAME).apply { addAll(
                BLOCKY_SLABS).apply { addAll(BLOCKY_STAIRS) } }) -> {
                //TODO For some reason BARRIER returns null here over entity, when everywhere else it doesnt? no clue
                val lookingAtPrefab =
                    player.getTargetBlockExact(5, FluidCollisionMode.NEVER)?.prefabKey ?:
                    player.getTargetEntity(5)?.prefabKey ?: return
                val existingSlot = (0..8).firstOrNull {
                    itemProvider.deserializeItemStackToEntity(player.inventory.getItem(it), player.toGeary())?.get<PrefabKey>() == lookingAtPrefab
                }
                if (existingSlot != null) {
                    player.inventory.heldItemSlot = existingSlot
                    isCancelled = true
                    return
                }
                cursor = itemProvider.serializePrefabToItemStack(lookingAtPrefab) ?: return
            }
        }
    }
}
