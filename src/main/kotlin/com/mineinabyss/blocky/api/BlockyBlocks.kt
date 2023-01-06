package com.mineinabyss.blocky.api

import com.jeff_media.morepersistentdatatypes.DataType
import com.mineinabyss.blocky.api.events.block.BlockyBlockPlaceEvent
import com.mineinabyss.blocky.blockyConfig
import com.mineinabyss.blocky.components.core.BlockyBlock
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.events.call
import com.mineinabyss.looty.tracking.toGearyFromUUIDOrNull
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object BlockyBlocks {

    //TODO This might not work due to the way PlayerInstancedItems work? test it
    val ItemStack.isBlockyBlock get() = this.toGearyFromUUIDOrNull()?.has<BlockyBlock>() == true
    val PrefabKey.isBlockyBlock get() = this.toEntityOrNull()?.has<BlockyBlock>() == true
    val Location.isBlockyBlock get() = this.block.gearyEntity?.has<BlockyBlock>() == true
    val Block.isBlockyBlock get() = this.gearyEntity?.has<BlockyBlock>() == true

    val ItemStack.isBlockyNoteBlock get() = this.toGearyFromUUIDOrNull()?.get<BlockyBlock>()?.blockType == BlockyBlock.BlockType.NOTEBLOCK
    val PrefabKey.isBlockyNoteBlock get() = this.toEntityOrNull()?.get<BlockyBlock>()?.blockType == BlockyBlock.BlockType.NOTEBLOCK
    val Location.isBlockyNoteBlock get() = this.block.gearyEntity?.get<BlockyBlock>()?.blockType == BlockyBlock.BlockType.NOTEBLOCK
    val Block.isBlockyNoteBlock get() = this.gearyEntity?.get<BlockyBlock>()?.blockType == BlockyBlock.BlockType.NOTEBLOCK

    val ItemStack.isBlockyWire get() = this.toGearyFromUUIDOrNull()?.get<BlockyBlock>()?.blockType == BlockyBlock.BlockType.WIRE
    val PrefabKey.isBlockyWire get() = this.toEntityOrNull()?.get<BlockyBlock>()?.blockType == BlockyBlock.BlockType.WIRE
    val Location.isBlockyWire get() = this.block.gearyEntity?.get<BlockyBlock>()?.blockType == BlockyBlock.BlockType.WIRE
    val Block.isBlockyWire get() = this.gearyEntity?.get<BlockyBlock>()?.blockType == BlockyBlock.BlockType.WIRE

    val ItemStack.isBlockyCaveVine get() = this.toGearyFromUUIDOrNull()?.get<BlockyBlock>()?.blockType == BlockyBlock.BlockType.CAVEVINE
    val PrefabKey.isBlockyCaveVine get() = this.toEntityOrNull()?.get<BlockyBlock>()?.blockType == BlockyBlock.BlockType.CAVEVINE
    val Location.isBlockyCaveVine get() = this.block.gearyEntity?.get<BlockyBlock>()?.blockType == BlockyBlock.BlockType.CAVEVINE
    val Block.isBlockyCaveVine get() = this.gearyEntity?.get<BlockyBlock>()?.blockType == BlockyBlock.BlockType.CAVEVINE

    val ItemStack.blockyBlock get() = this.toGearyFromUUIDOrNull()?.get<BlockyBlock>()
    val PrefabKey.blockyBlock get() = this.toEntityOrNull()?.get<BlockyBlock>()
    val Location.blockyBlock get() = this.block.gearyEntity?.get<BlockyBlock>()
    val Block.blockyBlock get() = this.gearyEntity?.get<BlockyBlock>()

    private fun placeBlockyBlock(location: Location, prefabKey: PrefabKey): Boolean {
        val block = location.block
        val gearyEntity = prefabKey.toEntityOrNull() ?: return false
        val blockyBlock = gearyEntity.get<BlockyBlock>() ?: return false

        val blockyEvent = BlockyBlockPlaceEvent(block, null)
        if (blockyEvent.isCancelled) return false
        blockyEvent.call()

        block.blockData =
                when {
                    block.isBlockyBlock -> return false
                    block.isBlockyNoteBlock -> gearyEntity.getBlockyNoteBlock()
                    block.isBlockyWire -> blockyBlock.getBlockyTripWire()
                    block.isBlockyCaveVine -> blockyBlock.getBlockyCaveVine()
                    //block.isFakeWaxedCopper -> blockyBlock.get
                    else -> return false
                }
        //TODO Actually place the block with its mechanics
        if (!blockyConfig.noteBlocks.restoreFunctionality && block.isVanillaNoteBlock)
            block.customBlockData.set(NOTE_KEY, DataType.INTEGER, 0)
        return true
    }

    fun Location.removeBlockyBlock(player: Player? = null): Boolean {
        blockyBlock ?: return false
        this.block.attemptBreakBlockyBlock(player)
        return true
    }
}
