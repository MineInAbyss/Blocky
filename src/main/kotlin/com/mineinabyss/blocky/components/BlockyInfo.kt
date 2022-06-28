@file:UseSerializers(IntRangeSerializer::class)

package com.mineinabyss.blocky.components

import com.mineinabyss.idofront.serialization.DurationSerializer
import com.mineinabyss.idofront.serialization.IntRangeSerializer
import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.time.Duration

@Serializable
@SerialName("blocky:info")
data class BlockyInfo (
    val requiredTools: List<SerializableItemStack> = listOf(),
    val isUnbreakable: Boolean = false,
    val blockBreakTime: @Serializable(with = DurationSerializer::class) Duration,
    val blockDrop: List<BlockyDrops> = listOf(),
)
