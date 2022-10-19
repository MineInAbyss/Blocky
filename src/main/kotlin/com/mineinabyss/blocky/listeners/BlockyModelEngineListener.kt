package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.components.core.BlockyModelEngine
import com.mineinabyss.blocky.helpers.getRotation
import com.mineinabyss.blocky.helpers.getYaw
import com.mineinabyss.geary.papermc.helpers.spawnFromPrefab
import com.mineinabyss.geary.prefabs.helpers.prefabs
import com.mineinabyss.looty.tracking.toGearyOrNull
import io.th0rgal.protectionlib.ProtectionLib
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class BlockyModelEngineListener : Listener {

    @EventHandler
    fun PlayerInteractEvent.onPlaceBlockyModelEngine() {
        if (action != Action.RIGHT_CLICK_BLOCK || hand != EquipmentSlot.HAND) return
        if (!ProtectionLib.canBuild(player, clickedBlock?.location)) return
        val gearyItem = item?.toGearyOrNull(player) ?: return
        gearyItem.get<BlockyModelEngine>() ?: return
        val entity = clickedBlock?.getRelative(blockFace)?.location?.toCenterLocation()?.spawnFromPrefab(gearyItem.prefabs.first()) ?: return
        val rotation = getRotation(player.location.yaw, false).rotateCounterClockwise()
        entity.setRotation(getYaw(rotation), 0f)
    }
}
