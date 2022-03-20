package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.components.BlockyEntity
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.spawnFromPrefab
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot


class BlockyEntityListener : Listener {

    //TODO Make blockFace check for placing entity better
    @EventHandler
    fun PlayerInteractEvent.onPlacingBlockyEntity() {
        val item = player.inventory.itemInMainHand
        val geary = item.toGearyOrNull(player) ?: return
        val blockyInfo = geary.get<BlockyInfo>() ?: return
        val blockEntity = geary.get<BlockyEntity>() ?: return
        val newLoc =
            if (blockFace != BlockFace.DOWN) player.getLastTwoTargetBlocks(null, 6)
                .first()?.location?.toCenterLocation() ?: return
            else if (blockFace == BlockFace.DOWN) player.getLastTwoTargetBlocks(null, 6)
                .last()?.location?.toCenterLocation()
                ?.apply { y -= blockEntity.collisionRadius }
                ?: return
            else return

        if (hand != EquipmentSlot.HAND) return
        if (action != Action.RIGHT_CLICK_BLOCK) return
        newLoc.block.type.broadcastVal()
        interactionPoint!!.block.type.broadcastVal()
        if (newLoc != interactionPoint?.toCenterLocation() && blockFace != BlockFace.DOWN) return
        else if (newLoc != interactionPoint?.toCenterLocation()?.apply { y -= blockEntity.collisionRadius }
                && blockFace == BlockFace.DOWN) return

        newLoc.spawnFromPrefab(blockEntity.entityPrefab)

        if (player.gameMode != GameMode.CREATIVE) item.subtract()
        player.playSound(newLoc, blockyInfo.placeSound, 1f, 1f)

        if (blockEntity.collisionRadius > 0) {
            val iterations = blockEntity.collisionRadius
            val locs: MutableList<Location> = ArrayList(iterations * iterations * iterations)
            for (x in -blockEntity.collisionRadius..blockEntity.collisionRadius) {
                for (y in -blockEntity.collisionRadius..blockEntity.collisionRadius) {
                    for (z in -blockEntity.collisionRadius..blockEntity.collisionRadius) {
                        locs.add(
                            Location(
                                newLoc.world,
                                newLoc.x + x.toDouble(),
                                newLoc.y + y.toDouble(),
                                newLoc.z + z.toDouble()
                            )
                        )
                    }
                }
            }
            locs.forEach { if (it.block.type == Material.AIR) it.block.type = Material.BARRIER }
        }
    }

    //TODO This does nothing, use whatever mobzy uses but with the below checks
    @EventHandler
    fun EntityDamageByEntityEvent.onBreakingBlockyEntity() {
        val blocky = entity.toGeary().get<BlockyInfo>() ?: return
        if (!entity.toGeary().has<BlockyEntity>()) return
        if (blocky.isUnbreakable && (damager as Player).gameMode != GameMode.CREATIVE) isCancelled = true
    }
}