package com.mineinabyss.blocky

import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.accessors.building.get
import com.mineinabyss.geary.ecs.query.Query
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.looty.ecs.components.LootyType

object BlockyTypeQuery : Query() {
    init {
        has<LootyType>()
        has<Prefab>()
        has<BlockyInfo>()
    }

    val TargetScope.key by get<PrefabKey>()
}