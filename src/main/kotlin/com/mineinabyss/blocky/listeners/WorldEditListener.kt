package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.blockMap
import com.mineinabyss.blocky.components.BlockType
import com.mineinabyss.blocky.components.BlockyBlock
import com.mineinabyss.blocky.helpers.getBlockyNoteBlock
import com.mineinabyss.blocky.helpers.getBlockyTransparent
import com.mineinabyss.blocky.helpers.getBlockyTripWire
import com.mineinabyss.blocky.helpers.leafList
import com.mineinabyss.geary.papermc.helpers.toPrefabKey
import com.mineinabyss.idofront.util.toMCKey
import org.bukkit.Instrument
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.MultipleFacing
import org.bukkit.block.data.type.CaveVines
import org.bukkit.block.data.type.Leaves
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.block.data.type.Tripwire
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent


class WorldEditListener : Listener {

    @EventHandler
    fun PlayerCommandPreprocessEvent.commandStuff() {
        if (!message.startsWith("//")) return
        val argId = message.split(" ").firstOrNull { it.contains("mineinabyss:") } ?: return
        val prefab = (argId.replace("[direction=up]", "")
            .replace("[direction=down]", "")
            .replace("[direction=north]", "")
            .replace("[direction=south]", "")
            .replace("[direction=west]", "")
            .replace("[direction=east]", "").toMCKey().toPrefabKey()).toEntity()
        val type = prefab.get<BlockyBlock>()?.blockType ?: return
        val data = when {
                type == BlockType.GROUND -> prefab.get<BlockyBlock>()!!.getBlockyTripWire()
                argId.endsWith("[direction=up]") -> {
                    if (type == BlockType.CUBE) prefab.getBlockyNoteBlock(BlockFace.UP)
                    else prefab.getBlockyTransparent(BlockFace.UP)
                }
                argId.endsWith("[direction=north]") -> {
                    if (type == BlockType.CUBE)
                        prefab.getBlockyNoteBlock(BlockFace.NORTH)
                    else prefab.getBlockyTransparent(BlockFace.NORTH)
                }
                argId.endsWith("[direction=south]") -> {
                    if (type == BlockType.CUBE)
                        prefab.getBlockyNoteBlock(BlockFace.SOUTH)
                    else prefab.getBlockyTransparent(BlockFace.SOUTH)
                }
                argId.endsWith("[direction=west]") -> {
                    if (type == BlockType.CUBE)
                        prefab.getBlockyNoteBlock(BlockFace.WEST)
                    else prefab.getBlockyTransparent(BlockFace.WEST)
                }
                argId.endsWith("[direction=east]") -> {
                    if (type == BlockType.CUBE)
                        prefab.getBlockyNoteBlock(BlockFace.EAST)
                    else prefab.getBlockyTransparent(BlockFace.EAST)
                }
                else -> {
                    if (type == BlockType.CUBE)
                        prefab.getBlockyNoteBlock(BlockFace.UP)
                    else prefab.getBlockyTransparent(BlockFace.UP)
                }
            }

        val blockData = when (type) {
                BlockType.CUBE ->
                    String.format(
                        "%s[instrument=%s,note=%s,powered=%s]",
                        Material.NOTE_BLOCK.toString().lowercase(),
                        getInstrument((data as NoteBlock).instrument),
                        blockMap[data]?.rem(25)!!,
                        data.isPowered
                    )
                BlockType.TRANSPARENT ->
                    String.format(
                        "%s[north=%s,south=%s,west=%s,east=%s,up=%s,down=%s]",
                        Material.CHORUS_PLANT.toString().lowercase(),
                        (data as MultipleFacing).hasFace(BlockFace.NORTH),
                        data.hasFace(BlockFace.SOUTH),
                        data.hasFace(BlockFace.WEST),
                        data.hasFace(BlockFace.EAST),
                        data.hasFace(BlockFace.UP),
                        data.hasFace(BlockFace.DOWN)
                    )
                BlockType.GROUND ->
                    String.format(
                        "%s[north=%s,south=%s,west=%s,east=%s,attached=%s,disarmed=%s,powered=%s]",
                        Material.TRIPWIRE.toString().lowercase(),
                        (data as Tripwire).hasFace(BlockFace.NORTH),
                        data.hasFace(BlockFace.SOUTH),
                        data.hasFace(BlockFace.WEST),
                        data.hasFace(BlockFace.EAST),
                        data.isAttached,
                        data.isDisarmed,
                        data.isPowered
                    )
                BlockType.LEAF ->
                    String.format(
                        "%s[distance=%s,persistent=true,waterlogged=false]",
                        leafList[blockMap[data]!!].toString().lowercase(),
                        (data as Leaves).distance
                    )
                BlockType.CAVEVINE ->
                    String.format(
                        "%s[age=%s,berries=%s]",
                        Material.CAVE_VINES.toString().lowercase(),
                        (data as CaveVines).age,
                        data.isBerries
                    )
            }
        message = message.replace(argId, blockData, true)
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
}
