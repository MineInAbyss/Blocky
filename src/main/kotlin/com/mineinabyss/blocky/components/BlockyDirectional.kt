package com.mineinabyss.blocky.components

import com.mineinabyss.blocky.helpers.getPrefabFromBlock
import com.mineinabyss.geary.datatypes.GearyEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.block.Block

@Serializable
@SerialName("blocky:directional")
class BlockyDirectional(
    val yBlockId: Int = 0,
    val xBlockId: Int = 0,
    val zBlockId: Int = 0,
) {
    fun hasYVariant() : Boolean { return yBlockId > 0 }
    fun hasXVariant() : Boolean { return xBlockId > 0 }
    fun hasZVariant() : Boolean { return zBlockId > 0 }
}

val GearyEntity.directional get() = get<BlockyDirectional>()
val GearyEntity.isDirectional get() = has<BlockyDirectional>()

val Block.directional get() = getPrefabFromBlock()?.toEntity()?.get<BlockyDirectional>()
val Block.isDirectional get() = directional != null
