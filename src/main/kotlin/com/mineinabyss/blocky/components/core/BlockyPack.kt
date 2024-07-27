package com.mineinabyss.blocky.components.core

import com.mineinabyss.idofront.serialization.KeySerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.kyori.adventure.key.Key
import team.unnamed.creative.model.ModelTexture
import team.unnamed.creative.model.ModelTextures

@Serializable
@SerialName("blocky:pack")
data class BlockyPack(
    val model: @Serializable(KeySerializer::class) Key? = null,
    val parentModel: @Serializable(KeySerializer::class) Key? = null,
    val textures: Map<String, @Serializable(KeySerializer::class) Key>? = null,
) {
    @Transient val modelTextures: ModelTextures = ModelTextures.of(listOf(), null, textures?.map { it.key to ModelTexture.ofKey(it.value)!! }?.toMap() ?: mutableMapOf())
}