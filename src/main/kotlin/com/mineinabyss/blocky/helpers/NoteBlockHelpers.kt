package com.mineinabyss.blocky.helpers

import com.jeff_media.customblockdata.CustomBlockData
import com.mineinabyss.blocky.api.BlockyBlocks.blockyBlock
import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.VanillaNoteBlock
import com.mineinabyss.blocky.components.features.BlockyBreaking
import com.mineinabyss.blocky.components.features.blocks.BlockyDirectional
import com.mineinabyss.blocky.components.features.blocks.BlockyInstrument
import com.mineinabyss.blocky.components.features.mining.ToolType
import com.mineinabyss.blocky.helpers.GenericHelpers.toBlockCenterLocation
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.gearyBlocks
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.toGearyOrNull
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.location.up
import com.mineinabyss.idofront.serialization.SerializableItemStack
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Skull
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.craftbukkit.block.CraftBlockState
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.jvm.optionals.getOrNull
import kotlin.math.pow

/**
 * Gets the blockdata of a given Blocky-block from a GearyEntity
 * Note: For directional Blocky-blocks, use [getBlockyNoteBlock(org.bukkit.block.BlockFace, org.bukkit.entity.Player)] instead
 * This will just use the parent-block of the BlockyDirectional component, which is not what you want
 */
fun PrefabKey.blockyNoteBlock(): BlockData {
    val blockID =
        (this.toEntityOrNull()?.get<BlockyDirectional>()?.parentBlock?.blockyBlock ?: blockyBlock)?.blockId ?: 0
    return gearyBlocks.block2Prefab.blockMap[SetBlock.BlockType.NOTEBLOCK]!![blockID]
}

fun GearyEntity.blockyNoteBlock(face: BlockFace = BlockFace.NORTH, player: Player? = null): BlockData {
    val directional = GenericHelpers.directionalId(this, face, player)
    return gearyBlocks.block2Prefab.blockMap[SetBlock.BlockType.NOTEBLOCK]!![directional]
}

// If the blockmap doesn't contain data, it means it's a vanilla note block
val Block.isVanillaNoteBlock get() = blockData is NoteBlock && blockData !in gearyBlocks.block2Prefab
val BlockData.isVanillaNoteBlock get() = this is NoteBlock && this !in gearyBlocks.block2Prefab

val Block.vanillaNoteBlock get() = persistentDataContainer.decode<VanillaNoteBlock>()
    ?: VanillaNoteBlock().takeIf { isVanillaNoteBlock }?.apply { persistentDataContainer.encode(this) }

val Block.isBlockyNoteBlock get() = blockData is NoteBlock && blockData in gearyBlocks.block2Prefab
val BlockData.isBlockyNoteBlock get() = this is NoteBlock && this in gearyBlocks.block2Prefab


object NoteBlockHelpers {

    val vanillaBreakingComponent = BlockyBreaking(
        hardness = 0.8,
        modifiers = BlockyBreaking.BlockyModifiers(
            heldTypes = setOf(BlockyBreaking.BlockyModifiers.BlockyToolModifier(toolType = ToolType.AXE, value = 0.3)),
            heldItems = setOf(
                BlockyBreaking.BlockyModifiers.BlockySerializableItemModifier(
                    item = SerializableItemStack(type = Material.NETHERITE_AXE),
                    value = 9.0
                ),
                BlockyBreaking.BlockyModifiers.BlockySerializableItemModifier(
                    item = SerializableItemStack(type = Material.DIAMOND_AXE),
                    value = 8.0
                ),
                BlockyBreaking.BlockyModifiers.BlockySerializableItemModifier(
                    item = SerializableItemStack(type = Material.GOLDEN_AXE),
                    value = 12.0
                ),
                BlockyBreaking.BlockyModifiers.BlockySerializableItemModifier(
                    item = SerializableItemStack(type = Material.IRON_AXE),
                    value = 6.0
                ),
                BlockyBreaking.BlockyModifiers.BlockySerializableItemModifier(
                    item = SerializableItemStack(type = Material.STONE_AXE),
                    value = 4.0
                ),
                BlockyBreaking.BlockyModifiers.BlockySerializableItemModifier(
                    item = SerializableItemStack(type = Material.WOODEN_AXE),
                    value = 2.0
                )
            )
        )
    )
}
