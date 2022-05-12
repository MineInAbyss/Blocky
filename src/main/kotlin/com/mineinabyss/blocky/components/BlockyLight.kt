package com.mineinabyss.blocky.components

import com.mineinabyss.blocky.helpers.getPrefabFromBlock
import com.mineinabyss.geary.datatypes.GearyEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.block.Block

@Serializable
@SerialName("blocky:light")
data class BlockyLight (
    val lightLevel: Int = 15
)

val GearyEntity.blockyLight get() = get<BlockyLight>()
val GearyEntity.hasBlockyLight get() = has<BlockyLight>()

val Block.blockyLight get() = getPrefabFromBlock()?.toEntity()?.get<BlockyLight>()
val Block.hasBlockyLight get() = blockyLight != null