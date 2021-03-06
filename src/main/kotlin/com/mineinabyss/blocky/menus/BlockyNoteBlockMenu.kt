package com.mineinabyss.blocky.menus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mineinabyss.blocky.BlockyTypeQuery
import com.mineinabyss.blocky.BlockyTypeQuery.key
import com.mineinabyss.blocky.components.BlockType
import com.mineinabyss.blocky.components.BlockyBlock
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size

@Composable
fun BlockyUIScope.BlockyNoteBlockMenu() {
    Grid(Modifier.size(9, 5)) {
        remember {
            BlockyTypeQuery.filter {
                it.entity.get<BlockyBlock>()?.blockType == BlockType.CUBE ||
                        it.entity.get<BlockyBlock>()?.blockType == BlockType.TRANSPARENT
            }
        }.sortedBy { it.key.key }.forEach { HandleMenuClicks(it.key, player) }
    }

    BackButton(Modifier.at(0, 5))
}
