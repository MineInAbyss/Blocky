package com.mineinabyss.blocky.listeners

import com.destroystokyo.paper.MaterialTags
import com.mineinabyss.blocky.api.events.block.BlockyBlockPlaceEvent
import com.mineinabyss.blocky.components.core.VanillaCopperBlock
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.blocky.helpers.GenericHelpers.isInteractable
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.idofront.events.call
import io.th0rgal.protectionlib.ProtectionLib
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Slab.Type
import org.bukkit.block.data.type.Stairs
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
            val (block, item, hand) = (clickedBlock ?: return) to (item ?: return) to (hand ?: return)
            if (action != Action.RIGHT_CLICK_BLOCK) return
            if (hand != EquipmentSlot.HAND) return
            if (item.type in CopperHelpers.BLOCKY_SLABS) return

            val blockyBlock = player.gearyInventory?.get(hand)?.get<SetBlock>() ?: return

            if (blockyBlock.blockType != SetBlock.BlockType.SLAB) return
            if (!player.isSneaking && block.isInteractable()) return

            val blockyData = CopperHelpers.BLOCKY_SLABS.elementAt(blockyBlock.blockId).createBlockData() as Slab
            val relative = block.getRelative(blockFace)
            val oldData: BlockData

            when {
                // When the relative block is same slab, make it a double slab
                block.type == blockyData.material && (block.blockData as Slab).type == Type.BOTTOM -> {
                    blockyData.type = Type.DOUBLE
                    oldData = block.blockData
                    block.blockData = blockyData
                }

                block.isReplaceable -> {
                    blockyData.type = blockFace.getSlabHalf(player)
                    oldData = block.blockData
                    block.blockData = blockyData
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

            val loc = if (relative.blockData == blockyData) relative.location else block.location

            val blockyEvent = BlockyBlockPlaceEvent(loc.block, player, hand, item)
            if (!ProtectionLib.canBuild(player, loc)) blockyEvent.isCancelled = true
            blockyEvent.call()
            if (blockyEvent.isCancelled) {
                loc.block.blockData = oldData
                return
            }

            // Set PDC Key so that the converter knows it should skip this blocky block
            loc.block.persistentDataContainer.encode(VanillaCopperBlock)
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

        @EventHandler(priority = EventPriority.LOWEST)
        fun PlayerInteractEvent.onWaxCopperSlab() {
            val block = clickedBlock ?: return
            if (hand != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK) return
            if (block.type !in CopperHelpers.COPPER_SLABS || item?.type != Material.HONEYCOMB) return

            isCancelled = true
            if (!CopperHelpers.isFakeWaxedCopper(block))
                CopperHelpers.setFakeWaxedCopper(block, true)
        }


        @EventHandler(priority = EventPriority.LOWEST)
        fun PlayerInteractEvent.onUnwaxCopperSlab() {
            val block = clickedBlock ?: return
            if (hand != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK) return
            if (block.type !in CopperHelpers.COPPER_SLABS || item?.let { MaterialTags.AXES.isTagged(it) } != true) return

            if (CopperHelpers.isFakeWaxedCopper(block))
                CopperHelpers.setFakeWaxedCopper(block, false)
        }

        @EventHandler(priority = EventPriority.LOWEST)
        fun PlayerInteractEvent.onUnwaxBlockySlab() {
            if (clickedBlock?.type in CopperHelpers.BLOCKY_SLABS && item?.let { MaterialTags.AXES.isTagged(it) } == true)
                isCancelled = true
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        fun BlockFormEvent.onOxidizedCopperSlab() {
            if (newState.type in CopperHelpers.BLOCKY_SLABS || CopperHelpers.isFakeWaxedCopper(block))
                isCancelled = true
        }

    }


    class BlockyStairListener : Listener {

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        fun PlayerInteractEvent.onPlacingBlockyStair() {
            val (block, item, hand) = (clickedBlock ?: return) to (item ?: return) to (hand ?: return)
            if (action != Action.RIGHT_CLICK_BLOCK) return
            if (hand != EquipmentSlot.HAND) return
            if (item.type in CopperHelpers.BLOCKY_STAIRS) return

            val blockyBlock = player.gearyInventory?.get(hand)?.get<SetBlock>() ?: return
            if (blockyBlock.blockType != SetBlock.BlockType.STAIR) return
            if (!player.isSneaking && block.isInteractable()) return

            val blockyData = CopperHelpers.BLOCKY_STAIRS.elementAt(blockyBlock.blockId).createBlockData() as Stairs
            val relative = block.getRelative(blockFace)
            val oldData: BlockData

            when {
                block.isReplaceable -> {
                    blockyData.half = blockFace.getStairHalf(player)
                    blockyData.facing = player.facing
                    blockyData.shape = block.getStairShape(player)
                    oldData = block.blockData
                    block.blockData = blockyData
                }

                relative.type.isAir -> {
                    blockyData.half = blockFace.getStairHalf(player)
                    blockyData.facing = player.facing
                    blockyData.shape = relative.getStairShape(player)
                    oldData = relative.blockData
                    relative.blockData = blockyData
                }

                relative.type == blockyData.material -> {
                    blockyData.half = Bisected.Half.TOP
                    blockyData.facing = player.facing
                    blockyData.shape = relative.getStairShape(player)
                    oldData = relative.blockData
                    relative.blockData = blockyData
                }

                else -> return
            }

            val loc = if (relative.blockData == blockyData) relative.location else block.location
            val blockyEvent = BlockyBlockPlaceEvent(loc.block, player, hand, item)
            if (!ProtectionLib.canBuild(player, loc)) blockyEvent.isCancelled = true
            if (!blockyEvent.callEvent()) {
                loc.block.blockData = oldData
                return
            }

            // Set PDC Key so that the converter knows it should skip this blocky block
            loc.block.persistentDataContainer.encode(VanillaCopperBlock)
            player.swingMainHand()
        }

        @EventHandler(priority = EventPriority.LOWEST)
        fun PlayerInteractEvent.onWaxCopperStair() {
            val block = clickedBlock ?: return
            if (hand != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK) return
            if (block.type !in CopperHelpers.COPPER_STAIRS || item?.type != Material.HONEYCOMB) return

            isCancelled = true
            if (!CopperHelpers.isFakeWaxedCopper(block))
                CopperHelpers.setFakeWaxedCopper(block, true)
        }


        @EventHandler(priority = EventPriority.LOWEST)
        fun PlayerInteractEvent.onUnwaxCopperStair() {
            val block = clickedBlock ?: return
            if (hand != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK) return
            if (block.type !in CopperHelpers.COPPER_STAIRS || item?.let { MaterialTags.AXES.isTagged(it) } != true) return

            if (CopperHelpers.isFakeWaxedCopper(block))
                CopperHelpers.setFakeWaxedCopper(block, false)
        }

        @EventHandler(priority = EventPriority.LOWEST)
        fun PlayerInteractEvent.onUnwaxBlockyStair() {
            if (clickedBlock?.type in CopperHelpers.BLOCKY_STAIRS && item?.let { MaterialTags.AXES.isTagged(it) } == true)
                isCancelled = true
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        fun BlockFormEvent.onOxidizedCopperStair() {
            if (newState.type in CopperHelpers.BLOCKY_STAIRS || CopperHelpers.isFakeWaxedCopper(block))
                isCancelled = true
        }

        private fun BlockFace.getStairHalf(player: Player): Bisected.Half {
            val trace = player.rayTraceBlocks(5.0) ?: return Bisected.Half.BOTTOM
            val block = trace.hitBlock ?: return Bisected.Half.BOTTOM
            return when (this) {
                BlockFace.UP -> Bisected.Half.BOTTOM
                BlockFace.DOWN -> Bisected.Half.TOP
                else -> if (trace.hitPosition.y < block.y + 0.5) Bisected.Half.BOTTOM else Bisected.Half.TOP
            }
        }

        private fun Block.getStairShape(player: Player): Stairs.Shape {
            val leftBlock = GenericHelpers.leftBlock(this, player)
            val rightBlock = GenericHelpers.rightBlock(this, player)
            val aheadBlock = getRelative(player.facing)
            val behindBlock = getRelative(player.facing.oppositeFace)

            return when {
                leftBlock.isStair && rightBlock.isStair -> Stairs.Shape.STRAIGHT
                leftBlock.isStair && aheadBlock.isStair -> Stairs.Shape.INNER_LEFT
                rightBlock.isStair && aheadBlock.isStair -> Stairs.Shape.INNER_RIGHT
                leftBlock.isStair && behindBlock.isStair -> Stairs.Shape.OUTER_LEFT
                rightBlock.isStair && behindBlock.isStair -> Stairs.Shape.OUTER_RIGHT
                else -> Stairs.Shape.STRAIGHT
            }
        }

        private val Block.isStair get() = blockData is Stairs

    }
}
