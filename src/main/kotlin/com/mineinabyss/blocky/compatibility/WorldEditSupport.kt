package com.mineinabyss.blocky.compatibility

import com.mineinabyss.blocky.components.core.BlockType
import com.mineinabyss.blocky.components.core.BlockyBlock
import com.mineinabyss.blocky.helpers.getBlockyNoteBlock
import com.mineinabyss.blocky.helpers.getBlockyTripWire
import com.mineinabyss.geary.prefabs.PrefabKey
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extension.input.ParserContext
import com.sk89q.worldedit.internal.registry.InputParser
import com.sk89q.worldedit.world.block.BaseBlock
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockFace

class WorldEditSupport {
    // TODO Make this skip checking all non-namespace:blocky blocks, but make namespace not hardcoded to mineinabyss
    class BlockyInputParser : InputParser<BaseBlock>(WorldEdit.getInstance()) {
        override fun parseFromInput(input: String, context: ParserContext): BaseBlock? {
            // To prevent the parser from parsing note_block[direction=up] as a blocky block
            if (input == "minecraft:note_block" || input == "note_block") {
                return BukkitAdapter.adapt(Bukkit.createBlockData(Material.NOTE_BLOCK)).toBaseBlock()
            } else if (input == "minecraft:tripwire" || input == "tripwire") {
                return BukkitAdapter.adapt(Bukkit.createBlockData(Material.TRIPWIRE)).toBaseBlock()
            }

            val gearyEntity = PrefabKey.ofOrNull(input.replace("[direction=up]", "")
                .replace("[direction=down]", "")
                .replace("[direction=north]", "")
                .replace("[direction=south]", "")
                .replace("[direction=west]", "")
                .replace("[direction=east]", ""))?.toEntityOrNull() ?: return null
            val type = gearyEntity.get<BlockyBlock>()?.blockType ?: return null

            val blockData = when {
                type == BlockType.TRIPWIRE -> gearyEntity.get<BlockyBlock>()!!.getBlockyTripWire()
                input.endsWith("[direction=up]") -> {
                    gearyEntity.getBlockyNoteBlock(BlockFace.UP)
                }
                input.endsWith("[direction=north]") -> {
                    gearyEntity.getBlockyNoteBlock(BlockFace.NORTH)
                }
                input.endsWith("[direction=south]") -> {
                    gearyEntity.getBlockyNoteBlock(BlockFace.SOUTH)
                }
                input.endsWith("[direction=west]") -> {
                    gearyEntity.getBlockyNoteBlock(BlockFace.WEST)
                }
                input.endsWith("[direction=east]") -> {
                    gearyEntity.getBlockyNoteBlock(BlockFace.EAST)
                }
                else -> {
                    gearyEntity.getBlockyNoteBlock(BlockFace.UP)
                }
            }

            return BukkitAdapter.adapt(blockData).toBaseBlock()
        }
    }
}


