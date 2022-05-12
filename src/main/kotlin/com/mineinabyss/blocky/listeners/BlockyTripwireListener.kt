package com.mineinabyss.blocky.listeners

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.blocky.components.*
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.looty.LootyFactory
import com.mineinabyss.looty.tracking.toGearyOrNull
import io.papermc.paper.event.entity.EntityInsideBlockEvent
import kotlinx.coroutines.delay
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Tripwire
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class BlockyTripwireListener : Listener {

    @EventHandler
    fun BlockPistonExtendEvent.cancelBlockyPiston() {
        val wireList = blocks.stream().filter { it.type == Material.TRIPWIRE }.toList()

        wireList.forEach { wire ->
            val gearyEntity = wire.getPrefabFromBlock() ?: return@forEach
            wire.world.dropItemNaturally(wire.location, LootyFactory.createFromPrefab(gearyEntity)!!)
            wire.type = Material.AIR
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun BlockPhysicsEvent.cancelTripwirePhysics() {
        if (changedType == Material.TRIPWIRE) {
            isCancelled = true
            block.state.update(true, false)
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun BlockPlaceEvent.onPlacingTripwire() {
        if (blockPlaced.type == Material.TRIPWIRE) {

            block.state.update(true, false)
            blockAgainst.state.update(true, false)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun EntityInsideBlockEvent.onEnterTripwire() {
        if (block.type == Material.TRIPWIRE) isCancelled = true
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun PlayerInteractEvent.onInteract() {
        if (action == Action.RIGHT_CLICK_BLOCK && clickedBlock?.type == Material.TRIPWIRE) {
            if (hand != EquipmentSlot.HAND) return

            val item = item ?: return
            val blockyBlock = item.toGearyOrNull(player)?.blockyBlock ?: return
            var type = item.type
            if (type == Material.LAVA_BUCKET) type = Material.LAVA
            if (type == Material.WATER_BUCKET) type = Material.WATER
            if (type == Material.TRIPWIRE || type == Material.STRING || type.isBlock) {
                clickedBlock?.getRelative(BlockFace.DOWN)?.let { block ->
                    placeBlockyBlock(
                        player,
                        hand!!,
                        item,
                        block,
                        blockFace,
                        blockyBlock.getBlockyTripWire()
                    )
                    blockyPlugin.launch {
                        delay(1)
                        fixClientsideUpdate(block.location)
                    }
                } ?: return
            }
            player.swingMainHand()
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun BlockBreakEvent.onBreakingBlockyTripwire() {
        val blockAbove = block.getRelative(BlockFace.UP)

        if (block.type == Material.TRIPWIRE) breakTripwireBlock(block, player)
        if (blockAbove.type == Material.TRIPWIRE) breakTripwireBlock(blockAbove, player)
        else return

        isDropItems = false
        blockyPlugin.launch {
            delay(1)
            fixClientsideUpdate(block.location)
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.prePlaceBlockyWire() {
        if (action != Action.RIGHT_CLICK_BLOCK) return
        if (hand != EquipmentSlot.HAND) return
        if (blockFace == BlockFace.UP && player.world.getBlockData(clickedBlock?.location!!) is Tripwire) {
            isCancelled = true
            return
        }

        val blockyWire = item?.toGearyOrNull(player) ?: return
        val wireBlock = blockyWire.blockyBlock ?: return
        if (wireBlock.blockType != BlockType.GROUND) return
        if (!blockyWire.hasBlockyInfo) return

        val lightLevel = blockyWire.blockyLight?.lightLevel
        val placedWire =
            placeBlockyBlock(player, hand!!, item!!, clickedBlock!!, blockFace, wireBlock.getBlockyTripWire()) ?: return

        if (blockyWire.hasBlockySound) placedWire.world.playSound(
            placedWire.location,
            blockyWire.blockySound!!.placeSound,
            1.0f,
            0.8f
        )
        if (blockyWire.hasBlockyLight) createBlockLight(placedWire.location, lightLevel!!)

        blockyPlugin.launch {
            delay(1)
            fixClientsideUpdate(placedWire.location)
        }
    }
}