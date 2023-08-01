package com.mineinabyss.blocky.api

import com.mineinabyss.blocky.components.features.BlockyLight
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.toGearyOrNull
import com.mineinabyss.geary.prefabs.PrefabKey
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

object BlockyBlocks {

    val ItemStack.isBlockyBlock get() = this.decode<SetBlock>() != null
    val String.isBlockyBlock get() = PrefabKey.ofOrNull(this)?.toEntityOrNull()?.has<SetBlock>() == true
    val PrefabKey.isBlockyBlock get() = this.toEntityOrNull()?.has<SetBlock>() == true
    val Location.isBlockyBlock get() = block.toGearyOrNull()?.has<SetBlock>() == true
    val Block.isBlockyBlock get() = this.toGearyOrNull()?.has<SetBlock>() == true

    val String.isBlockyNoteBlock get() = PrefabKey.ofOrNull(this)?.toEntityOrNull()?.get<SetBlock>()?.blockType == SetBlock.BlockType.NOTEBLOCK
    val PrefabKey.isBlockyNoteBlock get() = this.toEntityOrNull()?.get<SetBlock>()?.blockType == SetBlock.BlockType.NOTEBLOCK
    val ItemStack.isBlockyNoteBlock get() = this.decode<SetBlock>()?.blockType == SetBlock.BlockType.NOTEBLOCK
    val Location.isBlockyNoteBlock get() = this.block.toGearyOrNull()?.get<SetBlock>()?.blockType == SetBlock.BlockType.NOTEBLOCK
    val Block.isBlockyNoteBlock get() = this.toGearyOrNull()?.get<SetBlock>()?.blockType == SetBlock.BlockType.NOTEBLOCK

    val String.isBlockyWire get() = PrefabKey.ofOrNull(this)?.toEntityOrNull()?.get<SetBlock>()?.blockType == SetBlock.BlockType.WIRE
    val PrefabKey.isBlockyWire get() = this.toEntityOrNull()?.get<SetBlock>()?.blockType == SetBlock.BlockType.WIRE
    val ItemStack.isBlockyWire get() = this.decode<SetBlock>()?.blockType == SetBlock.BlockType.WIRE
    val Location.isBlockyWire get() = this.block.toGearyOrNull()?.get<SetBlock>()?.blockType == SetBlock.BlockType.WIRE
    val Block.isBlockyWire get() = this.toGearyOrNull()?.get<SetBlock>()?.blockType == SetBlock.BlockType.WIRE

    val String.isBlockyCaveVine get() = PrefabKey.ofOrNull(this)?.toEntityOrNull()?.get<SetBlock>()?.blockType == SetBlock.BlockType.CAVEVINE
    val PrefabKey.isBlockyCaveVine get() = this.toEntityOrNull()?.get<SetBlock>()?.blockType == SetBlock.BlockType.CAVEVINE
    val ItemStack.isBlockyCaveVine get() = this.decode<SetBlock>()?.blockType == SetBlock.BlockType.CAVEVINE
    val Location.isBlockyCaveVine get() = this.block.toGearyOrNull()?.get<SetBlock>()?.blockType == SetBlock.BlockType.CAVEVINE
    val Block.isBlockyCaveVine get() = this.toGearyOrNull()?.get<SetBlock>()?.blockType == SetBlock.BlockType.CAVEVINE

    val PrefabKey.blockyBlock get() = this.toEntityOrNull()?.get<SetBlock>()
    val ItemStack.blockyBlock get() = this.decode<SetBlock>()
    val Location.blockyBlock get() = this.block.toGearyOrNull()?.get<SetBlock>()
    val Block.blockyBlock get() = this.toGearyOrNull()?.get<SetBlock>()

    fun placeBlockyBlock(location: Location, prefabKey: PrefabKey): Boolean {
        val gearyEntity = prefabKey.toEntityOrNull() ?: return false
        val blockyBlock = gearyEntity.get<SetBlock>() ?: return false

        location.block.blockData = when (blockyBlock.blockType) {
            SetBlock.BlockType.NOTEBLOCK -> gearyEntity.getBlockyNoteBlock()
            SetBlock.BlockType.WIRE -> blockyBlock.getBlockyTripWire()
            SetBlock.BlockType.CAVEVINE -> CaveVineHelpers.getBlockyCaveVine(blockyBlock)
            else -> return false
        }

        /*if (!blocky.config.noteBlocks.restoreFunctionality && block.isVanillaNoteBlock)
            block.persistentDataContainer.encode(VanillaNoteBlock(0))*/

        if (gearyEntity.has<BlockyLight>())
            BlockLight.createBlockLight(location, gearyEntity.get<BlockyLight>()!!.lightLevel)
        return true
    }

    fun removeBlockyBlock(location: Location): Boolean {
        location.blockyBlock ?: return false
        return attemptBreakBlockyBlock(location.block)
    }
}
