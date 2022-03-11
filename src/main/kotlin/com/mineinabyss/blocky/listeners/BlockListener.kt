package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.components.BlockModelType
import com.mineinabyss.blocky.components.BlockType
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.blocky.components.BlockyType
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.spawnFromPrefab
import com.mineinabyss.idofront.entities.rightClicked
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.GameMode
import org.bukkit.Instrument
import org.bukkit.Material
import org.bukkit.Note
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.NotePlayEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot


class BlockListener : Listener {

    @EventHandler
    fun NotePlayEvent.cancelBlockyNotes() {
        isCancelled = true
    }

    @EventHandler
    fun PlayerInteractEvent.onChangingNote() {
        (clickedBlock?.blockData as NoteBlock).instrument.broadcastVal()
        (clickedBlock?.blockData as NoteBlock).note.id.broadcastVal()
        if (clickedBlock?.type == Material.NOTE_BLOCK && rightClicked) isCancelled = true
    }

    @EventHandler
    fun BlockPlaceEvent.onPlacingBlockyBlock() {
        val blockyType = itemInHand.toGearyOrNull(player)?.get<BlockyType>() ?: return
        val blockyItem = itemInHand.toGearyOrNull(player)?.get<BlockyInfo>() ?: return

        //if (!itemInHand.itemMeta.hasCustomModelData()) return
        if (blockyType.blockType == BlockType.NORMAL) {
            block.setType(Material.NOTE_BLOCK, false)

            val blockId = blockyItem.modelId.toInt()
            val data = block.blockData as NoteBlock
            val instrumentId = (blockId / 25).toDouble().toInt()
            val noteId = instrumentId * 25

            data.instrument = when (instrumentId) {
                0 -> Instrument.BASS_DRUM
                1 -> Instrument.PIANO
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
        else if (blockyType.blockType == BlockType.PASSTHROUGH) {
            block.setType(Material.TRIPWIRE, false)
        }
    }

    @EventHandler
    fun PlayerInteractEvent.onPlacingBlockyMisc() {
        val item = player.inventory.itemInMainHand
        val blockyType = item.toGearyOrNull(player)?.get<BlockyType>() ?: return
        val blockyInfo = item.toGearyOrNull(player)?.get<BlockyInfo>() ?: return
        val loc = interactionPoint!!.toCenterLocation()

        if (hand != EquipmentSlot.HAND) return
        if (action != Action.RIGHT_CLICK_BLOCK) return
        if (blockyType.blockType == BlockType.NORMAL || blockyType.blockType == BlockType.PASSTHROUGH) return

        //TODO This will probably not work like it does in Mobzy
        if (blockyType.blockModelType == BlockModelType.MODELENGINE) {
            loc.spawnFromPrefab(item.toGearyOrNull(player)!!)

            if (player.gameMode != GameMode.CREATIVE) player.inventory.itemInMainHand.subtract()
            player.playSound(loc, blockyInfo.placeSound, 1f, 1f)
        }
    }

    @EventHandler
    fun EntityDamageByEntityEvent.onBreakingBlockyMisc() {
        val blocky = entity.toGeary().get<BlockyInfo>() ?: return
        if (!blocky.canBeBroken && (damager as Player).gameMode != GameMode.CREATIVE) isCancelled = true
    }
}