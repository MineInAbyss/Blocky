package com.mineinabyss.blocky.listeners

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.blocky.blockMap
import com.mineinabyss.blocky.blockyConfig
import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.blocky.components.core.BlockyBlock
import com.mineinabyss.blocky.components.core.BlockyBlock.BlockType
import com.mineinabyss.blocky.components.core.BlockyInfo
import com.mineinabyss.blocky.components.features.BlockyBurnable
import com.mineinabyss.blocky.components.features.BlockyLight
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.idofront.entities.rightClicked
import com.mineinabyss.looty.tracking.toGearyOrNull
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.GameEvent
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.GenericGameEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataType

class BlockyNoteBlockListener : Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun NotePlayEvent.cancelBlockyNotes() {
        val defaultData = Bukkit.createBlockData(Material.NOTE_BLOCK) as NoteBlock
        val data = block.blockData as NoteBlock
        //TODO data != defaultData might work here
        if (data.instrument != defaultData.instrument || data.note != defaultData.note) {
            isCancelled = true
        } else {
            // Update  the note stored in chunk and get instrument from below block
            note = block.updateBlockyNote()
            instrument = block.getBlockyInstrument()
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockPistonExtendEvent.cancelBlockyPiston() {
        if (blocks.any { it.isBlockyNoteBlock }) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockPistonRetractEvent.cancelBlockyPiston() {
        if (blocks.any { it.isBlockyNoteBlock }) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockBurnEvent.onBurnBlockyNoteBlock() {
        if (!block.isBlockyNoteBlock) return
        if (block.gearyEntity?.has<BlockyBurnable>() != true) isCancelled = true
    }

    // If not restoreFunctionality handle interaction if vanilla block otherwise return cuz vanilla handles it
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun PlayerInteractEvent.onChangingNote() {
        val block = clickedBlock ?: return
        if (block.type != Material.NOTE_BLOCK) return
        if (blockyConfig.noteBlocks.restoreFunctionality && block.isVanillaNoteBlock) return

        if (rightClicked) isCancelled = true
        if (block.isVanillaNoteBlock) {
            if (rightClicked) block.updateBlockyNote()
            block.playBlockyNoteBlock()
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun PlayerInteractEvent.onInteractBlockyNoteBlock() {
        val block = clickedBlock ?: return
        val item = item ?: return
        val type = item.clone().type

        if (action != Action.RIGHT_CLICK_BLOCK || block.type != Material.NOTE_BLOCK || hand != EquipmentSlot.HAND) return
        if (block.type.isInteractable && block.type != Material.NOTE_BLOCK) return
        if (type.isBlock) placeBlockyBlock(player, hand!!, item, block, blockFace, Bukkit.createBlockData(type))
    }

    // Handle playing the sound. If BlockData isnt in map, it means this should be handled by vanilla
    // AKA restoreFunctionality enabled and normal block
    @EventHandler(priority = EventPriority.HIGHEST)
    fun BlockPhysicsEvent.onBlockPhysics() {
        if (block.type != Material.NOTE_BLOCK) return
        if (block.blockData !in blockMap) return

        isCancelled = true
        block.state.update(true, false)

        if (block.isBlockIndirectlyPowered) {
            block.playBlockyNoteBlock()
        }
        if (block.getRelative(BlockFace.UP).type == Material.NOTE_BLOCK)
            block.updateNoteBlockAbove()
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun GenericGameEvent.disableRedstone() {
        val block = location.block
        val data = block.blockData.clone() as? NoteBlock ?: return

        if (!block.isBlockyNoteBlock || event != GameEvent.NOTE_BLOCK_PLAY) return

        blockyPlugin.launch {
            delay(1)
            block.setBlockData(data, false)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun PlayerInteractEvent.onPrePlacingBlockyNoteBlock() {
        if (action != Action.RIGHT_CLICK_BLOCK) return
        if (hand != EquipmentSlot.HAND) return

        val gearyItem = item?.toGearyOrNull(player) ?: return
        val blockyBlock = gearyItem.get<BlockyBlock>() ?: return
        val blockyLight = gearyItem.get<BlockyLight>()?.lightLevel
        val against = clickedBlock ?: return

        if ((against.type.isInteractable && against.gearyEntity == null) && !player.isSneaking) return
        if (!gearyItem.has<BlockyInfo>()) return
        if (blockyBlock.blockType != BlockType.NOTEBLOCK) return

        val placed =
            placeBlockyBlock(player, hand!!, item!!, against, blockFace, gearyItem.getBlockyNoteBlock(blockFace))
                ?: return
        if (gearyItem.has<BlockyLight>()) handleLight.createBlockLight(placed.location, blockyLight!!)
    }

    // Set default note of normal noteblock only if not restoreFunctionality is enabled
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockPlaceEvent.onPlaceNoteBlock() {
        if (!blockyConfig.noteBlocks.restoreFunctionality && blockPlaced.isVanillaNoteBlock) {
            blockPlaced.customBlockData.set(NOTEBLOCK_KEY, PersistentDataType.INTEGER, 0)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun EntityExplodeEvent.onExplodingBlocky() {
        blockList().forEach { block ->
            val prefab = block.gearyEntity ?: return@forEach
            if (!block.isBlockyNoteBlock) return@forEach
            if (prefab.has<BlockyInfo>()) handleBlockyDrops(block, null)
            block.setType(Material.AIR, false)
        }
    }
}
