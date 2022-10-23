package com.mineinabyss.blocky.compatibility

import com.mineinabyss.blocky.components.core.BlockType
import com.mineinabyss.blocky.components.core.BlockyBlock
import com.mineinabyss.blocky.helpers.getBlockyNoteBlock
import com.mineinabyss.blocky.helpers.getBlockyTransparent
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
            if (input.startsWith("minecraft:note_block") || input.startsWith("note_block")) {
                return BukkitAdapter.adapt(Bukkit.createBlockData(Material.NOTE_BLOCK)).toBaseBlock()
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
                    if (type == BlockType.NOTEBLOCK) gearyEntity.getBlockyNoteBlock(BlockFace.UP)
                    else gearyEntity.getBlockyTransparent(BlockFace.UP)
                }
                input.endsWith("[direction=north]") -> {
                    if (type == BlockType.NOTEBLOCK)
                        gearyEntity.getBlockyNoteBlock(BlockFace.NORTH)
                    else gearyEntity.getBlockyTransparent(BlockFace.NORTH)
                }
                input.endsWith("[direction=south]") -> {
                    if (type == BlockType.NOTEBLOCK)
                        gearyEntity.getBlockyNoteBlock(BlockFace.SOUTH)
                    else gearyEntity.getBlockyTransparent(BlockFace.SOUTH)
                }
                input.endsWith("[direction=west]") -> {
                    if (type == BlockType.NOTEBLOCK)
                        gearyEntity.getBlockyNoteBlock(BlockFace.WEST)
                    else gearyEntity.getBlockyTransparent(BlockFace.WEST)
                }
                input.endsWith("[direction=east]") -> {
                    if (type == BlockType.NOTEBLOCK)
                        gearyEntity.getBlockyNoteBlock(BlockFace.EAST)
                    else gearyEntity.getBlockyTransparent(BlockFace.EAST)
                }
                else -> {
                    if (type == BlockType.NOTEBLOCK)
                        gearyEntity.getBlockyNoteBlock(BlockFace.UP)
                    else gearyEntity.getBlockyTransparent(BlockFace.UP)
                }
            }

            /*context.selection.boundingBox.run {
                (pos1.blockX..pos2.blockX).forEach x@{ x ->
                    (pos1.blockY..pos2.blockY).forEach y@{ y ->
                        (pos1.blockZ..pos2.blockZ).forEach z@{ z ->
                            val block = BukkitAdapter.adapt(context.world.broadcastVal() ?: return@z).getBlockAt(x, y, z)
                            block.isBlockyBlock().broadcastVal() || return@z
                            if (gearyEntity.has<BlockyLight>().broadcastVal())
                                broadcast("Lighting block at ${block.location}")//
                                //handleLight.createBlockLight(block.location, gearyEntity.get<BlockyLight>()!!.lightLevel)
                        }
                    }
                }
            }*/

            return BukkitAdapter.adapt(blockData).toBaseBlock()
        }
    }
}


