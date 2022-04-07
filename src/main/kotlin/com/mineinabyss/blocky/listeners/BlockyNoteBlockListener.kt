package com.mineinabyss.blocky.listeners

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.blocky.components.*
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.blocky.systems.BlockyBreakingPacketAdapter
import com.mineinabyss.idofront.entities.rightClicked
import com.mineinabyss.looty.tracking.toGearyOrNull
import com.okkero.skedule.schedule
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.player.PlayerInteractEvent

val protocolManager: ProtocolManager = ProtocolLibrary.getProtocolManager()

class BlockyNoteBlockListener : Listener {

    @EventHandler
    fun BlockPistonExtendEvent.cancelBlockyPiston() {
        if (blocks.stream().anyMatch { it.type == Material.NOTE_BLOCK }) isCancelled = true

    }

    @EventHandler
    fun BlockPistonRetractEvent.cancelBlockyPiston() {
        if (blocks.stream().anyMatch { it.type == Material.NOTE_BLOCK }) isCancelled = true
    }

    @EventHandler
    fun NotePlayEvent.cancelBlockyNotes() {
        isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.onChangingNote() {
        if (clickedBlock?.type == Material.NOTE_BLOCK && rightClicked) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockPhysicsEvent.onBlockPhysics() {
        val aboveBlock = block.location.add(0.0, 1.0, 0.0).block
        if (aboveBlock.type == Material.NOTE_BLOCK) {
            isCancelled = true
            updateAndCheck(block.location)
        }
        if (block.type == Material.NOTE_BLOCK) {
            isCancelled = true
            block.state.update(true, false)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun PlayerInteractEvent.onPrePlacingBlockyBlock() {
        val gearyItem = player.inventory.itemInMainHand.toGearyOrNull(player) ?: return
        val blockyBlock = gearyItem.get<BlockyBlock>() ?: return
        val blockyInfo = gearyItem.get<BlockyInfo>() ?: return
        val blockyLight = gearyItem.get<BlockyLight>()?.lightLevel
        val blockySound = gearyItem.get<BlockySound>()
        if (action != Action.RIGHT_CLICK_BLOCK) return
        if (blockyBlock.blockType != BlockType.CUBE) return

        val against = clickedBlock ?: return
        val placed =
            placeBlockyBlock(player, hand!!, item!!, against, blockFace, blockyBlock.getBlockyNoteBlockDataFromPrefab())
                ?: return
        if (gearyItem.has<BlockySound>()) placed.world.playSound(placed.location, blockySound!!.placeSound, 1.0f,  0.8f)
        if (gearyItem.has<BlockyLight>()) createBlockLight(placed.location, blockyLight!!)
        isCancelled = true

    }

    @EventHandler
    fun BlockPlaceEvent.onPlacingBlockyBlock() {
        val gearyItem = itemInHand.toGearyOrNull(player) ?: return
        val blockyBlock = gearyItem.get<BlockyBlock>() ?: return
        val blockyInfo = gearyItem.get<BlockyInfo>() ?: return

        if (blockyBlock.blockType == BlockType.CUBE) {
            block.setBlockData(block.getBlockyBlockDataFromItem(blockyBlock.blockId), false)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockBreakEvent.onBreakingBlockyBlock() {
        if (block.type != Material.NOTE_BLOCK || isCancelled || !isDropItems) return

        val prefab = block.getPrefabFromBlock()?.toEntity() ?: return
        val blockyInfo = prefab.get<BlockyInfo>() ?: return
        val blockySound = prefab.get<BlockySound>()

        if (blockyInfo.isUnbreakable && player.gameMode != GameMode.CREATIVE) isCancelled = true

        blockyPlugin.schedule {
            protocolManager.addPacketListener(
                BlockyBreakingPacketAdapter(player, mutableMapOf(Pair(block.location, this.scheduler)))
            )
        }

        if (prefab.has<BlockySound>()) block.world.playSound(block.location, blockySound!!.breakSound, 1.0f,  0.8f)
        isDropItems = false
        handleBlockyDrops(block, player)
        removeBlockLight(block.location)
    }
}