@file:UseSerializers(IntRangeSerializer::class)

package com.mineinabyss.blocky.components.core

import com.mineinabyss.blocky.components.features.BlockyDrops
import com.mineinabyss.blocky.components.features.mining.ToolType
import com.mineinabyss.idofront.serialization.DurationSerializer
import com.mineinabyss.idofront.serialization.IntRangeSerializer
import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Serializable
@SerialName("blocky:info")
data class BlockyInfo (
    val requiredTools: List<SerializableItemStack> = listOf(),
    val isUnbreakable: Boolean = false,
    val acceptedToolTypes: Set<ToolType> = setOf(ToolType.ANY),
    val onlyDropWithCorrectTool: Boolean = false,
    val blockBreakTime: @Serializable(with = DurationSerializer::class) Duration = 2.seconds,
    val blockDrop: List<BlockyDrops> = listOf(),
)
