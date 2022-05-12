package com.mineinabyss.blocky.components

import com.mineinabyss.blocky.helpers.getPrefabFromBlock
import com.mineinabyss.geary.datatypes.GearyEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Sound
import org.bukkit.block.Block

@Serializable
@SerialName("blocky:sound")
class BlockySound (
    val placeSound: Sound,
    val breakSound: Sound
)

val GearyEntity.blockySound get() = get<BlockySound>()
val GearyEntity.hasBlockySound get() = has<BlockySound>()

val Block.blockySound get() = getPrefabFromBlock()?.toEntity()?.get<BlockySound>()
val Block.hasBlockySound get() = blockySound != null