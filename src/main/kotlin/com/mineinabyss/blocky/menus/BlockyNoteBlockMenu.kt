package com.mineinabyss.blocky.menus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mineinabyss.blocky.components.core.BlockyBlock.BlockType
import com.mineinabyss.blocky.components.features.BlockyDirectional
import com.mineinabyss.blocky.systems.BlockyBlockQuery
import com.mineinabyss.blocky.systems.BlockyBlockQuery.prefabKey
import com.mineinabyss.blocky.systems.BlockyBlockQuery.type
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size

@Composable
fun BlockyUIScope.BlockyNoteBlockMenu() {
    Grid(Modifier.size(9, 5)) {
        remember {
            BlockyBlockQuery.filter {
                it.type.blockType !in setOf(BlockType.WIRE, BlockType.CAVEVINE) &&
                it.entity.get<BlockyDirectional>()?.isParentBlock != false
            }
        }.sortedBy { it.prefabKey.key }.forEach { HandleMenuClicks(it.prefabKey, player) }
    }

    BackButton(Modifier.at(0, 5))
}
