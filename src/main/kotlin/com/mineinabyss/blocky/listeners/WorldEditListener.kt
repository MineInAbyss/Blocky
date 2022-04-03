package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.components.BlockType
import com.mineinabyss.blocky.components.BlockyBlock
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.geary.papermc.toPrefabKey
import com.mineinabyss.idofront.util.toMCKey
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent


class WorldEditListener : Listener {

    @EventHandler
    fun PlayerCommandPreprocessEvent.commandStuff() {
        if (!message.startsWith("//")) return
        //if (!isFAWELoaded) return
        val args: List<String> = message.split(" ")
        val blockyID = args.firstOrNull { it.contains("mineinabyss:") }?.toMCKey()?.toPrefabKey() ?: return
        val block = blockyID.toEntity()?.get<BlockyBlock>() ?: return
        val info = blockyID.toEntity()?.get<BlockyInfo>() ?: return

        val blockData = when (block.blockType) {
            BlockType.CUBE -> String.format(
                "%s[instrument=%s,note=%s,powered=%s]",
                Material.NOTE_BLOCK.toString().lowercase(),
                getInstrument(block.blockId / 25),
                block.blockId,
                true
            )
            else -> getDecorationData(block.blockId)
        }
        message = message.replace(blockyID.toString(), blockData, true)
    }
}

private fun getInstrument(id: Int): String {
    when (id) {
        0 -> return "basedrum"
        1 -> return "hat"
        2 -> return "snare"
        3 -> return "harp"
        4 -> return "bass"
        5 -> return "flute"
        6 -> return "bell"
        7 -> return "guitar"
        8 -> return "chime"
        9 -> return "xylophone"
        10 -> return "iron_xylophone"
        11 -> return "cow_bell"
        12 -> return "didgeridoo"
        13 -> return "bit"
        14 -> return "banjo"
        15 -> return "pling"
        else -> return "hat"
    }
}

private fun getDecorationData(blockId: Int): String {
    val inAttachedRange = blockId in 33..64
    val inPoweredRange = blockId in 17..32 || blockId in 49..64
    val northRange = 2..64
    val southRange = 5..64
    val eastRange = 3..64
    val westRange = 9..64
    var north = false
    var east = false
    var south = false
    var west = false

    if (blockId in northRange step 2) north = true

    for (i in westRange) {
        if (blockId !in i..i + 7) westRange step 8
        else west = true
    }

    for (i in southRange) {
        if (blockId !in i..i + 4) southRange step 4
        else south = true
    }

    for (i in eastRange) {
        if (blockId in i..i + 1) eastRange step 2
        else east = true
    }
    return "trip_wire[north=$north,east=$east,south=$south,west=$west,attached=$inAttachedRange,disarmed=true,powered=$inPoweredRange]"
}
