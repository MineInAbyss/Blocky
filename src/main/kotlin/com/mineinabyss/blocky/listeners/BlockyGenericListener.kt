package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.api.BlockyBlocks.isBlockyBlock
import com.mineinabyss.blocky.api.events.block.BlockyBlockDamageAbortEvent
import com.mineinabyss.blocky.api.events.block.BlockyBlockDamageEvent
import com.mineinabyss.blocky.api.events.block.BlockyBlockInteractEvent
import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.features.BlockyBreaking
import com.mineinabyss.blocky.components.features.mining.PlayerMiningAttribute
import com.mineinabyss.blocky.components.features.mining.miningAttribute
import com.mineinabyss.blocky.helpers.CopperHelpers
import com.mineinabyss.blocky.helpers.attemptBreakBlockyBlock
import com.mineinabyss.blocky.helpers.gearyInventory
import com.mineinabyss.blocky.helpers.to
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.messaging.broadcastVal
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Stairs
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.to

class BlockyGenericListener : Listener {

    @EventHandler(ignoreCancelled = true)
    fun BlockDamageEvent.onDamage() {
        player.getAttribute(Attribute.PLAYER_BLOCK_BREAK_SPEED)?.value.broadcastVal("<red>" + player.name + ": ")

        player.miningAttribute?.removeModifier(player)
        if (player.gameMode == GameMode.CREATIVE) return

        val breaking = block.toGearyOrNull()?.get<BlockyBreaking>() ?: return
        PlayerMiningAttribute(breaking.createBreakingModifier(block)).let {
            player.toGearyOrNull()?.set(it)
            it.addTransientModifier(player)
        }
        player.getAttribute(Attribute.PLAYER_BLOCK_BREAK_SPEED)?.value.broadcastVal("<green>" + player.name + ": ")

        isCancelled = true
        BlockyBlockDamageEvent(block, player).callEvent()
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun BlockDamageAbortEvent.onCancelMine() {
        BlockyBlockDamageAbortEvent(block, player).takeIf { block.isBlockyBlock }?.callEvent()
        player.miningAttribute?.removeModifier(player)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockBreakEvent.onBreakBlockyBlock() {
        if (!block.isBlockyBlock) return
        attemptBreakBlockyBlock(block, player) || return
        isDropItems = false
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun BlockPlaceEvent.onPlacingDefaultBlock() {
        val materialSet =
            setOf(Material.NOTE_BLOCK, Material.STRING, Material.CAVE_VINES).plus(CopperHelpers.BLOCKY_SLABS)
                .plus(CopperHelpers.BLOCKY_STAIRS)

        when {
            itemInHand.type !in materialSet -> return
            blockPlaced.isBlockyBlock && player.gearyInventory?.get(hand)?.has<SetBlock>() == true -> return
            // TODO Are these even needed?
            !blocky.config.noteBlocks.isEnabled && itemInHand.type == Material.NOTE_BLOCK -> return
            !blocky.config.tripWires.isEnabled && itemInHand.type == Material.STRING -> return
            !blocky.config.caveVineBlocks.isEnabled && itemInHand.type == Material.CAVE_VINES -> return
            !blocky.config.slabBlocks.isEnabled && itemInHand.type in CopperHelpers.BLOCKY_SLABS -> return
            !blocky.config.stairBlocks.isEnabled && itemInHand.type in CopperHelpers.BLOCKY_STAIRS -> return
        }

        val newData = when (itemInHand.type) {
            Material.STRING -> Material.TRIPWIRE.createBlockData()
            in CopperHelpers.BLOCKY_SLABS -> CopperHelpers.COPPER_SLABS.elementAt(
                CopperHelpers.BLOCKY_SLABS.indexOf(
                    itemInHand.type
                )
            ).createBlockData {
                (it as Slab to blockPlaced.blockData as Slab).let { (new, old) -> new.type = old.type }
            }

            in CopperHelpers.BLOCKY_STAIRS -> CopperHelpers.COPPER_STAIRS.elementAt(
                CopperHelpers.BLOCKY_STAIRS.indexOf(
                    itemInHand.type
                )
            ).createBlockData {
                (it as Stairs to blockPlaced.blockData as Stairs).let { (new, old) ->
                    new.facing = old.facing; new.half = old.half
                }
            }

            else -> itemInHand.type.createBlockData()
        }

        blockPlaced.blockData = newData
        CopperHelpers.setFakeWaxedCopper(blockPlaced, true)
        player.swingMainHand()
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun PlayerInteractEvent.onInteractBlockyBlock() {
        val (block, hand, item) = (clickedBlock ?: return) to (hand ?: return) to (item ?: return)
        if (action != Action.RIGHT_CLICK_BLOCK || !block.isBlockyBlock) return
        val event = BlockyBlockInteractEvent(block, player, hand, item, blockFace)
        if (!event.callEvent()) isCancelled = true
    }
}
