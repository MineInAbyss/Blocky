@file:UseSerializers(IntRangeSerializer::class)

package com.mineinabyss.blocky.components

import com.mineinabyss.blocky.helpers.getPrefabFromBlock
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.idofront.serialization.IntRangeSerializer
import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.bukkit.block.Block

@Serializable
@SerialName("blocky:info")
data class BlockyInfo (
    val requiredTools: List<SerializableItemStack> = listOf(),
    val isUnbreakable: Boolean = false,
    val blockBreakTime: Int,
    val blockDrop: List<BlockyDrops> = listOf(),
)

val GearyEntity.blockyInfo get() = get<BlockyInfo>()
val GearyEntity.hasBlockyInfo get() = blockyInfo != null

val Block.blockyInfo get() = getPrefabFromBlock()?.toEntity()?.get<BlockyInfo>()
val Block.hasBlockyInfo get() = blockyInfo != null