package com.mineinabyss.blocky.listeners

import com.destroystokyo.paper.MaterialTags
import com.mineinabyss.blocky.components.core.BlockyBlock
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.Bukkit
import org.bukkit.Effect
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class BlockyCopperListener {

    class BlockySlabListener : Listener {

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        fun PlayerInteractEvent.onPlacingBlockySlab() {
            if (action != Action.RIGHT_CLICK_BLOCK) return
            if (hand != EquipmentSlot.HAND) return
            broadcast("placing blocky slab")
            val gearyItem = item?.toGearyOrNull(player) ?: return
            val blockyBlock = gearyItem.get<BlockyBlock>() ?: return
            val against = clickedBlock ?: return
            broadcast("placing blocky slab")
            if (blockyBlock.blockType != BlockyBlock.BlockType.SLAB) return
            if ((against.type.isInteractable && !against.isBlockyBlock) && !player.isSneaking) return

            placeBlockyBlock(player, hand!!, item!!, against, blockFace, Bukkit.createBlockData(Material.CUT_COPPER_SLAB))
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        fun PlayerInteractEvent.onWaxCopperSlab() {
            val block = clickedBlock ?: return
            if (hand != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK) return
            if (block.type in BLOCKY_SLABS || item?.type != Material.HONEYCOMB) return

            isCancelled = true
            if (!block.isFakeWaxedCopper)
                block.isFakeWaxedCopper = true
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        fun PlayerInteractEvent.onUnwaxCopperSlab() {
            val block = clickedBlock ?: return
            if (hand != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK) return
            if (block.type !in COPPER_SLABS || item?.let { MaterialTags.AXES.isTagged(it) } != true) return

            isCancelled = true
            if (block.isFakeWaxedCopper) {
                block.isFakeWaxedCopper = false
                block.world.playEffect(block.location, Effect.COPPER_WAX_OFF, 0)
                player.swingMainHand()
            }
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        fun PlayerInteractEvent.onUnwaxBlockySlab() {
            if (clickedBlock?.type !in BLOCKY_SLABS || item?.let { MaterialTags.AXES.isTagged(it) } != true) return
            isCancelled = true
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        fun BlockFormEvent.onOxidizedCopper() {
            if (newState.type in BLOCKY_SLABS || block.isFakeWaxedCopper)
                isCancelled = true
        }

    }

    class BlockyStairListener : Listener {

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        fun PlayerInteractEvent.onWaxCopperStair() {
            val block = clickedBlock ?: return
            if (hand != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK) return
            if (block.type in BLOCKY_STAIRS || item?.type != Material.HONEYCOMB) return

            isCancelled = true
            if (!block.isFakeWaxedCopper)
                block.isFakeWaxedCopper = true
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        fun PlayerInteractEvent.onUnwaxCopperStair() {
            val block = clickedBlock ?: return
            if (hand != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK) return
            if (block.type !in COPPER_STAIRS || item?.let { MaterialTags.AXES.isTagged(it) } != true) return

            isCancelled = true
            if (block.isFakeWaxedCopper) {
                block.isFakeWaxedCopper = false
                block.world.playEffect(block.location, Effect.COPPER_WAX_OFF, 0)
                player.swingMainHand()
            }
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        fun PlayerInteractEvent.onUnwaxBlockyStair() {
            if (clickedBlock?.type !in BLOCKY_STAIRS || item?.let { MaterialTags.AXES.isTagged(it) } != true) return
            isCancelled = true
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        fun BlockFormEvent.onOxidizedCopper() {
            if (newState.type in BLOCKY_STAIRS || block.isFakeWaxedCopper)
                isCancelled = true
        }

    }
}
