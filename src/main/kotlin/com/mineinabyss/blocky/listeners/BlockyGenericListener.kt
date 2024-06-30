package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.api.BlockyBlocks.isBlockyBlock
import com.mineinabyss.blocky.api.events.block.BlockyBlockBreakEvent
import com.mineinabyss.blocky.api.events.block.BlockyBlockDamageAbortEvent
import com.mineinabyss.blocky.api.events.block.BlockyBlockDamageEvent
import com.mineinabyss.blocky.api.events.block.BlockyBlockInteractEvent
import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.features.BlockyBreaking
import com.mineinabyss.blocky.components.features.mining.PlayerMiningAttribute
import com.mineinabyss.blocky.components.features.mining.miningAttribute
import com.mineinabyss.blocky.helpers.CopperHelpers
import com.mineinabyss.blocky.helpers.gearyInventory
import com.mineinabyss.blocky.helpers.handleBlockyDrops
import com.mineinabyss.blocky.helpers.isVanillaNoteBlock
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.idofront.util.to
import io.th0rgal.protectionlib.ProtectionLib
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Slab
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.player.PlayerInteractEvent

class BlockyGenericListener : Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockDamageEvent.onDamageCustomBlock() {
        player.miningAttribute?.takeUnless { block.isVanillaNoteBlock }?.removeModifier(player)
        if (player.gameMode == GameMode.CREATIVE) return

        val breaking = block.toGearyOrNull()?.get<BlockyBreaking>() ?: return
        PlayerMiningAttribute(breaking.createBreakingModifier(player, block)).let {
            player.toGearyOrNull()?.set(it)
            it.addTransientModifier(player)
        }

        if (!BlockyBlockDamageEvent(block, player).callEvent()) isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun BlockDamageAbortEvent.onCancelMine() {
        BlockyBlockDamageAbortEvent(block, player).takeIf { block.isBlockyBlock }?.callEvent()
        player.miningAttribute?.removeModifier(player)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockBreakEvent.onBreakCustomBlock() {
        player.miningAttribute?.removeModifier(player)
        if (!block.isBlockyBlock) return
        if (!BlockyBlockBreakEvent(block, player).callEvent()) isCancelled = true
        if (!ProtectionLib.canBreak(player, block.location)) isCancelled = true
        if (isCancelled) return

        if (player.gameMode != GameMode.CREATIVE) player.inventory.itemInMainHand.damage(1, player)
        handleBlockyDrops(block, player)
        isDropItems = false
    }

    private val materialSet = setOf(Material.NOTE_BLOCK, Material.STRING, Material.CAVE_VINES).asSequence()
        .plus(CopperHelpers.BLOCKY_COPPER).plus(CopperHelpers.VANILLA_COPPER)

    // Handles logic for correctly placing fake slabs and real unwaxed slabs
    // Minor issue with placing when block below is wrong but it would place in same, minor bug not important
    //TODO Try and swap this to use BlockStateCorrection eventually
    @EventHandler
    fun PlayerInteractEvent.onPrePlacingSlab() {
        val (block, item) = (clickedBlock.takeIf { it?.blockData is Slab } ?: return) to (item ?: return)
        val relative = block.getRelative(blockFace)
        if (action != Action.RIGHT_CLICK_BLOCK) return

        if (CopperHelpers.isFakeWaxedCopper(block) && item.type == block.type) {
            setUseItemInHand(Event.Result.DENY)
            setUseInteractedBlock(Event.Result.DENY)
            if (CopperHelpers.isFakeWaxedCopper(relative) && item.type == relative.type) return
            else if (!relative.canPlace(item.type.createBlockData())) return
            else {
                val newDataR = (item.type.createBlockData() as Slab).apply {
                    if (relative.type == item.type) type = Slab.Type.DOUBLE
                    else if (relative.isEmpty || relative.isReplaceable)
                        type = if (blockFace == BlockFace.UP) Slab.Type.BOTTOM else Slab.Type.TOP
                }
                if (relative.canPlace(newDataR)) relative.blockData = newDataR
            }
        } else if (CopperHelpers.isFakeWaxedCopper(relative) && item.type == relative.type) {
            val relativeData = relative.blockData as? Slab ?: return
            if (relativeData.type == Slab.Type.BOTTOM && blockFace == BlockFace.DOWN) {
                setUseItemInHand(Event.Result.DENY)
                setUseInteractedBlock(Event.Result.DENY)
            } else if (relativeData.type == Slab.Type.TOP && blockFace == BlockFace.UP) {
                setUseItemInHand(Event.Result.DENY)
                setUseInteractedBlock(Event.Result.DENY)
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun BlockPlaceEvent.onPlacingDefaultBlock() {
        val heldType = itemInHand.type
        val placedData = blockPlaced.blockData

        when {
            heldType !in materialSet -> return
            blockPlaced.isBlockyBlock && player.gearyInventory?.get(hand)?.has<SetBlock>() == true -> return
            // TODO Are these even needed?
            !blocky.config.noteBlocks.isEnabled && heldType == Material.NOTE_BLOCK -> return
            !blocky.config.tripWires.isEnabled && heldType == Material.STRING -> return
            !blocky.config.caveVineBlocks.isEnabled && heldType == Material.CAVE_VINES -> return
            !blocky.config.slabBlocks.isEnabled && heldType in CopperHelpers.BLOCKY_SLABS -> return
            !blocky.config.stairBlocks.isEnabled && heldType in CopperHelpers.BLOCKY_STAIRS -> return
            !blocky.config.doorBlocks.isEnabled && heldType in CopperHelpers.BLOCKY_DOORS -> return
            !blocky.config.trapdoorBlocks.isEnabled && heldType in CopperHelpers.BLOCKY_TRAPDOORS -> return
            !blocky.config.grateBlocks.isEnabled && heldType in CopperHelpers.BLOCKY_GRATE -> return
        }

        val newData = when (heldType) {
            Material.STRING -> Material.TRIPWIRE
            in CopperHelpers.BLOCKY_SLABS -> CopperHelpers.COPPER_SLABS.elementAt(CopperHelpers.BLOCKY_SLABS.indexOf(heldType))
            in CopperHelpers.BLOCKY_STAIRS -> CopperHelpers.COPPER_STAIRS.elementAt(CopperHelpers.BLOCKY_STAIRS.indexOf(heldType))
            in CopperHelpers.BLOCKY_DOORS -> CopperHelpers.COPPER_DOORS.elementAt(CopperHelpers.BLOCKY_DOORS.indexOf(heldType))
            in CopperHelpers.BLOCKY_TRAPDOORS -> CopperHelpers.COPPER_TRAPDOORS.elementAt(CopperHelpers.BLOCKY_TRAPDOORS.indexOf(heldType))
            in CopperHelpers.BLOCKY_GRATE -> CopperHelpers.COPPER_GRATE.elementAt(CopperHelpers.BLOCKY_GRATE.indexOf(heldType))
            else -> heldType
        }.createBlockData().apply(placedData::copyTo)

        blockPlaced.blockData = newData
        if (heldType in CopperHelpers.BLOCKY_COPPER) CopperHelpers.setFakeWaxedCopper(blockPlaced, true)
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun PlayerInteractEvent.onInteractBlockyBlock() {
        val (block, hand, item) = (clickedBlock ?: return) to (hand ?: return) to (item ?: return)
        if (action != Action.RIGHT_CLICK_BLOCK || !block.isBlockyBlock) return
        val event = BlockyBlockInteractEvent(block, player, hand, item, blockFace)
        if (!event.callEvent()) isCancelled = true
    }
}
