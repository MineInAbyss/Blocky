package com.mineinabyss.blocky.systems

import com.mineinabyss.blocky.components.BlockyBlock
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.blocky.components.BlockyModelEngine
import com.mineinabyss.blocky.systems.BlockyTypeQuery.prefabKey
import com.mineinabyss.blocky.systems.BlockyTypeQuery.type
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.geary.systems.query.GearyQuery
import com.mineinabyss.looty.ecs.components.LootyType

object BlockyTypeQuery : GearyQuery() {

    val TargetScope.prefabKey by get<PrefabKey>()
    val TargetScope.type by get<BlockyBlock>()
    val TargetScope.isPrefab by family { has<Prefab>() }
}

object BlockyModelEngineQuery : GearyQuery() {
    val TargetScope.key by get<PrefabKey>()
    val TargetScope.modelEngine by family {
        has<LootyType>()
        has<Prefab>()
        has<BlockyModelEngine>()
    }
}


val blockyModelQuery = BlockyTypeQuery.map { it.type }
val blockyQuery = BlockyTypeQuery.filter { it.entity.has<BlockyInfo>() }.map { it.prefabKey.toString() }
val blockyModelEngineQuery = BlockyModelEngineQuery.map { it.prefabKey.toString() }
