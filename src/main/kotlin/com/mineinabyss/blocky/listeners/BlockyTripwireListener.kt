package com.mineinabyss.blocky.listeners

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.blocky.components.BlockType
import com.mineinabyss.blocky.components.BlockyBlock
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.blocky.components.BlockyLight
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.looty.LootyFactory
import com.mineinabyss.looty.tracking.toGearyOrNull
import io.papermc.paper.event.block.BlockBreakBlockEvent
import io.papermc.paper.event.entity.EntityInsideBlockEvent
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.GameMode
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

    @EventHandler(priority = EventPriority.LOWEST)
    fun BlockPhysicsEvent.cancelTripwirePhysics() {
        if (changedType == Material.TRIPWIRE) {
            isCancelled = true
            block.state.update(true, false)
        }

        BlockFace.values().filter { it.isCardinal() }.forEach { f ->
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
            blockyPlugin.launch {
                delay(1)
                fixClientsideUpdate(block.location)
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun PlayerInteractEvent.onInteract() {
        if (action == Action.RIGHT_CLICK_BLOCK && clickedBlock?.type == Material.TRIPWIRE) {
            if (hand != EquipmentSlot.HAND) return

            val item = item ?: return
            val blockyBlock = item.toGearyOrNull(player)?.get<BlockyBlock>() ?: return
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
                    ) ?: return
                } ?: return
            }
            player.swingMainHand()
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun BlockBreakEvent.onBreakingBlockyTripwire() {
        if (block.type != Material.TRIPWIRE) return
        BlockFace.values().forEach { face ->
            if (block.getRelative(face).type != Material.TRIPWIRE) return@forEach
            if (block.isBlockyBlock() && player.gameMode != GameMode.CREATIVE)
                block.drops.forEach { player.world.dropItemNaturally(block.location, it) }

            block.setType(Material.AIR, false)
            blockyPlugin.launch {
                delay(1)
                fixClientsideUpdate(block.location)
            }
        }

        breakTripwireBlock(block, player)
        isDropItems = false
    }

    @EventHandler
    fun BlockBreakBlockEvent.onWaterCollide() {
        if (block.type == Material.TRIPWIRE) {
            breakTripwireBlock(block, null)
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
        }
        if (clickedBlock.type.isInteractable && !player.isSneaking) return

        // Fixes tripwire updating when placing blocks next to it
        if (item?.type?.isBlock == true && item?.toGearyOrNull(player)?.has<BlockyBlock>() != true) {
            BlockFace.values().filter { !it.isCartesian && it.modZ == 0 }.forEach {
                if (clickedBlock.getRelative(it).getGearyEntityFromBlock() == null) return@forEach
                placeBlockyBlock(player, hand!!, item!!, clickedBlock, blockFace, Bukkit.createBlockData(item!!.type))
                fixClientsideUpdate(clickedBlock.location)
            }
        }

        val blockyWire = item?.toGearyOrNull(player) ?: return
        val wireBlock = blockyWire.get<BlockyBlock>() ?: return
        if (wireBlock.blockType != BlockType.GROUND) return
        if (!blockyWire.has<BlockyInfo>()) return

        val lightLevel = blockyWire.get<BlockyLight>()?.lightLevel
        val placedWire =
            placeBlockyBlock(player, hand!!, item!!, clickedBlock, blockFace, wireBlock.getBlockyTripWire()) ?: return

        if (blockyWire.has<BlockyLight>())
            createBlockLight(placedWire.location, lightLevel!!)

        blockyPlugin.launch {
            delay(1)
            fixClientsideUpdate(placedWire.location)
        }
    }
}
