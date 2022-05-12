package com.mineinabyss.blocky.components

import com.mineinabyss.blocky.helpers.getPrefabFromBlock
import com.mineinabyss.geary.datatypes.GearyEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.block.Block

@Serializable
@SerialName("blocky:block")
data class BlockyBlock (
    val blockType: BlockType,
    val blockId: Int
)

val GearyEntity.blockyBlock get() = get<BlockyBlock>()
val GearyEntity.isBlockyBlock get() = blockyBlock != null

val Block.blockyBlock get() = getPrefabFromBlock()?.toEntity()?.get<BlockyBlock>()
val Block.isBlockyBlock get() = blockyBlock != null

enum class BlockType {
    CUBE, GROUND, TRANSPARENT
}
