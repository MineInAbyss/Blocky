@file:OptIn(UnsafeAccessors::class, UnsafeAccessors::class, UnsafeAccessors::class)

package com.mineinabyss.blocky.systems

import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.features.blocks.BlockyDirectional
import com.mineinabyss.blocky.components.features.furniture.BlockyModelEngine
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.query.GearyQuery

class BlockyQuery : GearyQuery() {
    val prefabKey by get<PrefabKey>()
    val block by get<SetBlock>().orNull()
    val directional by get<BlockyDirectional>().orNull()
    val modelEngine by get<BlockyModelEngine>().orNull()

    override fun ensure() = this {
        has<Prefab>()
    }
}

class BlockyBlockQuery : GearyQuery() {
    val prefabKey by get<PrefabKey>()
    val block by get<SetBlock>()
    val directional by get<BlockyDirectional>().orNull()

    override fun ensure() = this {
        has<Prefab>()
        has<SetBlock>()
        not {
            has<BlockyFurniture>()
            has<BlockyModelEngine>()
        }
    }
}

class BlockyFurnitureQuery : GearyQuery() {
    val key by get<PrefabKey>()
    val modelEngine by get<BlockyModelEngine>().orNull()

    override fun ensure() = this {
        has<Prefab>()
        or {
            has<BlockyFurniture>()
            has<BlockyModelEngine>()
        }
        not {
            has<SetBlock>()
        }
    }
}

val allBlockyPrefabs
    get() = blocky.prefabQuery
        .map { BlockyPrefabs.from(it.prefabKey, it.block, it.directional, it.modelEngine) }
        .filterNotNull()
        .sortedBy { it.prefabKey.key }
val blockPrefabs
    get() = blocky.blockQuery
        .map { BlockyPrefabs.Block.from(it.prefabKey, it.block, it.directional) }
        .filterNotNull()
        .sortedBy { it.prefabKey.key }

val plantPrefabs
    get() = blocky.blockQuery
        .map { BlockyPrefabs.Plant.from(it.prefabKey, it.block, it.directional) }
        .filterNotNull()
        .sortedBy { it.prefabKey.key }

val furniturePrefabs
    get() = blocky
        .furnitureQuery
        .map { BlockyPrefabs.Furniture.from(it.key, it.modelEngine) }
        .sortedBy { it.prefabKey.key }

val megFurniturePrefabs = furniturePrefabs.filter { it.isModelEngine }
