@file:UseSerializers(IntRangeSerializer::class)

package com.mineinabyss.blocky.components.core

import com.mineinabyss.idofront.serialization.IntRangeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
@SerialName("blocky:info")
data class BlockyInfo(
    val blockModel: String? = null,
    val isUnbreakable: Boolean = false
)
