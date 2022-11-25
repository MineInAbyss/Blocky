package com.mineinabyss.blocky.listeners

import com.destroystokyo.paper.MaterialTags
import com.mineinabyss.blocky.api.events.block.BlockyBlockPlaceEvent
import com.mineinabyss.blocky.components.core.BlockyBlock
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.idofront.events.call
import com.mineinabyss.looty.tracking.toGearyOrNull
import io.th0rgal.protectionlib.ProtectionLib
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Slab.Type
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class BlockyCopperListener {
    class BlockySlabListener : Listener {

        // If the GearyItem isn't using a blockyslab as its main material, replicate functionality
        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        fun PlayerInteractEvent.onPlacingBlockySlab() {
            if (action != Action.RIGHT_CLICK_BLOCK) return
            if (hand != EquipmentSlot.HAND) return
            if (item?.type in BLOCKY_SLABS) return

            val gearyItem = item?.toGearyOrNull(player) ?: return
            val blockyBlock = gearyItem.get<BlockyBlock>() ?: return
            val against = clickedBlock ?: return

            if (blockyBlock.blockType != BlockyBlock.BlockType.SLAB) return
            if ((against.type.isInteractable && !against.isBlockyBlock) && !player.isSneaking) return

            val blockyData = Bukkit.createBlockData(BLOCKY_SLABS.elementAt(blockyBlock.blockId)) as Slab
            val relative = against.getRelative(blockFace)
            val oldData: BlockData

            when {
                // When the relative block is same slab, make it a double slab
                against.type == blockyData.material && (against.blockData as Slab).type == Type.BOTTOM -> {
                    blockyData.type = Type.DOUBLE
                    oldData = against.blockData
                    against.blockData = blockyData
                }

                against.isReplaceable -> {
                    blockyData.type = blockFace.getSlabHalf(player)
                    oldData = against.blockData
                    against.blockData = blockyData
                }

                relative.type.isAir -> {
                    blockyData.type =
                        if (!blockFace.isVertical) blockFace.getSlabHalf(player) else blockFace.getSlabHalf
                    oldData = relative.blockData
                    relative.blockData = blockyData
                }

                relative.type == blockyData.material -> {
                    blockyData.type = Type.DOUBLE
                    oldData = relative.blockData
                    relative.blockData = blockyData
                }

                else -> return
            }

            val loc = if (relative.blockData == blockyData) relative.location else against.location

            val blockyEvent = BlockyBlockPlaceEvent(loc.block, player).run { call(); this }
            if (!ProtectionLib.canBuild(player, loc)) blockyEvent.isCancelled = true
            if (blockyEvent.isCancelled) {
                loc.block.blockData = oldData
                return
            }

            player.swingMainHand()
        }

        private val BlockFace.isVertical: Boolean
            get() = this == BlockFace.UP || this == BlockFace.DOWN

        private val BlockFace.getSlabHalf
            get() =
                when (this) {
                    BlockFace.UP -> Type.BOTTOM
                    BlockFace.DOWN -> Type.TOP
                    else -> Type.BOTTOM
                }

        private fun BlockFace.getSlabHalf(player: Player): Type {
            val trace = player.rayTraceBlocks(5.0) ?: return Type.BOTTOM
            val block = trace.hitBlock ?: return Type.BOTTOM
            return when (this) {
                BlockFace.UP -> Type.BOTTOM
                BlockFace.DOWN -> Type.TOP
                else -> if (trace.hitPosition.y < block.y + 0.5) Type.BOTTOM else Type.TOP
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        fun PlayerInteractEvent.onWaxCopperSlab() {
            val block = clickedBlock ?: return
            if (hand != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK) return
            if (block.type in BLOCKY_SLABS || item?.type != Material.HONEYCOMB) return

            if (!block.isFakeWaxedCopper)
                block.isFakeWaxedCopper = true
        }


        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        fun PlayerInteractEvent.onUnwaxCopperSlab() {
            val block = clickedBlock ?: return
            if (hand != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK) return
            if (block.type !in COPPER_SLABS || item?.let { MaterialTags.AXES.isTagged(it) } != true) return

            if (block.isFakeWaxedCopper)
                block.isFakeWaxedCopper = false
        }

        @EventHandler(priority = EventPriority.LOWEST)
        fun PlayerInteractEvent.onUnwaxBlockySlab() {
            if (clickedBlock?.type in BLOCKY_SLABS && item?.let { MaterialTags.AXES.isTagged(it) } == true)
                isCancelled = true
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        fun BlockFormEvent.onOxidizedCopper() {
            if (newState.type in BLOCKY_SLABS || block.isFakeWaxedCopper)
                isCancelled = true
        }

    }


    class BlockyStairListener : Listener {

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        fun PlayerInteractEvent.onWaxCopperSlab() {
            val block = clickedBlock ?: return
            if (hand != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK) return
            if (block.type in BLOCKY_STAIRS || item?.type != Material.HONEYCOMB) return

            if (!block.isFakeWaxedCopper)
                block.isFakeWaxedCopper = true
        }


        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        fun PlayerInteractEvent.onUnwaxCopperSlab() {
            val block = clickedBlock ?: return
            if (hand != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK) return
            if (block.type !in COPPER_STAIRS || item?.let { MaterialTags.AXES.isTagged(it) } != true) return

            if (block.isFakeWaxedCopper)
                block.isFakeWaxedCopper = false
        }

        @EventHandler(priority = EventPriority.LOWEST)
        fun PlayerInteractEvent.onUnwaxBlockySlab() {
            if (clickedBlock?.type in BLOCKY_STAIRS && item?.let { MaterialTags.AXES.isTagged(it) } == true)
                isCancelled = true
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        fun BlockFormEvent.onOxidizedCopper() {
            if (newState.type in BLOCKY_STAIRS || block.isFakeWaxedCopper)
                isCancelled = true
        }

    }
}
