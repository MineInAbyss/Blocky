package com.mineinabyss.blocky.api

import com.jeff_media.morepersistentdatatypes.DataType
import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.BlockyBlock
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.geary.prefabs.PrefabKey
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

object BlockyBlocks {
    val Block.gearyEntity get() = prefabKey?.toEntityOrNull()

    //TODO This might not work due to the way PlayerInstancedItems work? test it
    val ItemStack.isBlockyBlock get() = this.decode<BlockyBlock>() != null
    val PrefabKey.isBlockyBlock get() = this.toEntityOrNull()?.has<BlockyBlock>() == true
    val Location.isBlockyBlock get() = this.block.gearyEntity?.has<BlockyBlock>() == true
    val Block.isBlockyBlock get() = this.gearyEntity?.has<BlockyBlock>() == true

    val ItemStack.isBlockyNoteBlock get() = this.decode<BlockyBlock>()?.blockType == BlockyBlock.BlockType.NOTEBLOCK
    val PrefabKey.isBlockyNoteBlock
        get() = this.toEntityOrNull()?.get<BlockyBlock>()?.blockType == BlockyBlock.BlockType.NOTEBLOCK
    val Location.isBlockyNoteBlock get() = this.block.gearyEntity?.get<BlockyBlock>()?.blockType == BlockyBlock.BlockType.NOTEBLOCK
    val Block.isBlockyNoteBlock get() = this.gearyEntity?.get<BlockyBlock>()?.blockType == BlockyBlock.BlockType.NOTEBLOCK

    val ItemStack.isBlockyWire get() = this.decode<BlockyBlock>()?.blockType == BlockyBlock.BlockType.WIRE
    val PrefabKey.isBlockyWire
        get() = this.toEntityOrNull()?.get<BlockyBlock>()?.blockType == BlockyBlock.BlockType.WIRE
    val Location.isBlockyWire get() = this.block.gearyEntity?.get<BlockyBlock>()?.blockType == BlockyBlock.BlockType.WIRE
    val Block.isBlockyWire get() = this.gearyEntity?.get<BlockyBlock>()?.blockType == BlockyBlock.BlockType.WIRE

    val ItemStack.isBlockyCaveVine get() = this.decode<BlockyBlock>()?.blockType == BlockyBlock.BlockType.CAVEVINE
    val PrefabKey.isBlockyCaveVine
        get() = this.toEntityOrNull()?.get<BlockyBlock>()?.blockType == BlockyBlock.BlockType.CAVEVINE
    val Location.isBlockyCaveVine get() = this.block.gearyEntity?.get<BlockyBlock>()?.blockType == BlockyBlock.BlockType.CAVEVINE
    val Block.isBlockyCaveVine get() = this.gearyEntity?.get<BlockyBlock>()?.blockType == BlockyBlock.BlockType.CAVEVINE

    val ItemStack.blockyBlock get() = this.decode<BlockyBlock>()
    val PrefabKey.blockyBlock get() = this.toEntityOrNull()?.get<BlockyBlock>()
    val Location.blockyBlock get() = this.block.gearyEntity?.get<BlockyBlock>()
    val Block.blockyBlock get() = this.gearyEntity?.get<BlockyBlock>()

    fun placeBlockyBlock(location: Location, prefabKey: PrefabKey): Boolean {
        val block = location.block
        val gearyEntity = prefabKey.toEntityOrNull() ?: return false
        val blockyBlock = gearyEntity.get<BlockyBlock>() ?: return false

        block.blockData = when {
            block.isBlockyBlock -> return false
            block.isBlockyNoteBlock -> gearyEntity.getBlockyNoteBlock()
            block.isBlockyWire -> blockyBlock.getBlockyTripWire()
            block.isBlockyCaveVine -> blockyBlock.getBlockyCaveVine()
            //block.isFakeWaxedCopper -> blockyBlock.get
            else -> return false
        }
        //TODO Actually place the block with its mechanics
        if (!blocky.config.noteBlocks.restoreFunctionality && block.isVanillaNoteBlock)
            block.customBlockData.set(NOTE_KEY, DataType.INTEGER, 0)
        return true
    }

    fun removeBlockyBlock(location: Location): Boolean {
        location.blockyBlock ?: return false
        return attemptBreakBlockyBlock(location.block)
    }
}
