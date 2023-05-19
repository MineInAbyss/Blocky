package com.mineinabyss.blocky.listeners

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.jeff_media.customblockdata.CustomBlockData
import com.jeff_media.morepersistentdatatypes.DataType
import com.mineinabyss.blocky.api.BlockyBlocks.gearyEntity
import com.mineinabyss.blocky.api.BlockyBlocks.isBlockyBlock
import com.mineinabyss.blocky.blockyConfig
import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.blocky.components.core.BlockyBlock
import com.mineinabyss.blocky.components.core.BlockyBlock.BlockType
import com.mineinabyss.blocky.components.core.BlockyInfo
import com.mineinabyss.blocky.components.features.BlockyBurnable
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.blocky.itemProvider
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.entities.rightClicked
import kotlinx.coroutines.delay
import org.bukkit.GameEvent
import org.bukkit.Instrument
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.GenericGameEvent
import org.bukkit.inventory.EquipmentSlot

class BlockyNoteBlockListener : Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun NotePlayEvent.cancelBlockyNotes() {
        if (!block.isVanillaNoteBlock) isCancelled = true
        else if (block.isVanillaNoteBlock && !blockyConfig.noteBlocks.restoreFunctionality) {
            note = block.updateBlockyNote()
            instrument = block.getBlockyInstrument()
        } else return
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

        if (rightClicked) setUseInteractedBlock(Event.Result.DENY)
        if (block.isVanillaNoteBlock) {
            if (rightClicked) block.updateBlockyNote()
            block.playBlockyNoteBlock()
        }
    }

    // Handle playing the sound. If BlockData isn't in map, it means this should be handled by vanilla
    // AKA restoreFunctionality enabled and normal block
    @EventHandler(priority = EventPriority.HIGHEST)
    fun BlockPhysicsEvent.onBlockPhysics() {
        if (!block.isBlockyNoteBlock) return

        isCancelled = true
        if (block.isBlockFacePowered(block.getFace(sourceBlock)!!))
            block.playBlockyNoteBlock()
        if (block.getRelative(BlockFace.UP).type == Material.NOTE_BLOCK)
            block.updateNoteBlockAbove()
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun GenericGameEvent.disableRedstone() {
        val block = location.block
        val data = block.blockData.clone() as? NoteBlock ?: return

        if (event != GameEvent.NOTE_BLOCK_PLAY) return
        isCancelled = true
        blockyPlugin.launch {
            delay(1.ticks)
            block.setBlockData(data, false)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun PlayerInteractEvent.onInteractBlockyNoteBlock() {
        val block = clickedBlock ?: return
        val item = item ?: return
        val type = item.clone().type

        if (action != Action.RIGHT_CLICK_BLOCK || !block.isBlockyBlock || hand != EquipmentSlot.HAND) return
        setUseInteractedBlock(Event.Result.DENY)
        if (type.isBlock) placeBlockyBlock(player, hand!!, item, block, blockFace, type.createBlockData())
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.onPrePlacingBlockyNoteBlock() {
        if (action != Action.RIGHT_CLICK_BLOCK) return
        if (hand != EquipmentSlot.HAND) return

        val gearyItem = itemProvider.deserializeItemStackToEntity(item, player.toGeary()) ?: return
        val blockyBlock = gearyItem.get<BlockyBlock>() ?: return
        val against = clickedBlock ?: return

        if (blockyBlock.blockType != BlockType.NOTEBLOCK) return
        if ((against.type.isInteractable && !against.isBlockyBlock) && !player.isSneaking) return

        placeBlockyBlock(player, hand!!, item!!, against, blockFace, gearyItem.getBlockyNoteBlock(blockFace, player))
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

    // Set default note of normal noteblock only if not restoreFunctionality is enabled
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockPlaceEvent.onPlaceNoteBlock() {
        if (!blockPlaced.isVanillaNoteBlock) return

        if (!blockyConfig.noteBlocks.restoreFunctionality)
            blockPlaced.customBlockData.set(NOTE_KEY, DataType.INTEGER, 0)
        else blockPlaced.customBlockData.set(VANILLA_NOTEBLOCK_KEY, DataType.BOOLEAN, true)
    }

    // Convert vanilla blocks into custom note blocks if any after changing the value
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun ChunkLoadEvent.migrateOnChunkLoad() {
        CustomBlockData.getBlocksWithCustomData(blockyPlugin, chunk)
            .filter { it.customBlockData.has(VANILLA_NOTEBLOCK_KEY, DataType.BOOLEAN) }.forEach { block ->

                if (block.blockData !is NoteBlock) {
                    block.customBlockData.remove(VANILLA_NOTEBLOCK_KEY)
                    return@forEach
                }

                // Convert any VANILLA_NOTEBLOCK_KEY blocks to custom if restoreFunctionality is disabled
                if (!blockyConfig.noteBlocks.restoreFunctionality) {
                    // If block doesn't have VANILLA_NOTEBLOCK_KEY or NOTE_KEY,
                    // assume it to be a vanilla and convert it to custom
                    if (block.customBlockData.isEmpty) {
                        block.customBlockData.set(NOTE_KEY, DataType.INTEGER, 0)
                        block.blockData = Material.NOTE_BLOCK.createBlockData()
                    }
                    // If block has NOTE_KEY, aka it was a custom vanilla block, convert to full vanilla
                    else if (block.customBlockData.has(VANILLA_NOTEBLOCK_KEY, DataType.BOOLEAN)) {
                        block.customBlockData.set(
                            NOTE_KEY,
                            DataType.INTEGER,
                            (block.blockData as NoteBlock).note.id.toInt()
                        )
                        block.blockData = Material.NOTE_BLOCK.createBlockData()
                        block.customBlockData.remove(VANILLA_NOTEBLOCK_KEY)
                    }
                } else {
                    if (block.customBlockData.has(NOTE_KEY, DataType.INTEGER)) {
                        (block.blockData as NoteBlock).instrument = Instrument.PIANO
                        (block.blockData as NoteBlock).note = block.getBlockyNote() // Set note from PDC data

                        block.customBlockData.remove(NOTE_KEY)
                        block.customBlockData.set(VANILLA_NOTEBLOCK_KEY, DataType.BOOLEAN, true)
                    }
                }
            }
    }
}
