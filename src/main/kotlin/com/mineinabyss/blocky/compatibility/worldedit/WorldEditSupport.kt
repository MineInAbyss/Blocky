package com.mineinabyss.blocky.compatibility.worldedit

import com.mineinabyss.blocky.helpers.blockyNoteBlock
import com.mineinabyss.blocky.helpers.blockyTripWire
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.prefabs.PrefabKey
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extension.input.ParserContext
import com.sk89q.worldedit.internal.registry.InputParser
import com.sk89q.worldedit.world.block.BaseBlock
import org.bukkit.Material
import org.bukkit.block.BlockFace

class WorldEditSupport {
    // TODO Make this skip checking all non-namespace:blocky blocks, but make namespace not hardcoded to mineinabyss
    class BlockyInputParser : InputParser<BaseBlock>(WorldEdit.getInstance()) {
        override fun parseFromInput(input: String, context: ParserContext): BaseBlock? {
            // To prevent the parser from parsing note_block[direction=up] as a blocky block
            if (input == "minecraft:note_block" || input == "note_block") {
                return BukkitAdapter.adapt(Material.NOTE_BLOCK.createBlockData()).toBaseBlock()
            } else if (input == "minecraft:tripwire" || input == "tripwire") {
                return BukkitAdapter.adapt(Material.TRIPWIRE.createBlockData()).toBaseBlock()
            }

            val gearyEntity = PrefabKey.ofOrNull(input.replaceDirectionText())?.toEntityOrNull() ?: return null
            val type = gearyEntity.get<SetBlock>()?.blockType ?: return null
            val blockData =
                if (type == SetBlock.BlockType.WIRE) gearyEntity.get<SetBlock>()!!.blockyTripWire()
                else gearyEntity.blockyNoteBlock(
                    BlockFace.valueOf(
                        input.substringAfter("[direction=", "up]").substringBefore("]", "UP").uppercase()
                    ), null
                )

            return BukkitAdapter.adapt(blockData).toBaseBlock()
        }

        private fun String.replaceDirectionText(): String {
            return this.replace("[direction=up]", "")
                .replace("[direction=down]", "")
                .replace("[direction=north]", "")
                .replace("[direction=south]", "")
                .replace("[direction=west]", "")
                .replace("[direction=east]", "")
        }
    }
}


