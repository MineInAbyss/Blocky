package com.mineinabyss.blocky.listeners

import com.destroystokyo.paper.MaterialTags
import com.mineinabyss.blocky.api.events.block.BlockyBlockPlaceEvent
import com.mineinabyss.blocky.components.core.VanillaCopperBlock
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.blocky.helpers.GenericHelpers.isInteractable
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.gearyBlocks
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.util.to
import io.th0rgal.protectionlib.ProtectionLib
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Slab
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
        fun PlayerInteractEvent.onPlacingBlockySlab() {
            val (block, item, hand) = (clickedBlock ?: return) to (item ?: return) to (hand?.takeIf { it == EquipmentSlot.HAND } ?: return)
            val prefabKey = player.gearyInventory?.get(hand)?.prefabs?.firstOrNull()?.get<PrefabKey>() ?: return
            val blockData = gearyBlocks.createBlockData(prefabKey) as? Slab ?: return
            player.gearyInventory?.get(hand)?.get<SetBlock>()?.takeIf { it.blockType == SetBlock.BlockType.SLAB } ?: return
            if (action != Action.RIGHT_CLICK_BLOCK || (!player.isSneaking && block.isInteractable())) return

            setUseInteractedBlock(Event.Result.DENY)

            placeBlockyBlock(player, hand, item, block, blockFace, blockData)
        }

        @EventHandler(priority = EventPriority.LOWEST)
        fun PlayerInteractEvent.onWaxCopperSlab() {
            val block = clickedBlock ?: return
            if (hand != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK) return
            if (block.type !in CopperHelpers.COPPER_STAIRS || item?.type != Material.HONEYCOMB) return

            isCancelled = true
            if (!CopperHelpers.isFakeWaxedCopper(block))
                CopperHelpers.setFakeWaxedCopper(block, true)
        }

        @EventHandler(priority = EventPriority.LOWEST)
        fun PlayerInteractEvent.onUnwaxCopperSlab() {
            val block = clickedBlock ?: return
            if (hand != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK) return
            if (block.type !in CopperHelpers.COPPER_STAIRS || item?.let { MaterialTags.AXES.isTagged(it) } != true) return

            if (CopperHelpers.isFakeWaxedCopper(block))
                CopperHelpers.setFakeWaxedCopper(block, false)
        }

        @EventHandler(priority = EventPriority.LOWEST)
        fun PlayerInteractEvent.onUnwaxBlockySlab() {
            if (clickedBlock?.type in CopperHelpers.BLOCKY_STAIRS && item?.let { MaterialTags.AXES.isTagged(it) } == true)
                isCancelled = true
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        fun BlockFormEvent.onOxidizedCopperSlab() {
            if (newState.type in CopperHelpers.BLOCKY_STAIRS || CopperHelpers.isFakeWaxedCopper(block))
                isCancelled = true
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        fun BlockBreakEvent.onBreakFakeCopperSlab() {
            if (!CopperHelpers.isFakeWaxedCopper(block)) return
            val index = CopperHelpers.COPPER_STAIRS.indexOf(block.type).takeIf { it != -1 } ?: return
            val waxedType = CopperHelpers.BLOCKY_STAIRS.elementAtOrNull(index) ?: return
            isDropItems = false
            block.customBlockData.clear()
            block.world.dropItemNaturally(block.location, ItemStack(waxedType))
        }

    }

    class BlockyStairListener : Listener {

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        fun PlayerInteractEvent.onPlacingBlockyStair() {
            val (block, item, hand) = (clickedBlock ?: return) to (item ?: return) to (hand?.takeIf { it == EquipmentSlot.HAND } ?: return)
            val prefabKey = player.gearyInventory?.get(hand)?.prefabs?.firstOrNull()?.get<PrefabKey>() ?: return
            val blockData = gearyBlocks.createBlockData(prefabKey) ?: return
            player.gearyInventory?.get(hand)?.get<SetBlock>()?.takeIf { it.blockType == SetBlock.BlockType.STAIR } ?: return
            if (action != Action.RIGHT_CLICK_BLOCK || (!player.isSneaking && block.isInteractable())) return

            setUseInteractedBlock(Event.Result.DENY)

            placeBlockyBlock(player, hand, item, block, blockFace, blockData)
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
            val index = CopperHelpers.COPPER_STAIRS.indexOf(block.type).takeIf { it != -1 } ?: return
            val waxedType = CopperHelpers.BLOCKY_STAIRS.elementAtOrNull(index) ?: return
            isDropItems = false
            block.customBlockData.clear()
            block.world.dropItemNaturally(block.location, ItemStack(waxedType))
        }

    }

    class BlockyDoorListener : Listener {

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        fun PlayerInteractEvent.onPlacingBlockyDoor() {
            val (block, item, hand) = (clickedBlock ?: return) to (item ?: return) to (hand?.takeIf { it == EquipmentSlot.HAND } ?: return)
            val prefabKey = player.gearyInventory?.get(hand)?.prefabs?.firstOrNull()?.get<PrefabKey>() ?: return
            val blockData = gearyBlocks.createBlockData(prefabKey) ?: return
            player.gearyInventory?.get(hand)?.get<SetBlock>()?.takeIf { it.blockType == SetBlock.BlockType.DOOR } ?: return
            if (action != Action.RIGHT_CLICK_BLOCK || (!player.isSneaking && block.isInteractable())) return

            setUseInteractedBlock(Event.Result.DENY)

            placeBlockyBlock(player, hand, item, block, blockFace, blockData)
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
            val index = CopperHelpers.COPPER_DOORS.indexOf(block.type).takeIf { it != -1 } ?: return
            val waxedType = CopperHelpers.BLOCKY_DOORS.elementAtOrNull(index) ?: return
            isDropItems = false
            block.customBlockData.clear()
            block.world.dropItemNaturally(block.location, ItemStack.of(waxedType))
        }

    }

    class BlockyTrapDoorListener : Listener {

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        fun PlayerInteractEvent.onPlacingBlockyTrapDoor() {
            val (block, item, hand) = (clickedBlock ?: return) to (item ?: return) to (hand?.takeIf { it == EquipmentSlot.HAND } ?: return)
            val prefabKey = player.gearyInventory?.get(hand)?.prefabs?.firstOrNull()?.get<PrefabKey>() ?: return
            val blockData = gearyBlocks.createBlockData(prefabKey) ?: return
            player.gearyInventory?.get(hand)?.get<SetBlock>()?.takeIf { it.blockType == SetBlock.BlockType.TRAPDOOR } ?: return
            if (action != Action.RIGHT_CLICK_BLOCK || (!player.isSneaking && block.isInteractable())) return

            setUseInteractedBlock(Event.Result.DENY)

            placeBlockyBlock(player, hand, item, block, blockFace, blockData)
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
            val index = CopperHelpers.COPPER_TRAPDOORS.indexOf(block.type).takeIf { it != -1 } ?: return
            val waxedType = CopperHelpers.BLOCKY_TRAPDOORS.elementAtOrNull(index) ?: return
            isDropItems = false
            block.customBlockData.clear()
            block.world.dropItemNaturally(block.location, ItemStack.of(waxedType))
        }

    }

    class BlockyGrateListener : Listener {

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        fun PlayerInteractEvent.onPlacingBlockyGrate() {
            val (block, item, hand) = (clickedBlock ?: return) to (item ?: return) to (hand?.takeIf { it == EquipmentSlot.HAND } ?: return)
            val prefabKey = player.gearyInventory?.get(hand)?.prefabs?.firstOrNull()?.get<PrefabKey>() ?: return
            val blockData = gearyBlocks.createBlockData(prefabKey) ?: return
            player.gearyInventory?.get(hand)?.get<SetBlock>()?.takeIf { it.blockType == SetBlock.BlockType.GRATE } ?: return
            if (action != Action.RIGHT_CLICK_BLOCK || (!player.isSneaking && block.isInteractable())) return

            setUseInteractedBlock(Event.Result.DENY)

            placeBlockyBlock(player, hand, item, block, blockFace, blockData)
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
            val index = CopperHelpers.COPPER_GRATE.indexOf(block.type).takeIf { it != -1 } ?: return
            val waxedType = CopperHelpers.BLOCKY_GRATE.elementAtOrNull(index) ?: return
            isDropItems = false
            block.customBlockData.clear()
            block.world.dropItemNaturally(block.location, ItemStack.of(waxedType))
        }

    }
}
