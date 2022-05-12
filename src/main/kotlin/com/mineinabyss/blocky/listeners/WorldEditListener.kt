package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.blockMap
import com.mineinabyss.blocky.components.BlockType
import com.mineinabyss.blocky.components.blockyBlock
import com.mineinabyss.blocky.components.isDirectional
import com.mineinabyss.blocky.helpers.getBlockyNoteBlock
import com.mineinabyss.blocky.helpers.getBlockyTransparent
import com.mineinabyss.blocky.helpers.getBlockyTripWire
import com.mineinabyss.geary.papermc.helpers.toPrefabKey
import com.mineinabyss.idofront.util.toMCKey
import org.bukkit.Instrument
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.MultipleFacing
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.block.data.type.Tripwire
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent


class WorldEditListener : Listener {

    @EventHandler
    fun PlayerCommandPreprocessEvent.commandStuff() {
        if (!message.startsWith("//")) return
        val args: List<String> = message.split(" ")
        val argId = args.firstOrNull { it.contains("mineinabyss:") } ?: return
        val newPrefab = (argId.replace("[direction=up]", "")
            .replace("[direction=down]", "")
            .replace("[direction=north]", "")
            .replace("[direction=south]", "")
            .replace("[direction=west]", "")
            .replace("[direction=east]", "").toMCKey().toPrefabKey())
        val type = newPrefab.toEntity()?.blockyBlock?.blockType
        var data: BlockData
        if (newPrefab.toEntity()?.isDirectional == true) {
            if (argId.endsWith("[direction=up]")) {
                data =
                    if (type == BlockType.CUBE)
                        newPrefab.toEntity()!!.getBlockyNoteBlock(BlockFace.UP)
                    else newPrefab.toEntity()!!.getBlockyTransparent(BlockFace.UP)
            } else if (argId.endsWith("[direction=down]")) {
                data =
                    if (type == BlockType.CUBE)
                        newPrefab.toEntity()!!.getBlockyNoteBlock(BlockFace.DOWN)
                    else newPrefab.toEntity()!!.getBlockyTransparent(BlockFace.DOWN)
            } else if (argId.endsWith("[direction=west]")) {
                data =
                    if (type == BlockType.CUBE)
                        newPrefab.toEntity()!!.getBlockyNoteBlock(BlockFace.NORTH)
                    else newPrefab.toEntity()!!.getBlockyTransparent(BlockFace.NORTH)
            } else if (argId.endsWith("[direction=east]")) {
                data =
                    if (type == BlockType.CUBE)
                        newPrefab.toEntity()!!.getBlockyNoteBlock(BlockFace.SOUTH)
                    else newPrefab.toEntity()!!.getBlockyTransparent(BlockFace.SOUTH)
            } else if (argId.endsWith("[direction=north]")) {
                data =
                    if (type == BlockType.CUBE)
                        newPrefab.toEntity()!!.getBlockyNoteBlock(BlockFace.WEST)
                    else newPrefab.toEntity()!!.getBlockyTransparent(BlockFace.WEST)
            } else if (argId.endsWith("[direction=south]")) {
                data =
                    if (type == BlockType.CUBE)
                        newPrefab.toEntity()!!.getBlockyNoteBlock(BlockFace.EAST)
                    else newPrefab.toEntity()!!.getBlockyTransparent(BlockFace.EAST)
            } else {
                data =
                    if (type == BlockType.CUBE)
                        newPrefab.toEntity()!!.getBlockyNoteBlock(BlockFace.UP)
                    else newPrefab.toEntity()!!.getBlockyTransparent(BlockFace.UP)
            }
        } else {
            data =
                if (type == BlockType.CUBE)
                    newPrefab.toEntity()!!.getBlockyNoteBlock(BlockFace.UP)
                else newPrefab.toEntity()!!.getBlockyTransparent(BlockFace.UP)
        }

        val blockData =
            if (type == BlockType.CUBE) {
                String.format(
                    "%s[instrument=%s,note=%s,powered=%s]",
                    Material.NOTE_BLOCK.toString().lowercase(),
                    getInstrument((data as NoteBlock).instrument),
                    blockMap[data]?.rem(25)!!,
                    data.isPowered
                )
            } else if (type == BlockType.TRANSPARENT) {
                String.format(
                    "%s[north=%s,south=%s,west=%s,east=%s,up=%s,down=%s]",
                    Material.CHORUS_PLANT.toString(),
                    (data as MultipleFacing).hasFace(BlockFace.NORTH),
                    data.hasFace(BlockFace.SOUTH),
                    data.hasFace(BlockFace.WEST),
                    data.hasFace(BlockFace.EAST),
                    data.hasFace(BlockFace.UP),
                    data.hasFace(BlockFace.DOWN)
                )
            } else if (type == BlockType.GROUND) {
                data = newPrefab.toEntity()!!.blockyBlock!!.getBlockyTripWire()
                String.format(
                    "%s[north=%s,south=%s,west=%s,east=%s,attached=%s,disarmed=%s,powered=%s]",
                    Material.TRIPWIRE.toString(),
                    (data as Tripwire).hasFace(BlockFace.NORTH),
                    data.hasFace(BlockFace.SOUTH),
                    data.hasFace(BlockFace.WEST),
                    data.hasFace(BlockFace.EAST),
                    data.isAttached,
                    data.isDisarmed,
                    data.isPowered
                )
            } else ""
        message = message.replace(argId, blockData, true)
    }
}

private fun getInstrument(id: Instrument): String {
    when (id) {
        Instrument.BASS_DRUM -> return "basedrum"
        Instrument.STICKS -> return "hat"
        Instrument.SNARE_DRUM -> return "snare"
        Instrument.PIANO -> return "harp"
        Instrument.BASS_GUITAR -> return "bass"
        Instrument.FLUTE -> return "flute"
        Instrument.BELL -> return "bell"
        Instrument.GUITAR -> return "guitar"
        Instrument.CHIME -> return "chime"
        Instrument.XYLOPHONE -> return "xylophone"
        Instrument.IRON_XYLOPHONE -> return "iron_xylophone"
        Instrument.COW_BELL -> return "cow_bell"
        Instrument.DIDGERIDOO -> return "didgeridoo"
        Instrument.BIT -> return "bit"
        Instrument.BANJO -> return "banjo"
        Instrument.PLING -> return "pling"
        else -> return "hat"
    }
}
