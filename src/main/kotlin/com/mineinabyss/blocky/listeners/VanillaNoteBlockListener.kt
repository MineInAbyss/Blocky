package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.VanillaNoteBlock
import com.mineinabyss.blocky.helpers.NoteBlockHelpers
import com.mineinabyss.blocky.helpers.persistentDataContainer
import com.mineinabyss.blocky.helpers.vanillaNoteBlock
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.idofront.entities.rightClicked
import com.mineinabyss.idofront.messaging.broadcast
import org.bukkit.Material
import org.bukkit.Note
import org.bukkit.block.Block
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
    fun NotePlayEvent.onNotePlay() {
        isCancelled = true
        block.vanillaNoteBlock?.interact(block, null, Action.LEFT_CLICK_BLOCK)
    }

    @EventHandler
    fun BlockPhysicsEvent.onRedstone() {
        val vanillaNoteBlock = block.takeIf { it.type == Material.NOTE_BLOCK }?.vanillaNoteBlock ?: return

        if (!block.isBlockIndirectlyPowered) return vanillaNoteBlock.powered(block, false)
        if (!vanillaNoteBlock.powered()) vanillaNoteBlock.interact(block, null, Action.PHYSICAL)
        vanillaNoteBlock.powered(block, true)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun PlayerInteractEvent.onChangingNote() {
        val (block, vanillaNoteBlock) = (clickedBlock ?: return) to (clickedBlock?.vanillaNoteBlock ?: return)
        val (mainHand, offHand) = player.inventory.let { it.itemInMainHand to it.itemInOffHand }

        if (!rightClicked || (mainHand.itemMeta is SkullMeta && blockFace == BlockFace.UP)) return
        if (player.isSneaking && (!mainHand.isEmpty || offHand.isEmpty)) return

        setUseInteractedBlock(Event.Result.DENY)
        vanillaNoteBlock.interact(block, player, Action.RIGHT_CLICK_BLOCK)
    }

}