package com.mineinabyss.blocky.components

import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.blocky.helpers.getPrefabFromBlock
import com.mineinabyss.geary.datatypes.GearyEntity
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.NamespacedKey
import org.bukkit.block.Block

@Serializable
@SerialName("blocky:vanilla_noteblock")
class VanillaNoteBlock(
    val key: @Contextual NamespacedKey = NamespacedKey(blockyPlugin, "vanilla_noteblock")
)

val Block.getVanillaNoteBlock get() = getPrefabFromBlock()?.toEntity()?.get<VanillaNoteBlock>()
val Block.isVanillaNoteBlock get() = getVanillaNoteBlock != null

val GearyEntity.getVanillaNoteBlock get() = get<VanillaNoteBlock>()
val GearyEntity.isVanillaNoteBlock get() = getVanillaNoteBlock != null