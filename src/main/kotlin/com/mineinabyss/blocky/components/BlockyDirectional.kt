package com.mineinabyss.blocky.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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