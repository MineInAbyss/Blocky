package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.components.BlockyBlock
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.blocky.components.BlockyLight
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.looty.tracking.toGearyOrNull
import io.papermc.paper.event.entity.EntityInsideBlockEvent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent

class BlockyTripwireListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun BlockPhysicsEvent.cancelTripwirePhysics() {
        if (changedType == Material.TRIPWIRE) {
            block.state.update(true, false)
            isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun BlockPlaceEvent.onPlacingTripwire() {
        if (blockPlaced.type == Material.TRIPWIRE) {
            block.state.update(true, false)
            isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun EntityInsideBlockEvent.onEnterTripwire() {
        if (block.type == Material.TRIPWIRE) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun PlayerInteractEvent.onInteract() {
        if (action == Action.RIGHT_CLICK_BLOCK && clickedBlock?.type == Material.TRIPWIRE) {
            isCancelled = true
            val item = item ?: return
            var type = item.type
            if (item.type.isInteractable) return
            if (type == Material.LAVA_BUCKET) type = Material.LAVA
            if (type == Material.WATER_BUCKET) type = Material.WATER
            if (type == Material.TRIPWIRE || type.isBlock) {
                placeBlockyBlock(
                    player,
                    hand!!,
                    item,
                    clickedBlock!!,
                    blockFace,
                    item.toGearyOrNull(player)?.get<BlockyBlock>()?.getBlockyTripWireDataFromPrefab()
                        ?: Bukkit.createBlockData(type)
                )
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockBreakEvent.onBreakingBlockyTripwire() {
        if (block.type != Material.TRIPWIRE || isCancelled || !isDropItems) return

        val blockyWire = block.getPrefabFromBlock() ?: return
        val blockyInfo = blockyWire.get<BlockyInfo>() ?: return

        block.world.playSound(block.location, blockyInfo.breakSound, 1.0f, 0.8f)
        isDropItems = false
        handleBlockyDrops(block, player)
        block.state.update(true, false)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.prePlaceBlockyWire() {
        if (action != Action.RIGHT_CLICK_BLOCK) return

        val blockyWire = item?.toGearyOrNull(player) ?: return
        val info = blockyWire.get<BlockyInfo>() ?: return
        val wire = blockyWire.get<BlockyBlock>() ?: return
        val lightLevel = blockyWire.get<BlockyLight>()?.lightLevel

        val placedWire =
            placeBlockyBlock(
                player,
                hand!!,
                item!!,
                clickedBlock!!,
                blockFace,
                wire.getBlockyTripWireDataFromPrefab() ?: return
            ) ?: return

        placedWire.world.playSound(placedWire.location, info.placeSound, 1.0f, 0.8f)
        if (blockyWire.has<BlockyLight>()) createBlockLight(placedWire.location, lightLevel!!)
        isCancelled = true
    }
}