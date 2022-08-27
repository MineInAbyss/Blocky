package com.mineinabyss.blocky.systems

import com.mineinabyss.blocky.components.BlockyBlock
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.blocky.components.BlockyModelEngine
import com.mineinabyss.blocky.systems.BlockyTypeQuery.key
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.accessors.TargetScope
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

object BlockyModelQuery : GearyQuery() {

    val TargetScope.key by get<PrefabKey>()
    val TargetScope.blockyBlock by get<BlockyBlock>()
    val TargetScope.isBlocky by family {
        has<BlockyBlock>()
    }
}

object BlockyModelEngineQuery : GearyQuery() {
    val TargetScope.key by get<PrefabKey>()
    val TargetScope.modelEngine by family {
        has<LootyType>()
        has<Prefab>()
        has<BlockyModelEngine>()
    }
}

val blockyModelQuery = BlockyModelQuery.filter { it.entity.has<BlockyBlock>() }.map { it.entity.get<BlockyBlock>() }
val blockyQuery = BlockyTypeQuery.filter { it.entity.has<BlockyInfo>() }.map { it.key.toString() }
val blockyModelEngineQuery = BlockyModelEngineQuery.map { it.key.toString() }
