package com.mineinabyss.blocky.listeners

import com.destroystokyo.paper.MaterialTags
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
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
import com.mineinabyss.geary.papermc.datastore.remove
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.toGearyOrNull
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
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockBurnEvent.onBurnBlockyNoteBlock() {
        if (!block.isBlockyNoteBlock) return
        if (block.toGearyOrNull()?.has<BlockyBurnable>() != true) isCancelled = true
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun PlayerInteractEvent.onChangingNote() {
        val block = clickedBlock ?: return
        if (block.type != Material.NOTE_BLOCK) return

        if (rightClicked) setUseInteractedBlock(Event.Result.DENY)
        if (block.isVanillaNoteBlock) {
            if (rightClicked) block.updateBlockyNote()
            block.playBlockyNoteBlock()
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun PlayerInteractEvent.onPlaceAgainstBlockyBlock() {
        val (placedAgainst, item, hand) = (clickedBlock ?: return) to (item ?: return) to (hand ?: return)
        //TODO Figure out  why water replaces custom block
        if (action != Action.RIGHT_CLICK_BLOCK || !placedAgainst.isBlockyBlock || !item.type.isBlock) return

        if (!BlockyBlockInteractEvent(placedAgainst, player, hand, item, blockFace).callEvent()) isCancelled = true
        setUseInteractedBlock(Event.Result.DENY)

        val type = when {
            item.type == Material.MILK_BUCKET -> return
            item.type == Material.LAVA_BUCKET -> Material.LAVA
            MaterialTags.BUCKETS.isTagged(item.type) -> Material.WATER
            else -> null
        }

        val newData = when {
            type != null && type.isBlock -> type.createBlockData()
            else -> null
        }
        BlockStateCorrection.placeItemAsBlock(player, hand, item, placedAgainst, blockFace)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.onPrePlacingBlockyNoteBlock() {
        val (block, item, hand) = (clickedBlock ?: return) to (item ?: return) to (hand ?: return)
        if (action != Action.RIGHT_CLICK_BLOCK || hand != EquipmentSlot.HAND) return
        val gearyItem = player.gearyInventory?.get(hand) ?: return
        if (gearyItem.get<SetBlock>()?.blockType != SetBlock.BlockType.NOTEBLOCK) return
        if (!player.isSneaking && block.isInteractable()) return

        setUseInteractedBlock(Event.Result.DENY)

        placeBlockyBlock(player, hand, item, block, blockFace, gearyItem.getBlockyNoteBlock(blockFace, player))
    }

    @EventHandler(ignoreCancelled = true)
    fun EntityExplodeEvent.onExplodingBlocky() {
        blockList().filter { it.isBlockyNoteBlock }.forEach { block ->
            block.setType(Material.AIR, false)
        }
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
