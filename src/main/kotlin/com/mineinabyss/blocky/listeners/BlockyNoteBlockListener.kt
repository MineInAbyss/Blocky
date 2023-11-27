package com.mineinabyss.blocky.listeners

import com.jeff_media.customblockdata.CustomBlockData
import com.mineinabyss.blocky.api.BlockyBlocks.isBlockyBlock
import com.mineinabyss.blocky.api.events.block.BlockyBlockInteractEvent
import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.VanillaNoteBlock
import com.mineinabyss.blocky.components.features.blocks.BlockyBurnable
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.blocky.helpers.GenericHelpers.isInteractable
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.datastore.has
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.toGearyOrNull
import com.mineinabyss.idofront.entities.rightClicked
import org.bukkit.Material
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.NotePlayEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.inventory.EquipmentSlot

class BlockyNoteBlockListener : Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun NotePlayEvent.cancelBlockyNotes() {
        if (!block.isVanillaNoteBlock) isCancelled = true
        note = block.blockyNote()
        instrument = block.blockyInstrument()
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockBurnEvent.onBurnBlockyNoteBlock() {
        if (block.isBlockyNoteBlock && block.toGearyOrNull()?.has<BlockyBurnable>() != true)
            isCancelled = true
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun PlayerInteractEvent.onChangingNote() {
        val block = clickedBlock?.takeIf { it.type == Material.NOTE_BLOCK } ?: return
        if (hand != EquipmentSlot.HAND || !rightClicked) return

        if (block.isBlockyBlock) setUseInteractedBlock(Event.Result.DENY)
        else if (block.isVanillaNoteBlock && rightClicked) block.updateBlockyNote()
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun PlayerInteractEvent.onPlaceAgainstBlockyBlock() {
        val (placedAgainst, item, hand) = (clickedBlock?.takeIf { it.isBlockyBlock } ?: return) to (item?.takeIf { it.type.isBlock } ?: return) to (hand ?: return)
        if (action != Action.RIGHT_CLICK_BLOCK) return

        if (!BlockyBlockInteractEvent(placedAgainst, player, hand, item, blockFace).callEvent()) isCancelled = true
        setUseInteractedBlock(Event.Result.DENY)

        BlockStateCorrection.placeItemAsBlock(player, hand, item)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.onPrePlacingBlockyNoteBlock() {
        val (block, item, hand) = (clickedBlock ?: return) to (item ?: return) to (hand?.takeIf { it == EquipmentSlot.HAND } ?: return)
        val gearyItem = player.gearyInventory?.get(hand)?.takeIf { it.get<SetBlock>()?.blockType == SetBlock.BlockType.NOTEBLOCK } ?: return
        if (action != Action.RIGHT_CLICK_BLOCK || (!player.isSneaking && block.isInteractable())) return

        setUseInteractedBlock(Event.Result.DENY)

        placeBlockyBlock(player, hand, item, block, blockFace, gearyItem.getBlockyNoteBlock(blockFace, player))
    }

    @EventHandler(ignoreCancelled = true)
    fun EntityExplodeEvent.onExplodingBlocky() {
        blockList().filter { it.isBlockyNoteBlock }.forEach { it.type = Material.AIR }
        blockList().removeIf { it.isBlockyNoteBlock }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockPlaceEvent.onPlaceNoteBlock() {
        if (blockPlaced.isVanillaNoteBlock) blockPlaced.persistentDataContainer.encode(VanillaNoteBlock())
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun ChunkLoadEvent.migrateOnChunkLoad() {
        CustomBlockData.getBlocksWithCustomData(blocky.plugin, chunk)
            .filter { it.blockData is NoteBlock && it.persistentDataContainer.has<VanillaNoteBlock>() }.forEach { block ->
                // If block doesn't have VANILLA_NOTEBLOCK_KEY or NOTE_KEY,
                // assume it to be a vanilla and convert it to custom
                if (block.customBlockData.isEmpty) {
                    block.persistentDataContainer.encode(VanillaNoteBlock(0))
                    block.blockData = Material.NOTE_BLOCK.createBlockData()
                } else {
                    // If block has NOTE_KEY, aka it was a custom vanilla block, convert to full vanilla
                    block.persistentDataContainer.encode(VanillaNoteBlock((block.blockData as NoteBlock).note.id.toInt()))
                    block.blockData = Material.NOTE_BLOCK.createBlockData()
                }
            }
    }
}
