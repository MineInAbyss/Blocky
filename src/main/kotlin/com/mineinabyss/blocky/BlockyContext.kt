package com.mineinabyss.blocky

import com.mineinabyss.blocky.systems.BlockyBlockQuery
import com.mineinabyss.blocky.systems.BlockyFurnitureQuery
import com.mineinabyss.blocky.systems.BlockyQuery
import com.mineinabyss.geary.systems.query.CachedQueryRunner
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.messaging.ComponentLogger

val blocky by DI.observe<BlockyContext>()
interface BlockyContext {
    val plugin: BlockyPlugin
    val logger: ComponentLogger
    val config: BlockyConfig
    val prefabQuery: CachedQueryRunner<BlockyQuery>
    val blockQuery: CachedQueryRunner<BlockyBlockQuery>
    val furnitureQuery: CachedQueryRunner<BlockyFurnitureQuery>
}
