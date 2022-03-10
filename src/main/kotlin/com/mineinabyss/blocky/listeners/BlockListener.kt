package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.BlockType
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.blocky.components.BlockyType
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.geary.papermc.store.encodeComponentsTo
import com.mineinabyss.idofront.entities.leftClicked
import com.mineinabyss.idofront.entities.rightClicked
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.spawning.spawn
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.*
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.NotePlayEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import kotlin.math.roundToInt


class BlockListener : Listener {

    @EventHandler
    fun NotePlayEvent.cancelBlockyNotes() {
        isCancelled = true
    }

    @EventHandler
    fun PlayerInteractEvent.onChangingNote() {
        if (clickedBlock?.type == Material.NOTE_BLOCK && rightClicked) isCancelled = true
    }

    @EventHandler
    fun BlockPlaceEvent.onPlacingBlockyBlock() {
        val blockyItem = itemInHand.toGearyOrNull(player)?.get<BlockyType>() ?: return
        if (blockyItem.blockType != BlockType.NORMAL) return
        if (!itemInHand.itemMeta.hasCustomModelData()) return

        val blockId = blockyItem.blockModelId
        val data = block.blockData as NoteBlock
        val instrumentId = (blockId / 25).toDouble().toInt()
        val noteId = instrumentId * 25

        data.instrument = when (instrumentId) {
            0 -> Instrument.PIANO
            1 -> Instrument.BASS_DRUM
            2 -> Instrument.SNARE_DRUM
            3 -> Instrument.STICKS
            4 -> Instrument.BASS_GUITAR
            5 -> Instrument.FLUTE
            6 -> Instrument.BELL
            7 -> Instrument.GUITAR
            8 -> Instrument.CHIME
            9 -> Instrument.XYLOPHONE
            10 -> Instrument.IRON_XYLOPHONE
            11 -> Instrument.COW_BELL
            12 -> Instrument.DIDGERIDOO
            13 -> Instrument.BIT
            14 -> Instrument.BANJO
            15 -> Instrument.PLING
            else -> Instrument.PIANO
        }

        data.note = Note((blockId - noteId))
        block.blockData = data
    }

    @EventHandler
    fun PlayerInteractEvent.onPlacingBlockyMisc() {
        val item = player.inventory.itemInMainHand
        val blockyItem = item.toGearyOrNull(player)?.get<BlockyType>() ?: return
        val blockyInfo = item.toGearyOrNull(player)?.get<BlockyInfo>() ?: return

        if (hand != EquipmentSlot.HAND) return
        if (blockyItem.blockType == BlockType.NORMAL) return

        val loc = player.getLastTwoTargetBlocks(null, 6)
        val newLoc =
            if (blockFace == BlockFace.UP)
                loc.last()?.location?.toCenterLocation()?.apply { y += 0.5 } ?: return
            else
                loc.first()?.location?.toCenterLocation()?.apply { y -= 0.5 } ?: return

        newLoc.spawn<ArmorStand>()?.apply {
            setRotation(player.location.yaw - 180, 0.0F)
            toGearyOrNull()?.getOrSetPersisting { blockyInfo }
            this.toGearyOrNull()?.encodeComponentsTo(this)
        } ?: return

        if (player.gameMode != GameMode.CREATIVE) player.inventory.itemInMainHand.subtract()
        player.playSound(newLoc, blockyInfo.placeSound, 1f, 1f)
    }

    @EventHandler
    fun EntityDamageByEntityEvent.onBreakingBlockyMisc() {
        val blocky = entity.toGeary().get<BlockyInfo>() ?: return
        if (!blocky.canBeBroken && (damager as Player).gameMode != GameMode.CREATIVE) isCancelled = true
    }
}