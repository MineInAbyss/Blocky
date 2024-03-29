package com.mineinabyss.blocky.components.features.mining

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
* Lets you define a component that affects the mining-speed of custom blocks
 * @param breakSpeedModifier The speed at which this modifies the default break speed of the block
 * @param toolTypes The types of tool this item is registered under
*/
@Serializable
@SerialName("blocky:mining")
data class BlockyMining(val toolTypes: Set<ToolType> = setOf(ToolType.ANY))

enum class ToolType {
    PICKAXE, AXE, SHOVEL, HOE, SWORD, SHEARS, ANY
}
