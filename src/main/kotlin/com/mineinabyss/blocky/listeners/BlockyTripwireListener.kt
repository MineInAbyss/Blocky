package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.components.BlockyBlock
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.blocky.components.BlockyLight
import com.mineinabyss.blocky.components.BlockySound
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
            isCancelled = true
            block.state.update(true, false)
            sourceBlock.state.update(true, false)
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun BlockPlaceEvent.onPlacingTripwire() {
        if (blockPlaced.type == Material.TRIPWIRE) {
            //isCancelled = true
            block.state.update(true, false)
            blockAgainst.state.update(true, false)
            updateAndCheck(block.location)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun EntityInsideBlockEvent.onEnterTripwire() {
        if (block.type == Material.TRIPWIRE) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerInteractEvent.onInteract() {
        if (action == Action.RIGHT_CLICK_BLOCK && clickedBlock?.type == Material.TRIPWIRE) {
            isCancelled = true
            val item = item ?: return
            var type = item.type
            if (item.type.isInteractable) return
            if (type == Material.LAVA_BUCKET) type = Material.LAVA
            if (type == Material.WATER_BUCKET) type = Material.WATER
            if (type == Material.TRIPWIRE || type == Material.STRING|| type.isBlock) {
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

    @EventHandler(priority = EventPriority.HIGHEST)
    fun BlockBreakEvent.onBreakingBlockyTripwire() {
        if (block.type != Material.TRIPWIRE || !isDropItems) return

        val blockyWire = block.getPrefabFromBlock() ?: return
        val blockyInfo = blockyWire.get<BlockyInfo>() ?: return
        val blockySound = blockyWire.get<BlockySound>()
        block.state.update(true, false)

        if (blockyWire.has<BlockySound>()) block.world.playSound(block.location, blockySound!!.placeSound, 1.0f,  0.8f)
        if (blockyWire.has<BlockyLight>()) removeBlockLight(block.location)

        isDropItems = false
        handleBlockyDrops(block, player)
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun PlayerInteractEvent.prePlaceBlockyWire() {
        if (action != Action.RIGHT_CLICK_BLOCK) return

        val blockyWire = item?.toGearyOrNull(player) ?: return
        val info = blockyWire.get<BlockyInfo>() ?: return
        val wire = blockyWire.get<BlockyBlock>() ?: return
        val lightLevel = blockyWire.get<BlockyLight>()?.lightLevel
        val sound = blockyWire.get<BlockySound>()

        val placedWire =
            placeBlockyBlock(
                player,
                hand!!,
                item!!,
                clickedBlock!!,
                blockFace,
                wire.getBlockyTripWireDataFromPrefab() ?: return
            ) ?: return


        if (blockyWire.has<BlockySound>()) placedWire.world.playSound(placedWire.location, sound!!.placeSound, 1.0f,  0.8f)
        if (blockyWire.has<BlockyLight>()) createBlockLight(placedWire.location, lightLevel!!)
        isCancelled = true
    }
}