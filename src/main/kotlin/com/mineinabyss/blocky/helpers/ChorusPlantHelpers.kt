package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.blockMap
import com.mineinabyss.blocky.components.blockyBlock
import com.mineinabyss.blocky.components.directional
import com.mineinabyss.blocky.components.isDirectional
import com.mineinabyss.geary.datatypes.GearyEntity
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.MultipleFacing

fun GearyEntity.getBlockyTransparent(face: BlockFace) : BlockData {
    val id = getDirectionalId(face)
    return blockMap.filter { it.key is MultipleFacing && it.key.material == Material.CHORUS_PLANT && it.value == id }.keys.first() as MultipleFacing
}
