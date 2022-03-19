package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.components.BlockyEntity
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.spawnFromPrefab
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot


class BlockyEntityListener : Listener {

    @EventHandler
    fun PlayerInteractEvent.onPlacingBlockyEntity() {
        val item = player.inventory.itemInMainHand
        val geary = item.toGearyOrNull(player) ?: return
        val blockyInfo = geary.get<BlockyInfo>() ?: return
        val blockEntity = geary.get<BlockyEntity>() ?: return
        val loc = interactionPoint?.toCenterLocation() ?: return
        if (hand != EquipmentSlot.HAND) return
        if (action != Action.RIGHT_CLICK_BLOCK) return

        loc.spawnFromPrefab(blockEntity.entityPrefab)

        if (player.gameMode != GameMode.CREATIVE) item.subtract()
        player.playSound(loc, blockyInfo.placeSound, 1f, 1f)

        if (blockEntity.collisionRadius > 0) {
            val iterations = blockEntity.collisionRadius
            val locs: MutableList<Location> = ArrayList<Location>(iterations * iterations * iterations)
            for (x in -blockEntity.collisionRadius .. blockEntity.collisionRadius) {
                for (y in -blockEntity.collisionRadius .. blockEntity.collisionRadius) {
                    for (z in -blockEntity.collisionRadius .. blockEntity.collisionRadius) {
                        locs.add(Location(loc.world, loc.x + x.toDouble(), loc.y + y.toDouble(), loc.z + z.toDouble()))
                    }
                }
            }
            locs.forEach { if (it.block.type == Material.AIR) it.block.type = Material.BARRIER }
        }
    }

    @EventHandler
    fun EntityDamageByEntityEvent.onBreakingBlockyEntity() {
        val blocky = entity.toGeary().get<BlockyInfo>() ?: return
        if (!entity.toGeary().has<BlockyEntity>()) return
        if (blocky.isUnbreakable && (damager as Player).gameMode != GameMode.CREATIVE) isCancelled = true
    }
}