package com.mineinabyss.blocky.components

import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Sound

@Serializable
@SerialName("blocky:info")
data class BlockyInfo (
    val requiredTools: List<SerializableItemStack> = listOf(),
    val canBeBroken: Boolean = true,
    val blockBreakTime: Int,
    val affectedByPiston: Boolean = true,
    val placeSound: Sound,
    val canBeDebugged: Boolean = true
)