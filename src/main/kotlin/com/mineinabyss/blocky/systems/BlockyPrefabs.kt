package com.mineinabyss.blocky.systems

import com.mineinabyss.blocky.components.features.blocks.BlockyDirectional
import com.mineinabyss.blocky.components.features.furniture.BlockyModelEngine
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.prefabs.PrefabKey

sealed interface BlockyPrefabs {
    val prefabKey: PrefabKey
    companion object {
        fun from(
            prefabKey: PrefabKey,
            block: SetBlock?,
            directional: BlockyDirectional?,
            modelEngine: BlockyModelEngine?
        ): BlockyPrefabs? {
            if(block != null) {
                return Plant.from(prefabKey, block)
                    ?: Block.from(prefabKey, block, directional)
            }
            return Furniture.from(prefabKey, modelEngine)
        }
    }
    data class Block private constructor(
        override val prefabKey: PrefabKey,
        val block: SetBlock,
        val directional: BlockyDirectional?
    ): BlockyPrefabs {
        companion object {
            fun from(prefabKey: PrefabKey, block: SetBlock, directional: BlockyDirectional?): Block? {
                if (block.blockType in setOf(SetBlock.BlockType.WIRE, SetBlock.BlockType.CAVEVINE)) return null
                if (directional != null && !directional.isParentBlock) return null
                return Block(prefabKey, block, directional)
            }
        }
    }

    data class Plant private constructor(
        override val prefabKey: PrefabKey,
        val block: SetBlock
    ): BlockyPrefabs {
        companion object {
            fun from(prefabKey: PrefabKey, block: SetBlock): Plant? {
                if (block.blockType !in setOf(SetBlock.BlockType.WIRE, SetBlock.BlockType.CAVEVINE)) return null
                return Plant(prefabKey, block)
            }
        }
    }

    data class Furniture private constructor(
        override val prefabKey: PrefabKey,
        val modelEngine: BlockyModelEngine?
    ): BlockyPrefabs {
        val isModelEngine get() = modelEngine != null
        companion object {
            fun from(prefabKey: PrefabKey, modelEngine: BlockyModelEngine?): Furniture {
                return Furniture(prefabKey, modelEngine)
            }
        }
    }
}
