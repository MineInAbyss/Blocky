package com.mineinabyss.blocky.systems

import com.mineinabyss.blocky.api.BlockyFurnitures.isModelEngineFurniture
import com.mineinabyss.blocky.components.core.BlockyBlock
import com.mineinabyss.blocky.components.core.BlockyInfo
import com.mineinabyss.blocky.components.core.BlockyModelEngine
import com.mineinabyss.blocky.systems.BlockyBlockQuery.prefabKey
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.geary.systems.query.GearyQuery

//TODO See if this can be done without requiring BlockyInfo, but block || furniture
object BlockyQuery : GearyQuery() {
    val TargetScope.prefabKey by get<PrefabKey>()
    val TargetScope.isPrefab by family {
        has<Prefab>()
        has<BlockyInfo>()
    }
}

object BlockyBlockQuery : GearyQuery() {

    val TargetScope.prefabKey by get<PrefabKey>()
    val TargetScope.type by get<BlockyBlock>()
    val TargetScope.isPrefab by family { has<Prefab>() }
}

object BlockyFurnitureQuery : GearyQuery() {
    val TargetScope.key by get<PrefabKey>()
    val TargetScope.type by get<BlockyModelEngine>()
    val TargetScope.modelEngine by family {
        has<Prefab>()
    }
}

val blockyModelEngineQuery =
    BlockyFurnitureQuery.filter { it.entity.isModelEngineFurniture }.map { it.prefabKey.toString() }
