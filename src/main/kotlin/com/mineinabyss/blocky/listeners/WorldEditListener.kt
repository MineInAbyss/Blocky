package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.BlockyTypeQuery
import com.mineinabyss.blocky.components.BlockType
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.blocky.components.BlockyType
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
        val type = blockyID.toEntity()?.get<BlockyType>()?.blockType ?: return
        val info = blockyID.toEntity()?.get<BlockyInfo>() ?: return

        val cmd = when (type) {
            BlockType.CUBE -> String.format(
                "%s[instrument=%s,note=%s,powered=%s]",
                Material.NOTE_BLOCK.toString().lowercase(),
                getInstrument(info.modelId.toInt() / 25),
                info.modelId.toInt(),
                true
            )
            BlockType.WALL -> getDecorationData(info.modelId.toInt())
            BlockType.GROUND -> getDecorationData(info.modelId.toInt())
            else -> return
        }
        message = message.replace(blockyID.toString(), cmd, true)
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
    val blockyType = BlockyTypeQuery.firstOrNull {
        it.entity.get<BlockyInfo>()?.modelId?.toInt() == blockId
    }?.entity?.get<BlockyType>() ?: return ""

    when (blockyType.blockType) {
        BlockType.GROUND -> {
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
        BlockType.WALL -> {
            val inUpRange = blockId in 17..32
            val inNorthRange = blockId in 2..32 step 2
            val southRange = 5..32
            val eastRange = 3..32
            val westRange = 9..32
            var east = false
            var south = false
            var west = false

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
            return "glow_lichen[up=$inUpRange,north=$inNorthRange,east=$east,south=$south,west=$west,waterlogged=true]"
        }
        else -> return ""
    }
}
