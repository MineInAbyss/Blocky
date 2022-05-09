package com.mineinabyss.blocky

import com.mineinabyss.blocky.components.BlockyInfo
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