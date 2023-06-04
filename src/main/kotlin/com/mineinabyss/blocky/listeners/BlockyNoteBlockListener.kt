package com.mineinabyss.blocky.listeners

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.jeff_media.customblockdata.CustomBlockData
import com.mineinabyss.blocky.api.BlockyBlocks.gearyEntity
import com.mineinabyss.blocky.api.BlockyBlocks.isBlockyBlock
import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.BlockyBlock
import com.mineinabyss.blocky.components.core.BlockyBlock.BlockType
import com.mineinabyss.blocky.components.core.VanillaNoteBlock
import com.mineinabyss.blocky.components.features.blocks.BlockyBurnable
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.blocky.helpers.GenericHelpers.isInteractable
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.datastore.has
import com.mineinabyss.geary.papermc.datastore.remove
import com.mineinabyss.idofront.entities.rightClicked
import kotlinx.coroutines.delay
import org.bukkit.GameEvent
import org.bukkit.Instrument
import org.bukkit.Material
import org.bukkit.Tag
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
        else if (block.isVanillaNoteBlock && !blocky.config.noteBlocks.restoreFunctionality) {
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
        if (blocky.config.noteBlocks.restoreFunctionality && block.isVanillaNoteBlock) return

        if (rightClicked) setUseInteractedBlock(Event.Result.DENY)
        if (block.isVanillaNoteBlock) {
            if (rightClicked) block.updateBlockyNote()
            block.playBlockyNoteBlock()
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun BlockPhysicsEvent.onBlockPhysics() {
        if (block.getRelative(BlockFace.UP).type == Material.NOTE_BLOCK) {
            isCancelled = true
            block.updateNoteBlockAbove()
        }

        if (block.type == Material.NOTE_BLOCK) {
            isCancelled = true
            block.state.update(true, false)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun GenericGameEvent.disableRedstone() {
        val block = location.block
        val data = block.blockData.clone() as? NoteBlock ?: return

        if (event != GameEvent.NOTE_BLOCK_PLAY) return
        isCancelled = true
        blocky.plugin.launch {
            delay(1.ticks)
            block.setBlockData(data, false)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun PlayerInteractEvent.onPlaceAgainstBlockyBlock() {
        val (block, item, hand) = (clickedBlock ?: return) to (item ?: return) to (hand ?: return)
        //TODO Figure out  why water replaces custom block
        if (action != Action.RIGHT_CLICK_BLOCK || !block.isBlockyBlock) return

        setUseInteractedBlock(Event.Result.DENY)
        //TODO Might need old check if it is Blocky block?
        if (item.type.isBlock)
            if (Tag.STAIRS.isTagged(item.type) || Tag.SLABS.isTagged(item.type))
                placeBlockyBlock(player, hand, item, block, blockFace, item.type.createBlockData())
            else BlockStateCorrection.placeItemAsBlock(player, hand, item, block)
            //placeBlockyBlock(player, hand, item, block, blockFace, type.createBlockData())
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.onPrePlacingBlockyNoteBlock() {
        val (block, item, hand) = (clickedBlock ?: return) to (item ?: return) to (hand ?: return)
        if (action != Action.RIGHT_CLICK_BLOCK) return
        if (hand != EquipmentSlot.HAND) return

        val gearyItem = player.gearyInventory?.get(hand) ?: return
        val blockyBlock = gearyItem.get<BlockyBlock>() ?: return

        if (blockyBlock.blockType != BlockType.NOTEBLOCK) return
        if (!player.isSneaking && block.isInteractable()) return

        placeBlockyBlock(player, hand, item, block, blockFace, gearyItem.getBlockyNoteBlock(blockFace, player))
    }

    @EventHandler(ignoreCancelled = true)
    fun EntityExplodeEvent.onExplodingBlocky() {
        blockList().forEach { block ->
            if (!block.isBlockyNoteBlock) return@forEach
            handleBlockyDrops(block, null)
            block.setType(Material.AIR, false)
        }
    }

    // Set default note of normal noteblock only if not restoreFunctionality is enabled
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockPlaceEvent.onPlaceNoteBlock() {
        if (!blockPlaced.isVanillaNoteBlock) return

        if (!blocky.config.noteBlocks.restoreFunctionality)
            blockPlaced.persistentDataContainer.encode(VanillaNoteBlock())
        else blockPlaced.persistentDataContainer.encode(VanillaNoteBlock())
    }

    //TODO Make sure this works
    // Convert vanilla blocks into custom note blocks if any after changing the value
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun ChunkLoadEvent.migrateOnChunkLoad() {
        CustomBlockData.getBlocksWithCustomData(blocky.plugin, chunk)
            .filter { it.persistentDataContainer.has<VanillaNoteBlock>() }.forEach { block ->

                if (block.blockData !is NoteBlock) {
                    block.persistentDataContainer.remove<VanillaNoteBlock>()
                    return@forEach
                }

                // Convert any VANILLA_NOTEBLOCK_KEY blocks to custom if restoreFunctionality is disabled
                if (!blocky.config.noteBlocks.restoreFunctionality) {
                    // If block doesn't have VANILLA_NOTEBLOCK_KEY or NOTE_KEY,
                    // assume it to be a vanilla and convert it to custom
                    if (block.customBlockData.isEmpty) {
                        block.persistentDataContainer.encode(VanillaNoteBlock(0))
                        block.blockData = Material.NOTE_BLOCK.createBlockData()
                    }
                    // If block has NOTE_KEY, aka it was a custom vanilla block, convert to full vanilla
                    else if (block.persistentDataContainer.has<VanillaNoteBlock>()) {
                        block.persistentDataContainer.encode(VanillaNoteBlock((block.blockData as NoteBlock).note.id.toInt()))
                        block.blockData = Material.NOTE_BLOCK.createBlockData()
                    }
                } else {
                    if (block.persistentDataContainer.has<VanillaNoteBlock>()) {
                        val noteblock = block.blockData as? NoteBlock ?: return@forEach
                        noteblock.instrument = Instrument.PIANO
                        noteblock.note = block.getBlockyNote() // Set note from PDC data

                        block.persistentDataContainer.encode(VanillaNoteBlock(noteblock.note.id.toInt()))
                    }
                }
            }
    }
}
