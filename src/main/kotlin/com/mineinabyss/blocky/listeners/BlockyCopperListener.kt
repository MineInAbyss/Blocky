package com.mineinabyss.blocky.listeners

import com.destroystokyo.paper.MaterialTags
import com.mineinabyss.blocky.api.BlockyBlocks.gearyBlocks
import com.mineinabyss.blocky.helpers.CopperHelpers
import com.mineinabyss.blocky.helpers.GenericHelpers.isInteractable
import com.mineinabyss.blocky.helpers.customBlockData
import com.mineinabyss.blocky.helpers.gearyInventory
import com.mineinabyss.blocky.helpers.placeBlockyBlock
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.withGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.util.to
import org.bukkit.GameMode
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

class BlockyCopperListener : Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.onPlacingBlockyCopper() = player.withGeary {
        val (block, item, hand) = (clickedBlock ?: return) to (item
            ?: return) to (hand?.takeIf { it == EquipmentSlot.HAND } ?: return)
        val prefabKey = player.gearyInventory?.get(hand)?.prefabs?.firstOrNull()?.get<PrefabKey>() ?: return
        val blockData = gearyBlocks.createBlockData(prefabKey) ?: return
        player.gearyInventory?.get(hand)?.get<SetBlock>()?.takeIf { it.blockType.isCopper } ?: return
        if (action != Action.RIGHT_CLICK_BLOCK || (!player.isSneaking && block.isInteractable())) return
        if (!CopperHelpers.isFeatureEnabled(blockData)) return

        setUseInteractedBlock(Event.Result.DENY)

        placeBlockyBlock(player, hand, item, block, blockFace, blockData)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun PlayerInteractEvent.onWaxCopper() {
        val block = clickedBlock ?: return
        if (hand != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK) return
        if (block.type !in CopperHelpers.VANILLA_COPPER || item?.type != Material.HONEYCOMB) return
        if (!CopperHelpers.isFeatureEnabled(block.blockData)) return

        isCancelled = true
        if (!CopperHelpers.isFakeWaxedCopper(block))
            CopperHelpers.setFakeWaxedCopper(block, true)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun PlayerInteractEvent.onUnwaxCopper() {
        val block = clickedBlock ?: return
        if (hand != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK) return
        if (block.type !in CopperHelpers.VANILLA_COPPER || item?.let { MaterialTags.AXES.isTagged(it) } != true) return
        if (!CopperHelpers.isFeatureEnabled(block.blockData)) return

        if (CopperHelpers.isFakeWaxedCopper(block))
            CopperHelpers.setFakeWaxedCopper(block, false)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun PlayerInteractEvent.onUnwaxBlockyCopper() {
        if (action != Action.RIGHT_CLICK_BLOCK || !CopperHelpers.isFeatureEnabled(clickedBlock!!.blockData)) return
        if (clickedBlock?.type !in CopperHelpers.BLOCKY_COPPER || item?.let { MaterialTags.AXES.isTagged(it) } != true) return

        isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun BlockFormEvent.onOxidizedCopper() {
        if (!CopperHelpers.isFeatureEnabled(newState.blockData)) return
        if (newState.type !in CopperHelpers.BLOCKY_COPPER && !CopperHelpers.isFakeWaxedCopper(block)) return

        isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockBreakEvent.onBreakFakeCopper() {
        if (!CopperHelpers.isFeatureEnabled(block.blockData) || !CopperHelpers.isFakeWaxedCopper(block)) return
        val index = CopperHelpers.VANILLA_COPPER.indexOf(block.type).takeIf { it != -1 } ?: return
        val waxedType = CopperHelpers.BLOCKY_COPPER.elementAtOrNull(index) ?: return

        isDropItems = false
        block.customBlockData.clear()
        if (player.gameMode != GameMode.CREATIVE) block.world.dropItemNaturally(block.location, ItemStack(waxedType))
    }
}
