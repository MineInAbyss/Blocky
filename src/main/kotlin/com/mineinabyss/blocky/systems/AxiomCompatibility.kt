package com.mineinabyss.blocky.systems

import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.features.blocks.BlockyDirectional
import com.mineinabyss.blocky.helpers.blockyNoteBlock
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.textcomponents.serialize
import com.moulberry.axiom.paperapi.AxiomCustomBlocksAPI
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.key.Key

object AxiomCompatibility {

    enum class DirectionalType {
        NONE, AXIS, FACING, HORIZONTAL_FACING
    }

    data class DirectionalBlocks(val type: DirectionalType, val blocks: List<PrefabKey>) {
        constructor(parent: BlockyDirectional) : this(
            when {
                parent.isLogType -> DirectionalType.AXIS
                parent.isDropperType -> DirectionalType.FACING
                parent.isFurnaceType -> DirectionalType.HORIZONTAL_FACING
                else -> DirectionalType.NONE
            }, listOfNotNull(
                parent.xBlock, parent.yBlock, parent.zBlock,
                parent.northBlock, parent.eastBlock, parent.southBlock, parent.westBlock,
                parent.upBlock, parent.downBlock
            )
        )
    }

    context(Geary)
    fun registerCustomBlocks() {
        AxiomCustomBlocksAPI.getAPI().unregisterAll(blocky.plugin)
        blocky.blockQuery.forEach { (prefab, block, directional, itemstack) ->
            if (block.blockType != SetBlock.BlockType.NOTEBLOCK || directional != null) return@forEach

            val key = Key.key(prefab.full)
            val translationKey = itemstack?.getData(DataComponentTypes.ITEM_NAME)?.serialize() ?: prefab.full
            val blockData = prefab.blockyNoteBlock()
            val builder = AxiomCustomBlocksAPI.getAPI().createSingle(key, translationKey, blockData)

            builder.preventShapeUpdates(true)
            builder.pickBlockItemStack(itemstack)
            AxiomCustomBlocksAPI.getAPI().register(blocky.plugin, builder)
        }

        blocky.blockQuery.mapNotNullWithEntity { (prefab, block, directional, itemstack) ->
            if (block.blockType != SetBlock.BlockType.NOTEBLOCK) return@mapNotNullWithEntity null
            if (directional?.isParentBlock != true) return@mapNotNullWithEntity null

            val key = Key.key(prefab.full)
            val translationKey = itemstack?.getData(DataComponentTypes.ITEM_NAME)?.serialize() ?: prefab.full
            val directionalBlocks = DirectionalBlocks(directional)
            val builder = when(directionalBlocks.type) {
                DirectionalType.AXIS -> {
                    val (x, y, z) = directionalBlocks.blocks.take(3).map { it.blockyNoteBlock() }
                    AxiomCustomBlocksAPI.getAPI().createAxis(key, translationKey, x, y, z)
                }
                DirectionalType.HORIZONTAL_FACING -> {
                    val (n,e,s,w) = directionalBlocks.blocks.take(4).map { it.blockyNoteBlock() }
                    AxiomCustomBlocksAPI.getAPI().createHorizontalFacing(key, translationKey, n, e, s, w)
                }
                DirectionalType.FACING -> {
                    val (n,e,s,w) = directionalBlocks.blocks.take(4).map { it.blockyNoteBlock() }
                    val (u,d) = directionalBlocks.blocks.takeLast(2).map { it.blockyNoteBlock() }
                    AxiomCustomBlocksAPI.getAPI().createFacing(key, translationKey, n,e,s,w,u,d)
                }
                else -> return@mapNotNullWithEntity null
            }

            builder.preventShapeUpdates(true)
            builder.pickBlockItemStack(itemstack)
            AxiomCustomBlocksAPI.getAPI().register(blocky.plugin, builder)
        }
    }
}