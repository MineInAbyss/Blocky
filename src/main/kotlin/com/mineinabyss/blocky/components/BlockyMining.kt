package com.mineinabyss.blocky.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
* Lets you define a component that affects the mining-speed of custom blocks
 * @param breakSpeedModifier The speed at which this modifies the default break speed of the block
 * @param toolTypes The types of tools that
*/
@Serializable
@SerialName("blocky:mining")
data class BlockyMining(
    val breakSpeedModifier: Double = 1.0,
    val toolTypes: Set<ToolType> = setOf(ToolType.ANY)
)

enum class ToolType {
    PICKAXE, AXE, SHOVEL, HOE, SWORD, SHEARS, ANY
}
