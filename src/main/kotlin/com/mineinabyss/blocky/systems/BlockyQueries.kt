@file:OptIn(UnsafeAccessors::class, UnsafeAccessors::class, UnsafeAccessors::class)

package com.mineinabyss.blocky.systems

import com.mineinabyss.blocky.api.BlockyFurnitures.isModelEngineFurniture
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.features.blocks.BlockyDirectional
import com.mineinabyss.blocky.components.features.furniture.BlockyModelEngine
import com.mineinabyss.blocky.systems.BlockyBlockQuery.block
import com.mineinabyss.blocky.systems.BlockyBlockQuery.prefabKey
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.accessors.Pointer
import com.mineinabyss.geary.systems.query.GearyQuery

object BlockyQuery : GearyQuery() {
    val Pointer.prefabKey by get<PrefabKey>()
    val Pointer.isPrefab by family {
        has<Prefab>()
        or {
            has<SetBlock>()
            has<BlockyFurniture>()
            has<BlockyModelEngine>()
        }
    }
}

object BlockyBlockQuery : GearyQuery() {

    val Pointer.prefabKey by get<PrefabKey>()
    val Pointer.block by get<SetBlock>()
    val Pointer.isPrefab by family {
        has<Prefab>()
        has<SetBlock>()
        not {
            has<BlockyFurniture>()
            has<BlockyModelEngine>()
        }
    }
}

val blockyBlockQuery get() =
    BlockyBlockQuery.toList { it }.filter {
        it.block.blockType !in setOf(SetBlock.BlockType.WIRE, SetBlock.BlockType.CAVEVINE) &&
                it.entity.get<BlockyDirectional>()?.isParentBlock != false
    }.sortedBy { it.prefabKey.key }

val blockyPlantQuery get() =
    BlockyBlockQuery.toList { it }.filter {
        it.block.blockType in setOf(SetBlock.BlockType.WIRE, SetBlock.BlockType.CAVEVINE)
    }.sortedBy { it.prefabKey.key }

object BlockyFurnitureQuery : GearyQuery() {
    val Pointer.key by get<PrefabKey>()
    val Pointer.modelEngine by family {
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

val blockyFurnitureQuery get() = BlockyFurnitureQuery.toList { it }.sortedBy { it.prefabKey.key }

@OptIn(UnsafeAccessors::class)
val blockyModelEngineQuery =
    BlockyFurnitureQuery.toList { it }.filter { it.entity.isModelEngineFurniture }.map { it.prefabKey.toString() }
