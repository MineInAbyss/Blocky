package com.mineinabyss.blocky.api

import com.mineinabyss.blocky.helpers.decode
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.toEntityOrNull
import com.mineinabyss.geary.papermc.tracking.blocks.BlockTracking
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.entities.helpers.withGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.inventory.ItemStack

object BlockyBlocks {
    val Geary.gearyBlocks get() = getAddon(BlockTracking)

    context(Geary) val ItemStack.isBlockyBlock get() = this.decode<SetBlock>() != null
    context(Geary) val String.isBlockyBlock get() = PrefabKey.ofOrNull(this)?.toEntityOrNull()?.has<SetBlock>() == true
    context(Geary) val PrefabKey.isBlockyBlock get() = this.toEntityOrNull()?.has<SetBlock>() == true
    context(Geary) val BlockData.isBlockyBlock get() = this.toGearyOrNull()?.has<SetBlock>() == true

    val Location.isBlockyBlock get() = block.toGearyOrNull()?.has<SetBlock>() == true
    val Block.isBlockyBlock get() = this.toGearyOrNull()?.has<SetBlock>() == true

    context(Geary) val String.isBlockyNoteBlock
        get() = PrefabKey.ofOrNull(this)?.toEntityOrNull()?.get<SetBlock>()?.blockType == SetBlock.BlockType.NOTEBLOCK
    context(Geary) val PrefabKey.isBlockyNoteBlock
        get() = this.toEntityOrNull()?.get<SetBlock>()?.blockType == SetBlock.BlockType.NOTEBLOCK
    context(Geary) val ItemStack.isBlockyNoteBlock get() = this.decode<SetBlock>()?.blockType == SetBlock.BlockType.NOTEBLOCK

    val Location.isBlockyNoteBlock
        get() = this.block.toGearyOrNull()?.get<SetBlock>()?.blockType == SetBlock.BlockType.NOTEBLOCK
    val Block.isBlockyNoteBlock get() = this.toGearyOrNull()?.get<SetBlock>()?.blockType == SetBlock.BlockType.NOTEBLOCK

    context(Geary) val String.isBlockyWire
        get() = PrefabKey.ofOrNull(this)?.toEntityOrNull()?.get<SetBlock>()?.blockType == SetBlock.BlockType.WIRE
    context(Geary) val PrefabKey.isBlockyWire
        get() = this.toEntityOrNull()?.get<SetBlock>()?.blockType == SetBlock.BlockType.WIRE
    context(Geary) val ItemStack.isBlockyWire get() = this.decode<SetBlock>()?.blockType == SetBlock.BlockType.WIRE
    val Location.isBlockyWire get() = this.block.toGearyOrNull()?.get<SetBlock>()?.blockType == SetBlock.BlockType.WIRE
    val Block.isBlockyWire get() = this.toGearyOrNull()?.get<SetBlock>()?.blockType == SetBlock.BlockType.WIRE

    context(Geary) val String.isBlockyCaveVine
        get() = PrefabKey.ofOrNull(this)?.toEntityOrNull()?.get<SetBlock>()?.blockType == SetBlock.BlockType.CAVEVINE
    context(Geary) val PrefabKey.isBlockyCaveVine
        get() = this.toEntityOrNull()?.get<SetBlock>()?.blockType == SetBlock.BlockType.CAVEVINE
    context(Geary) val ItemStack.isBlockyCaveVine get() = this.decode<SetBlock>()?.blockType == SetBlock.BlockType.CAVEVINE
    val Location.isBlockyCaveVine
        get() = this.block.toGearyOrNull()?.get<SetBlock>()?.blockType == SetBlock.BlockType.CAVEVINE
    val Block.isBlockyCaveVine get() = this.toGearyOrNull()?.get<SetBlock>()?.blockType == SetBlock.BlockType.CAVEVINE

    context(Geary) val PrefabKey.blockyBlock get() = this.toEntityOrNull()?.get<SetBlock>()
    context(Geary) val ItemStack.blockyBlock get() = this.decode<SetBlock>()
    val Location.blockyBlock get() = this.block.toGearyOrNull()?.get<SetBlock>()
    val Block.blockyBlock get() = this.toGearyOrNull()?.get<SetBlock>()

    fun placeBlockyBlock(location: Location, prefabKey: PrefabKey): Boolean = location.withGeary {
        val gearyEntity = prefabKey.toEntityOrNull() ?: return false
        val blockyBlock = gearyEntity.get<SetBlock>() ?: return false
        val blockData = gearyBlocks.createBlockData(prefabKey) ?: return false

        return placeBlockyBlock(location, blockData)
    }

    internal fun placeBlockyBlock(location: Location, data: BlockData): Boolean = location.withGeary {
        if (gearyBlocks.block2Prefab[data]?.toEntityOrNull()?.has<SetBlock>() != true) return false
        location.block.blockData = data

        //TODO Handle light via packets for blocks
        return true
    }
}
