package com.mineinabyss.blocky

import com.mineinabyss.blocky.BlockyTypeQuery.key
import com.mineinabyss.blocky.components.*
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.geary.systems.accessors.get
import com.mineinabyss.geary.systems.accessors.has
import com.mineinabyss.geary.systems.query.GearyQuery
import com.mineinabyss.looty.ecs.components.LootyType

object BlockyTypeQuery : GearyQuery() {

    val TargetScope.key by get<PrefabKey>()
    val TargetScope.isBlocky by family {
        has<LootyType>()
        has<Prefab>()
        has<BlockyInfo>()
    }
}

val blockyQuery = BlockyTypeQuery.filter { it.entity.hasBlockyInfo }.map { it.key.toString() }
val blockyQueryBlocks =
    BlockyTypeQuery.filter {
        it.entity.isBlockyBlock && it.entity.blockyBlock!!.blockType != BlockType.GROUND
    }.map { it.key.toString() }