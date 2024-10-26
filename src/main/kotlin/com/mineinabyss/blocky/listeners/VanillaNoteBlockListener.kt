package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.helpers.vanillaNoteBlock
import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.idofront.entities.rightClicked
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.block.NotePlayEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.meta.SkullMeta

class VanillaNoteBlockListener: Listener {
    @EventHandler
    fun NotePlayEvent.onNotePlay() = with(block.world.toGeary()) {
        isCancelled = true
        block.vanillaNoteBlock?.interact(block, null, Action.LEFT_CLICK_BLOCK)
    }

    @EventHandler
    fun BlockPhysicsEvent.onRedstone() = with(block.world.toGeary()) {
        val vanillaNoteBlock = block.takeIf { it.type == Material.NOTE_BLOCK }?.vanillaNoteBlock ?: return

        if (!block.isBlockIndirectlyPowered) return vanillaNoteBlock.powered(block, false)
        if (!vanillaNoteBlock.powered()) vanillaNoteBlock.interact(block, null, Action.PHYSICAL)
        vanillaNoteBlock.powered(block, true)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun PlayerInteractEvent.onChangingNote() = with(player.world.toGeary()) {
        val (block, vanillaNoteBlock) = (clickedBlock ?: return) to (clickedBlock?.vanillaNoteBlock ?: return)
        val (mainHand, offHand) = player.inventory.let { it.itemInMainHand to it.itemInOffHand }

        if (!rightClicked || (mainHand.itemMeta is SkullMeta && blockFace == BlockFace.UP)) return
        if (player.isSneaking && (!mainHand.isEmpty || offHand.isEmpty)) return

        setUseInteractedBlock(Event.Result.DENY)
        vanillaNoteBlock.interact(block, player, Action.RIGHT_CLICK_BLOCK)
    }

}
