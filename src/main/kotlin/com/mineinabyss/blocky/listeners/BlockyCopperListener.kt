package com.mineinabyss.blocky.listeners

import com.destroystokyo.paper.MaterialTags
import com.mineinabyss.blocky.api.events.block.BlockyBlockPlaceEvent
import com.mineinabyss.blocky.components.core.VanillaCopperBlock
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.blocky.helpers.GenericHelpers.isInteractable
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import io.th0rgal.protectionlib.ProtectionLib
import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

class BlockyCopperListener {
    class BlockySlabListener : Listener {

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        fun PlayerInteractEvent.onPlacingBlockyStair() {
            val (item, hand) = (item ?: return) to (hand ?: return)
            val block = (clickedBlock!!.takeIf { it.isReplaceable } ?: clickedBlock!!.getRelative(blockFace))
            if (action != Action.RIGHT_CLICK_BLOCK || hand != EquipmentSlot.HAND || item.type in CopperHelpers.BLOCKY_SLABS) return

            val blockyBlock = player.gearyInventory?.get(hand)?.get<SetBlock>() ?: return
            if (blockyBlock.blockType != SetBlock.BlockType.SLAB) return
            if (!player.isSneaking && block.isInteractable()) return

            val blockyEvent = BlockyBlockPlaceEvent(block, player, hand, item)
            if (!ProtectionLib.canBuild(player, block.location)) blockyEvent.isCancelled = true
            if (!blockyEvent.callEvent()) return setUseItemInHand(Event.Result.DENY)

            // If item being placed is a blocky copper block, we want this logic to run with an item of waxed material
            // If it is not, we want to ensure the material is not waxed copper, and if it is, change it
            val placedItem = item.takeIf(CopperHelpers::isBlockyCopper)?.let(CopperHelpers::convertToBlockyType) ?: CopperHelpers.convertToFakeType(item)
            BlockStateCorrection.placeItemAsBlock(player, hand, placedItem)

            // Set PDC Key so that the converter knows it should skip this blocky block
            block.persistentDataContainer.encode(VanillaCopperBlock())
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

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        fun BlockBreakEvent.onBreakFakeCopperStair() {
            if (!CopperHelpers.isFakeWaxedCopper(block)) return
            val index = CopperHelpers.COPPER_STAIRS.indexOf(block.type)
            val waxedType = CopperHelpers.BLOCKY_STAIRS.elementAt(index)
            isDropItems = false
            block.customBlockData.clear()
            block.world.dropItemNaturally(block.location, ItemStack(waxedType))
        }

    }


    class BlockyStairListener : Listener {

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        fun PlayerInteractEvent.onPlacingBlockyStair() {
            val (item, hand) = (item ?: return) to (hand ?: return)
            val block = (clickedBlock!!.takeIf { it.isReplaceable } ?: clickedBlock!!.getRelative(blockFace))
            if (action != Action.RIGHT_CLICK_BLOCK || hand != EquipmentSlot.HAND || item.type in CopperHelpers.BLOCKY_STAIRS) return

            val blockyBlock = player.gearyInventory?.get(hand)?.get<SetBlock>() ?: return
            if (blockyBlock.blockType != SetBlock.BlockType.STAIR) return
            if (!player.isSneaking && block.isInteractable()) return

            val blockyEvent = BlockyBlockPlaceEvent(block, player, hand, item)
            if (!ProtectionLib.canBuild(player, block.location)) blockyEvent.isCancelled = true
            if (!blockyEvent.callEvent()) return setUseItemInHand(Event.Result.DENY)

            // If item being placed is a blocky copper block, we want this logic to run with an item of waxed material
            // If it is not, we want to ensure the material is not waxed copper, and if it is, change it
            val placedItem = item.takeIf(CopperHelpers::isBlockyCopper)?.let(CopperHelpers::convertToBlockyType) ?: CopperHelpers.convertToFakeType(item)
            BlockStateCorrection.placeItemAsBlock(player, hand, placedItem)

            // Set PDC Key so that the converter knows it should skip this blocky block
            block.persistentDataContainer.encode(VanillaCopperBlock())
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

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        fun BlockBreakEvent.onBreakFakeCopperStair() {
            if (!CopperHelpers.isFakeWaxedCopper(block)) return
            val index = CopperHelpers.COPPER_STAIRS.indexOf(block.type)
            val waxedType = CopperHelpers.BLOCKY_STAIRS.elementAt(index)
            isDropItems = false
            block.customBlockData.clear()
            block.world.dropItemNaturally(block.location, ItemStack(waxedType))
        }

    }

    class BlockyDoorListener : Listener {

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        fun PlayerInteractEvent.onPlacingBlockyDoor() {
            val (item, hand) = (item ?: return) to (hand ?: return)
            val block = (clickedBlock!!.takeIf { it.isReplaceable } ?: clickedBlock!!.getRelative(blockFace))
            if (action != Action.RIGHT_CLICK_BLOCK || hand != EquipmentSlot.HAND || item.type in CopperHelpers.BLOCKY_DOORS) return

            val blockyBlock = player.gearyInventory?.get(hand)?.get<SetBlock>() ?: return
            if (blockyBlock.blockType != SetBlock.BlockType.DOOR) return
            if (!player.isSneaking && block.isInteractable()) return

            val blockyEvent = BlockyBlockPlaceEvent(block, player, hand, item)
            if (!ProtectionLib.canBuild(player, block.location)) blockyEvent.isCancelled = true
            if (!blockyEvent.callEvent()) return setUseItemInHand(Event.Result.DENY)

            // If item being placed is a blocky copper block, we want this logic to run with an item of waxed material
            // If it is not, we want to ensure the material is not waxed copper, and if it is, change it
            val placedItem = item.takeIf(CopperHelpers::isBlockyCopper)?.let(CopperHelpers::convertToBlockyType) ?: CopperHelpers.convertToFakeType(item)
            BlockStateCorrection.placeItemAsBlock(player, hand, placedItem)

            // Set PDC Key so that the converter knows it should skip this blocky block
            block.persistentDataContainer.encode(VanillaCopperBlock())
        }

        @EventHandler(priority = EventPriority.LOWEST)
        fun PlayerInteractEvent.onWaxCopperDoor() {
            val block = clickedBlock ?: return
            if (hand != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK) return
            if (block.type !in CopperHelpers.COPPER_DOORS || item?.type != Material.HONEYCOMB) return

            isCancelled = true
            if (!CopperHelpers.isFakeWaxedCopper(block))
                CopperHelpers.setFakeWaxedCopper(block, true)
        }

        @EventHandler(priority = EventPriority.LOWEST)
        fun PlayerInteractEvent.onUnwaxCopperDoor() {
            val block = clickedBlock ?: return
            if (hand != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK) return
            if (block.type !in CopperHelpers.COPPER_DOORS || item?.let { MaterialTags.AXES.isTagged(it) } != true) return

            if (CopperHelpers.isFakeWaxedCopper(block))
                CopperHelpers.setFakeWaxedCopper(block, false)
        }

        @EventHandler(priority = EventPriority.LOWEST)
        fun PlayerInteractEvent.onUnwaxBlockyDoor() {
            if (clickedBlock?.type in CopperHelpers.BLOCKY_DOORS && item?.let { MaterialTags.AXES.isTagged(it) } == true)
                isCancelled = true
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        fun BlockFormEvent.onOxidizedCopperDoor() {
            if (newState.type in CopperHelpers.BLOCKY_DOORS || CopperHelpers.isFakeWaxedCopper(block))
                isCancelled = true
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        fun BlockBreakEvent.onBreakFakeCopperDoor() {
            if (!CopperHelpers.isFakeWaxedCopper(block)) return
            val index = CopperHelpers.COPPER_DOORS.indexOf(block.type)
            val waxedType = CopperHelpers.BLOCKY_DOORS.elementAt(index)
            isDropItems = false
            block.customBlockData.clear()
            block.world.dropItemNaturally(block.location, ItemStack.of(waxedType))
        }

    }

    class BlockyTrapDoorListener : Listener {

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        fun PlayerInteractEvent.onPlacingBlockyTrapDoor() {
            val (item, hand) = (item ?: return) to (hand ?: return)
            val block = (clickedBlock!!.takeIf { it.isReplaceable } ?: clickedBlock!!.getRelative(blockFace))
            if (action != Action.RIGHT_CLICK_BLOCK || hand != EquipmentSlot.HAND || item.type in CopperHelpers.BLOCKY_TRAPDOORS) return

            val blockyBlock = player.gearyInventory?.get(hand)?.get<SetBlock>() ?: return
            if (blockyBlock.blockType != SetBlock.BlockType.TRAPDOOR) return
            if (!player.isSneaking && block.isInteractable()) return

            val blockyEvent = BlockyBlockPlaceEvent(block, player, hand, item)
            if (!ProtectionLib.canBuild(player, block.location)) blockyEvent.isCancelled = true
            if (!blockyEvent.callEvent()) return setUseItemInHand(Event.Result.DENY)

            // If item being placed is a blocky copper block, we want this logic to run with an item of waxed material
            // If it is not, we want to ensure the material is not waxed copper, and if it is, change it
            val placedItem = item.takeIf(CopperHelpers::isBlockyCopper)?.let(CopperHelpers::convertToBlockyType) ?: CopperHelpers.convertToFakeType(item)
            BlockStateCorrection.placeItemAsBlock(player, hand, placedItem)

            // Set PDC Key so that the converter knows it should skip this blocky block
            block.persistentDataContainer.encode(VanillaCopperBlock())
        }

        @EventHandler(priority = EventPriority.LOWEST)
        fun PlayerInteractEvent.onWaxCopperTrapDoor() {
            val block = clickedBlock ?: return
            if (hand != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK) return
            if (block.type !in CopperHelpers.COPPER_TRAPDOORS || item?.type != Material.HONEYCOMB) return

            isCancelled = true
            if (!CopperHelpers.isFakeWaxedCopper(block))
                CopperHelpers.setFakeWaxedCopper(block, true)
        }

        @EventHandler(priority = EventPriority.LOWEST)
        fun PlayerInteractEvent.onUnwaxCopperTrapDoor() {
            val block = clickedBlock ?: return
            if (hand != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK) return
            if (block.type !in CopperHelpers.COPPER_TRAPDOORS || item?.let { MaterialTags.AXES.isTagged(it) } != true) return

            if (CopperHelpers.isFakeWaxedCopper(block))
                CopperHelpers.setFakeWaxedCopper(block, false)
        }

        @EventHandler(priority = EventPriority.LOWEST)
        fun PlayerInteractEvent.onUnwaxBlockyTrapDoor() {
            if (clickedBlock?.type in CopperHelpers.BLOCKY_TRAPDOORS && item?.let { MaterialTags.AXES.isTagged(it) } == true)
                isCancelled = true
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        fun BlockFormEvent.onOxidizedCopperTrapDoor() {
            if (newState.type in CopperHelpers.BLOCKY_TRAPDOORS || CopperHelpers.isFakeWaxedCopper(block))
                isCancelled = true
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        fun BlockBreakEvent.onBreakFakeCopperTrapDoor() {
            if (!CopperHelpers.isFakeWaxedCopper(block)) return
            val index = CopperHelpers.COPPER_TRAPDOORS.indexOf(block.type)
            val waxedType = CopperHelpers.BLOCKY_TRAPDOORS.elementAt(index)
            isDropItems = false
            block.customBlockData.clear()
            block.world.dropItemNaturally(block.location, ItemStack.of(waxedType))
        }

    }

    class BlockyGrateListener : Listener {

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        fun PlayerInteractEvent.onPlacingBlockyGrate() {
            val (item, hand) = (item ?: return) to (hand ?: return)
            val block = (clickedBlock!!.takeIf { it.isReplaceable } ?: clickedBlock!!.getRelative(blockFace))
            if (action != Action.RIGHT_CLICK_BLOCK || hand != EquipmentSlot.HAND || item.type in CopperHelpers.BLOCKY_GRATE) return

            val blockyBlock = player.gearyInventory?.get(hand)?.get<SetBlock>() ?: return
            if (blockyBlock.blockType != SetBlock.BlockType.GRATE) return
            if (!player.isSneaking && block.isInteractable()) return

            val blockyEvent = BlockyBlockPlaceEvent(block, player, hand, item)
            if (!ProtectionLib.canBuild(player, block.location)) blockyEvent.isCancelled = true
            if (!blockyEvent.callEvent()) return setUseItemInHand(Event.Result.DENY)

            // If item being placed is a blocky copper block, we want this logic to run with an item of waxed material
            // If it is not, we want to ensure the material is not waxed copper, and if it is, change it
            val placedItem = item.takeIf(CopperHelpers::isBlockyCopper)?.let(CopperHelpers::convertToBlockyType) ?: CopperHelpers.convertToFakeType(item)
            BlockStateCorrection.placeItemAsBlock(player, hand, placedItem)

            // Set PDC Key so that the converter knows it should skip this blocky block
            block.persistentDataContainer.encode(VanillaCopperBlock())
        }

        @EventHandler(priority = EventPriority.LOWEST)
        fun PlayerInteractEvent.onWaxCopperGrate() {
            val block = clickedBlock ?: return
            if (hand != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK) return
            if (block.type !in CopperHelpers.COPPER_GRATE || item?.type != Material.HONEYCOMB) return

            isCancelled = true
            if (!CopperHelpers.isFakeWaxedCopper(block))
                CopperHelpers.setFakeWaxedCopper(block, true)
        }

        @EventHandler(priority = EventPriority.LOWEST)
        fun PlayerInteractEvent.onUnwaxCopperGrate() {
            val block = clickedBlock ?: return
            if (hand != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK) return
            if (block.type !in CopperHelpers.COPPER_GRATE || item?.let { MaterialTags.AXES.isTagged(it) } != true) return

            if (CopperHelpers.isFakeWaxedCopper(block))
                CopperHelpers.setFakeWaxedCopper(block, false)
        }

        @EventHandler(priority = EventPriority.LOWEST)
        fun PlayerInteractEvent.onUnwaxBlockyGrate() {
            if (clickedBlock?.type in CopperHelpers.BLOCKY_GRATE && item?.let { MaterialTags.AXES.isTagged(it) } == true)
                isCancelled = true
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        fun BlockFormEvent.onOxidizedCopperGrate() {
            if (newState.type in CopperHelpers.BLOCKY_GRATE || CopperHelpers.isFakeWaxedCopper(block))
                isCancelled = true
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        fun BlockBreakEvent.onBreakFakeCopperGrate() {
            if (!CopperHelpers.isFakeWaxedCopper(block)) return
            val index = CopperHelpers.COPPER_GRATE.indexOf(block.type)
            val waxedType = CopperHelpers.BLOCKY_GRATE.elementAt(index)
            isDropItems = false
            block.customBlockData.clear()
            block.world.dropItemNaturally(block.location, ItemStack.of(waxedType))
        }

    }
}
