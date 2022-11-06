package com.mineinabyss.blocky.listeners

import com.github.shynixn.mccoroutine.bukkit.launch
import com.jeff_media.morepersistentdatatypes.DataType
import com.mineinabyss.blocky.api.events.block.BlockyBlockPlaceEvent
import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.blocky.components.core.BlockyBlock
import com.mineinabyss.blocky.components.core.BlockyBlock.BlockType
import com.mineinabyss.blocky.components.core.BlockyInfo
import com.mineinabyss.blocky.components.features.BlockyTallWire
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.looty.LootyFactory
import com.mineinabyss.looty.tracking.toGearyOrNull
import io.papermc.paper.event.block.BlockBreakBlockEvent
import io.papermc.paper.event.entity.EntityInsideBlockEvent
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Tripwire
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class BlockyWireListener : Listener {

    @EventHandler
    fun BlockPistonExtendEvent.cancelBlockyPiston() {
        blocks.filter { it.type == Material.TRIPWIRE }.forEach { wire ->
            val gearyEntity = wire.prefabKey ?: return@forEach
            LootyFactory.createFromPrefab(gearyEntity)?.let { wire.world.dropItemNaturally(wire.location, it) }
            wire.type = Material.AIR
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun BlockPhysicsEvent.cancelTripwirePhysics() {
        if (changedType == Material.TRIPWIRE) {
            isCancelled = true
            block.state.update(true, false)
        }

        BlockFace.values().filter { it.isCardinal }.forEach { f ->
            val changed = block.getRelative(f)
            if (changed.type != Material.TRIPWIRE) return@forEach

            blockyPlugin.launch {
                val data = changed.blockData.clone()
                delay(1)
                changed.setBlockData(data, false)
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun EntityInsideBlockEvent.onEnterTripwire() {
        if (block.type == Material.TRIPWIRE) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun BlockPlaceEvent.onPlacingTripwire() {
        if (blockPlaced.type == Material.TRIPWIRE) {
            block.state.update(true, false)
            blockAgainst.state.update(true, false)

            if (itemInHand.toGearyOrNull(player)?.has<BlockyBlock>() != true)
                block.setBlockData(Bukkit.createBlockData(Material.TRIPWIRE), false)
            block.fixClientsideUpdate()
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun PlayerInteractEvent.onInteract() {
        if (action == Action.RIGHT_CLICK_BLOCK && clickedBlock?.type == Material.TRIPWIRE) {
            if (hand != EquipmentSlot.HAND) return

            val item = item ?: return
            val hand = hand ?: return
            val blockyBlock = item.toGearyOrNull(player)?.get<BlockyBlock>() ?: return
            var type = item.type
            if (type == Material.LAVA_BUCKET) type = Material.LAVA
            if (type == Material.WATER_BUCKET) type = Material.WATER
            if (type == Material.TRIPWIRE || type == Material.STRING || type.isBlock) {
                clickedBlock?.getRelative(BlockFace.DOWN)?.let { block ->
                    placeBlockyBlock(player, hand, item, block, blockFace, blockyBlock.getBlockyTripWire()) ?: return
                } ?: return
            }
            player.swingMainHand()
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun BlockBreakEvent.onBreakingBlockyTripwire() {
        if (block.type != Material.TRIPWIRE || !block.isBlockyBlock) return

        breakWireBlock(block, player)
        isDropItems = false
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun BlockBreakBlockEvent.onWaterCollide() {
        if (block.type == Material.TRIPWIRE) {
            breakWireBlock(block, null)
            drops.removeIf { it.type == Material.STRING }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.prePlaceBlockyWire() {
        if (action != Action.RIGHT_CLICK_BLOCK) return
        if (hand != EquipmentSlot.HAND) return
        val clickedBlock = clickedBlock ?: return
        if (blockFace == BlockFace.UP && player.world.getBlockData(clickedBlock.location) is Tripwire) {
            isCancelled = true
            return
        } else if (clickedBlock.type.isInteractable && !player.isSneaking) return

        // Fixes tripwire updating when placing blocks next to it
        if (item?.type?.isBlock == true && item?.toGearyOrNull(player)?.has<BlockyBlock>() != true) {
            BlockFace.values().filter { !it.isCartesian && it.modZ == 0 }.forEach {
                if (clickedBlock.getRelative(it).gearyEntity == null) return@forEach
                placeBlockyBlock(player, hand!!, item!!, clickedBlock, blockFace, Bukkit.createBlockData(item!!.type))
                clickedBlock.fixClientsideUpdate()
            }
        }

        val blockyWire = item?.toGearyOrNull(player) ?: return
        val wireBlock = blockyWire.get<BlockyBlock>() ?: return
        if (wireBlock.blockType != BlockType.WIRE) return
        if (!blockyWire.has<BlockyInfo>()) return

        val placedWire =
            placeBlockyBlock(player, hand!!, item!!, clickedBlock, blockFace, wireBlock.getBlockyTripWire()) ?: return

        placedWire.fixClientsideUpdate()
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun BlockyBlockPlaceEvent.onPlaceTallWire() {
        val blockAbove = block.getRelative(BlockFace.UP)

        if (!blockAbove.isReplaceable) return
        if (blockyBlock?.blockType != BlockType.WIRE) return
        if (block.gearyEntity?.has<BlockyTallWire>() != true) return

        blockAbove.type = Material.TRIPWIRE
        blockAbove.persistentDataContainer.set(BlockyTallWire().getKey(), DataType.LOCATION, block.location)
        block.persistentDataContainer.set(BlockyTallWire().getKey(), DataType.LOCATION, blockAbove.location)
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun BlockBreakEvent.onBreakTallWire() {
        if (block.type != Material.TRIPWIRE) return
        if (block.gearyEntity?.has<BlockyTallWire>() == true) return

        val mainWire = block.persistentDataContainer.get(BlockyTallWire().getKey(), DataType.LOCATION)?.block ?: return
        if (mainWire.type != Material.TRIPWIRE) return
        if (mainWire.gearyEntity?.has<BlockyTallWire>() != true) return
        breakWireBlock(mainWire, player)
        isDropItems = false
    }
}