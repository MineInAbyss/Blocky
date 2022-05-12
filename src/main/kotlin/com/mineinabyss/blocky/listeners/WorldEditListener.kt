package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.blockMap
import com.mineinabyss.blocky.components.BlockType
import com.mineinabyss.blocky.components.blockyBlock
import com.mineinabyss.blocky.components.directional
import com.mineinabyss.blocky.components.isDirectional
import com.mineinabyss.blocky.helpers.getBlockyNoteBlock
import com.mineinabyss.blocky.helpers.getBlockyTransparent
import com.mineinabyss.blocky.helpers.getBlockyTripWire
import com.mineinabyss.geary.papermc.helpers.toPrefabKey
import com.mineinabyss.idofront.messaging.broadcast
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
        var note = 0
        if (!message.startsWith("//")) return
        val args: List<String> = message.split(" ")
        val argId = args.firstOrNull { it.contains("mineinabyss:") } ?: return
        val newPrefab = (argId.replace("[direction=x]", "")
            .replace("[direction=y]", "")
            .replace("[direction=z]", "").toMCKey().toPrefabKey())
        val type = newPrefab.toEntity()?.blockyBlock?.blockType
        var data: BlockData
        if (newPrefab.toEntity()?.isDirectional == true) {
            if (argId.contains("[direction=")) {
                if (argId.endsWith("[direction=x]")) {
                    data = blockMap.entries.elementAt(newPrefab.toEntity()?.directional?.xBlockId!!).key
                    note = blockMap[data]?.rem(25)!!
                } else if (argId.endsWith("[direction=y]")) {
                    data = blockMap.entries.elementAt(newPrefab.toEntity()?.directional?.yBlockId!!).key
                    note = blockMap[data]?.rem(25)!!
                } else if (argId.endsWith("[direction=z]")) {
                    data = blockMap.entries.elementAt(newPrefab.toEntity()?.directional?.zBlockId!!).key
                    note = blockMap[data]?.rem(25)!!
                }
            } else {
                data = blockMap.entries.elementAt(newPrefab.toEntity()?.directional?.yBlockId!!).key
                note = blockMap[data]?.rem(25)!!
            }
        } else {
            data = newPrefab.toEntity()!!.getBlockyNoteBlock(BlockFace.UP)
            note = blockMap[data]?.rem(25)!!
        }

        val blockData =
            if (type == BlockType.CUBE) {
                data = newPrefab.toEntity()!!.getBlockyNoteBlock(BlockFace.UP)
                String.format(
                    "%s[instrument=%s,note=%s,powered=%s]",
                    data.material.toString().lowercase(),
                    getInstrument((data as NoteBlock).instrument),
                    note,
                    data.isPowered
                )
            } else if (type == BlockType.TRANSPARENT) {
                broadcast(newPrefab.toEntity()?.blockyBlock!!.blockId)
                data = newPrefab.toEntity()!!.getBlockyTransparent(BlockFace.NORTH)
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
