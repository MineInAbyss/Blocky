package com.mineinabyss.blocky.api

import com.mineinabyss.blocky.components.core.BlockyBlock
import com.mineinabyss.blocky.components.features.BlockyLight
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
        val gearyEntity = prefabKey.toEntityOrNull() ?: return false
        val blockyBlock = gearyEntity.get<BlockyBlock>() ?: return false

        location.block.blockData = when (blockyBlock.blockType) {
            BlockyBlock.BlockType.NOTEBLOCK -> gearyEntity.getBlockyNoteBlock()
            BlockyBlock.BlockType.WIRE -> blockyBlock.getBlockyTripWire()
            BlockyBlock.BlockType.CAVEVINE -> blockyBlock.getBlockyCaveVine()
            else -> return false
        }

        /*if (!blocky.config.noteBlocks.restoreFunctionality && block.isVanillaNoteBlock)
            block.persistentDataContainer.encode(VanillaNoteBlock(0))*/

        if (gearyEntity.has<BlockyLight>())
            handleLight.createBlockLight(location, gearyEntity.get<BlockyLight>()!!.lightLevel)
        return true
    }

    fun removeBlockyBlock(location: Location): Boolean {
        location.blockyBlock ?: return false
        return attemptBreakBlockyBlock(location.block)
    }
}
