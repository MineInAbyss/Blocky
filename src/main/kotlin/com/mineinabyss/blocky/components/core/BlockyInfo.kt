@file:UseSerializers(IntRangeSerializer::class)

package com.mineinabyss.blocky.components.core

import com.mineinabyss.idofront.serialization.IntRangeSerializer
import com.mineinabyss.idofront.serialization.KeySerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.kyori.adventure.key.Key

@Serializable
@SerialName("blocky:info")
data class BlockyInfo(
    val blockModel: @Serializable(KeySerializer::class) Key? = null,
    val blockTexture: @Serializable(KeySerializer::class) Key? = null,
    val blockTextures: Map<String, @Serializable(KeySerializer::class) Key>? = null,
    val isUnbreakable: Boolean = false
)
