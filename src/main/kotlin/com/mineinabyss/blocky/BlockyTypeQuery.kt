package com.mineinabyss.blocky

import com.mineinabyss.blocky.BlockyTypeQuery.key
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.blocky.components.hasBlockyBiome
import com.mineinabyss.blocky.components.hasBlockyInfo
import com.mineinabyss.blocky.components.isBlockyBlock
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
val blockyBiomeQuery = BlockyTypeQuery.filter { it.entity.isBlockyBlock && it.entity.hasBlockyBiome }.map { it.key }
