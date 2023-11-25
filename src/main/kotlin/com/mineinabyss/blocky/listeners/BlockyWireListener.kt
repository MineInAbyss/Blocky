package com.mineinabyss.blocky.listeners

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.blocky.api.BlockyBlocks.isBlockyBlock
import com.mineinabyss.blocky.api.events.block.BlockyBlockPlaceEvent
import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.features.wire.BlockyTallWire
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.blocky.helpers.GenericHelpers.isInteractable
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.prefabKey
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import io.papermc.paper.event.block.BlockBreakBlockEvent
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

class BlockyWireListener : Listener {

    @EventHandler
    fun BlockPistonExtendEvent.cancelBlockyPiston() {
        blocks.filter { it.type == Material.TRIPWIRE }.forEach { wire ->
            val gearyEntity = wire.prefabKey ?: return@forEach
            gearyItems.createItem(gearyEntity)?.let { wire.world.dropItemNaturally(wire.location, it) }
            wire.type = Material.AIR
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun PlayerInteractEvent.onInteract() {
        if (action != Action.RIGHT_CLICK_BLOCK || clickedBlock?.type != Material.TRIPWIRE) return
        if (hand != EquipmentSlot.HAND) return

        val (block, item, hand) = (clickedBlock ?: return) to (item ?: return) to (hand ?: return)
        val blockyBlock = player.gearyInventory?.get(hand)?.get<SetBlock>() ?: return
        var type = item.type

        if (type == Material.LAVA_BUCKET) type = Material.LAVA
        if (type == Material.WATER_BUCKET) type = Material.WATER
        if (type != Material.STRING && !type.isBlock) return

        placeBlockyBlock(player, hand, item, block.getRelative(BlockFace.DOWN), blockFace, blockyBlock.getBlockyTripWire())
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun BlockBreakEvent.onBreakingBlockyTripwire() {
        if (block.type != Material.TRIPWIRE || !block.isBlockyBlock) return
        breakWireBlock(block, player)
        isDropItems = false
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun BlockBreakBlockEvent.onWaterCollide() {
        if (block.type != Material.TRIPWIRE) return
        breakWireBlock(block, null)
        drops.removeIf { it.type == Material.STRING }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.prePlaceBlockyWire() {
        val (block, item, hand) = (clickedBlock ?: return) to (item ?: return) to (hand ?: return)

        if (action != Action.RIGHT_CLICK_BLOCK || hand != EquipmentSlot.HAND) return
        if (blockFace == BlockFace.UP && block.blockData is Tripwire) {
            isCancelled = true
            return
        } else if (!player.isSneaking && block.isInteractable()) return

        val blockyWire = player.gearyInventory?.get(hand) ?: return
        val wireBlock = blockyWire.get<SetBlock>() ?: return
        if (wireBlock.blockType != SetBlock.BlockType.WIRE) return

        placeBlockyBlock(player, hand, item, block, blockFace, wireBlock.getBlockyTripWire())
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun BlockyBlockPlaceEvent.onPlaceTallWire() {
        val blockAbove = block.getRelative(BlockFace.UP)

        if (!blockAbove.isReplaceable) return
        if (blockyBlock?.blockType != SetBlock.BlockType.WIRE) return
        if (block.toGearyOrNull()?.has<BlockyTallWire>() != true) return

        blockAbove.type = Material.TRIPWIRE
        blockAbove.persistentDataContainer.encode(BlockyTallWire(block.location))
        block.persistentDataContainer.encode(BlockyTallWire(blockAbove.location))
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun BlockBreakEvent.onBreakTallWire() {
        if (block.type != Material.TRIPWIRE) return
        if (block.toGearyOrNull()?.has<BlockyTallWire>() == true) return

        val mainWire = block.persistentDataContainer.decode<BlockyTallWire>()?.baseWire ?: return
        if (mainWire.type != Material.TRIPWIRE) return
        if (mainWire.toGearyOrNull()?.has<BlockyTallWire>() != true) return
        breakWireBlock(mainWire, player)
        isDropItems = false
    }

}
