package com.mineinabyss.blocky.listeners

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.blocky.components.*
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.geary.helpers.with
import com.mineinabyss.looty.tracking.toGearyOrNull
import kotlinx.coroutines.delay
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.Powerable
import org.bukkit.block.data.type.Door
import org.bukkit.block.data.type.Gate
import org.bukkit.block.data.type.TrapDoor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class BlockyDoorListener : Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.onPrePlacingBlockyDoor() {
        if (action != Action.RIGHT_CLICK_BLOCK) return
        if (hand != EquipmentSlot.HAND) return

        val gearyItem = item?.toGearyOrNull(player) ?: return
        val blockyBlock = gearyItem.get<BlockyBlock>() ?: return
        val blockyLight = gearyItem.get<BlockyLight>()?.lightLevel
        val blockySound = gearyItem.get<BlockySound>()
        val against = clickedBlock ?: return
        if (against.type.isInteractable && against.getPrefabFromBlock()?.toEntity()
                ?.has<BlockyLight>() != true && !player.isSneaking
        ) return

        if (!gearyItem.has<BlockyInfo>()) return
        if (blockyBlock.blockType != BlockType.DOOR &&
            blockyBlock.blockType != BlockType.TRAPDOOR &&
            blockyBlock.blockType != BlockType.FENCEGATE
        ) return

        val newData =
            when (blockyBlock.blockType) {
                BlockType.DOOR -> blockyBlock.getBlockyDoor()
                BlockType.TRAPDOOR -> blockyBlock.getBlockyTrapDoor()
                BlockType.FENCEGATE -> blockyBlock.getBlockyFenceGate()
                else -> return
            }
        val placed = placeBlockyBlock(player, hand!!, item!!, against, blockFace, newData) ?: return

        if (gearyItem.has<BlockySound>()) placed.world.playSound(placed.location, blockySound!!.placeSound, 1.0f, 0.8f)
        if (gearyItem.has<BlockyLight>()) createBlockLight(placed.location, blockyLight!!)
    }

    // Sets normal doors to powered=false when placed next to a redstone source
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockPlaceEvent.onPlaceDoor() {
        if (itemInHand.toGearyOrNull(player) == null) {
            val data = blockPlaced.blockData.clone()
            when (data) {
                is Door -> {
                    data.facing = player.facing.oppositeFace
                    data.isOpen = (blockPlaced.isBlockPowered || blockPlaced.isBlockIndirectlyPowered)
                }
                is TrapDoor -> {
                    data.facing = player.facing.oppositeFace
                    data.isOpen = (blockPlaced.isBlockPowered || blockPlaced.isBlockIndirectlyPowered)
                }
                is Gate -> {
                    data.facing = player.facing.oppositeFace
                    data.isOpen = (blockPlaced.isBlockPowered || blockPlaced.isBlockIndirectlyPowered)
                    data.isInWall = blockPlaced.isConnectedToWall()
                }
                is Powerable -> {
                    data.isPowered = false
                }
            }
            blockPlaced.setBlockData(data, false)
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockPlaceEvent.onPlaceBlockyDoor() {
        itemInHand.toGearyOrNull(player)?.with { blockyBlock: BlockyBlock ->
            when (blockyBlock.blockType) {
                BlockType.DOOR -> {
                    getDoorType(blockyBlock.blockId)?.let { block.setType(it, false) } ?: return
                    val data = blockyBlock.getBlockyDoor()
                    data.isOpen = (blockPlaced.isBlockPowered || blockPlaced.isBlockIndirectlyPowered)
                    block.setBlockData(data, false)
                }
                BlockType.TRAPDOOR -> {
                    getTrapDoorType(blockyBlock.blockId)?.let { block.setType(it, false) } ?: return
                    val data = blockyBlock.getBlockyTrapDoor()
                    data.isOpen = (blockPlaced.isBlockPowered || blockPlaced.isBlockIndirectlyPowered)
                    block.setBlockData(data, false)
                }
                BlockType.FENCEGATE -> {
                    getFenceGate(blockyBlock.blockId)?.let { block.setType(it, false) } ?: return
                    val data = blockyBlock.getBlockyFenceGate()
                    data.isOpen = (blockPlaced.isBlockPowered || blockPlaced.isBlockIndirectlyPowered)
                    data.isInWall = block.isConnectedToWall()
                    block.setBlockData(data, false)
                }
                else -> return
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockBreakEvent.onOpenBlockyDoor() {
        block.getPrefabFromBlock()?.toEntity()?.with() { blockyBlock: BlockyBlock ->
            when (blockyBlock.blockType) {
                BlockType.DOOR -> {
                    breakBlockyBlock(block, player)
                    block.setType(Material.AIR, false)
                    if ((block.blockData as Door).half == Bisected.Half.BOTTOM)
                        block.getRelative(BlockFace.UP).setType(Material.AIR, false)
                    else block.getRelative(BlockFace.DOWN).setType(Material.AIR, false)
                }
                BlockType.TRAPDOOR -> {
                    breakBlockyBlock(block, player)
                    block.setType(Material.AIR, false)
                }
                BlockType.FENCEGATE -> {
                    breakBlockyBlock(block, player)
                    block.setType(Material.AIR, false)
                }
                else -> return
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun BlockPhysicsEvent.onRedstoneBlockyDoor() {
        when (val data = block.blockData.clone()) {
            is Gate -> {
                if (sourceBlock == block) return
                isCancelled = true
                data.isPowered = data.isPowered

                blockyPlugin.launch {
                    delay(1)
                    block.setBlockData(data, false)
                }
                block.state.update(true, false)
            }
            is TrapDoor -> {
                if (sourceBlock == block) return
                isCancelled = true
                block.state.update(true, false)
                data.isPowered = data.isPowered

                blockyPlugin.launch {
                    delay(1)
                    block.setBlockData(data, false)
                }
            }
            is Door -> {
                isCancelled = true
                if (sourceBlock == block) return
                if (sourceBlock.blockData !is Powerable) return
                val nextBlock =
                    if (data.half == Bisected.Half.BOTTOM) block.getRelative(BlockFace.UP)
                    else block.getRelative(BlockFace.DOWN)
                val nextData = nextBlock.blockData.clone() as? Door ?: return
                block.state.update(true, false)
                nextBlock.state.update(true, false)


                data.isPowered = data.isPowered
                data.isOpen = !data.isOpen
                nextData.isPowered = data.isPowered
                nextData.isOpen = data.isOpen

                blockyPlugin.launch {
                    delay(1)
                    block.setBlockData(data, false)
                    nextBlock.setBlockData(nextData, false)
                }
            }
            else -> return
        }
    }
}
