package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.components.BlockModelType
import com.mineinabyss.blocky.components.BlockyEntity
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.blocky.components.BlockyType
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.spawnFromPrefab
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.GameMode
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
        val blockyType = geary.get<BlockyType>() ?: return
        val blockyInfo = geary.get<BlockyInfo>() ?: return
        val blockEntity = geary.get<BlockyEntity>() ?: return
        val loc = clickedBlock?.location?.toCenterLocation() ?: return

        if (hand != EquipmentSlot.HAND) return
        if (action != Action.RIGHT_CLICK_BLOCK) return
        if (blockyType.blockModelType != BlockModelType.ENTITY) return

        val prefabKey = geary.getOrSetPersisting { BlockyEntity(blockEntity.prefab) }
        loc.spawnFromPrefab(prefabKey.prefab) ?: error("Prefab ${prefabKey.prefab} not found")

        if (player.gameMode != GameMode.CREATIVE) item.subtract()
        player.playSound(loc, blockyInfo.placeSound, 1f, 1f)
    }

    @EventHandler
    fun EntityDamageByEntityEvent.onBreakingBlockyEntity() {
        val blocky = entity.toGeary().get<BlockyInfo>() ?: return
        if (blocky.isUnbreakable && (damager as Player).gameMode != GameMode.CREATIVE) isCancelled = true
    }
}