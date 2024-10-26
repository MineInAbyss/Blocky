package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.api.BlockyFurnitures.prefabKey
import com.mineinabyss.blocky.components.features.blocks.BlockyDirectional
import com.mineinabyss.blocky.helpers.CopperHelpers
import com.mineinabyss.blocky.helpers.FurniturePacketHelpers
import com.mineinabyss.blocky.helpers.gearyInventory
import com.mineinabyss.blocky.helpers.toBlockPos
import com.mineinabyss.geary.papermc.toEntityOrNull
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.prefabKey
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.papermc.withGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import org.bukkit.FluidCollisionMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryCreativeEvent
import org.bukkit.event.inventory.InventoryType

class BlockyMiddleClickListener : Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun InventoryCreativeEvent.middleClickBlockyItem() {
        val player = clickedInventory?.holder as? Player ?: return
        player.withGeary {
            when {
                click != ClickType.CREATIVE || slotType != InventoryType.SlotType.QUICKBAR -> return
                (cursor.type in buildSet {
                    add(Material.NOTE_BLOCK)
                    add(Material.STRING)
                    add(Material.CAVE_VINES)
                    add(Material.BARRIER)
                    add(Material.PETRIFIED_OAK_SLAB)
                    addAll(CopperHelpers.BLOCKY_SLABS)
                    addAll(CopperHelpers.BLOCKY_STAIRS)
                }) -> {
                    val lookingAtPrefab = player.getTargetBlockExact(5, FluidCollisionMode.NEVER)?.prefabKey
                        ?: player.getTargetEntity(5)?.prefabKey ?: player.getTargetBlockExact(5)?.toBlockPos()
                            ?.let { FurniturePacketHelpers.baseFurnitureFromCollisionHitbox(it) }?.prefabKey ?: return
                    val prefabKey =
                        lookingAtPrefab.toEntityOrNull()?.get<BlockyDirectional>()?.parentBlock ?: lookingAtPrefab

                    val existingSlot = (0..8).firstOrNull {
                        player.gearyInventory?.get(it)?.prefabs?.firstOrNull()?.get<PrefabKey>() == prefabKey
                    }
                    if (existingSlot != null) {
                        player.inventory.heldItemSlot = existingSlot
                        isCancelled = true
                        return
                    }
                    cursor = getAddon(ItemTracking).createItem(prefabKey) ?: return
                }
            }
        }
    }
}
