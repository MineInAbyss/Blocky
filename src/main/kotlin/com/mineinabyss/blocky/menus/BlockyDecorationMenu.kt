package com.mineinabyss.blocky.menus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mineinabyss.blocky.BlockyTypeQuery
import com.mineinabyss.blocky.BlockyTypeQuery.key
import com.mineinabyss.blocky.components.BlockType
import com.mineinabyss.blocky.components.blockyBlock
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size

@Composable
fun BlockyUIScope.BlockyDecorationMenu() {
    Grid(Modifier.size(9, 5)) {
        BlockyTypeQuery.filter {
            remember {
                it.entity.blockyBlock?.blockType == BlockType.GROUND
            }
        }.sortedBy { it.key.key }.forEach { handleMenuClicks(it.key, player) }
    }
    BackButton(Modifier.at(0, 5))
}