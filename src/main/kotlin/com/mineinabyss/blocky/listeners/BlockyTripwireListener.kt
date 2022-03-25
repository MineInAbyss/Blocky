package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.components.BlockyBlock
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.looty.tracking.toGearyOrNull
import io.papermc.paper.event.entity.EntityInsideBlockEvent
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

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
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.prePlaceBlockyWire() {
        if (action != Action.RIGHT_CLICK_BLOCK) return

        val blockyWire = item?.toGearyOrNull(player) ?: return
        val info = blockyWire.get<BlockyInfo>() ?: return
        val wire = blockyWire.get<BlockyBlock>() ?: return
        wire.getBlockyTripWireDataFromPrefab().broadcastVal()


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
        isCancelled = true
    }

    fun placeBlockyBlock(
        player: Player,
        hand: EquipmentSlot,
        item: ItemStack,
        against: Block,
        face: BlockFace,
        newData: BlockData
    ): Block? {
        val targetBlock: Block

        if (REPLACEABLE_BLOCKS.contains(against.type)) targetBlock = against
        else {
            targetBlock = against.getRelative(face)
            if (!targetBlock.type.isAir && targetBlock.type != Material.WATER && targetBlock.type != Material.LAVA) return null
        }

        if (isStandingInside(player, targetBlock)) return null

        val currentData = targetBlock.blockData
        targetBlock.setBlockData(newData, false)

        val currentBlockState = targetBlock.state

        val blockPlaceEvent = BlockPlaceEvent(targetBlock, currentBlockState, against, item, player, true, hand)
        Bukkit.getPluginManager().callEvent(blockPlaceEvent)

        if (!blockPlaceEvent.canBuild() || blockPlaceEvent.isCancelled) {
            targetBlock.setBlockData(currentData, false) // false to cancel physic
            return null
        }

        if (player.gameMode != GameMode.CREATIVE) item.subtract(1)
        return targetBlock
    }
}