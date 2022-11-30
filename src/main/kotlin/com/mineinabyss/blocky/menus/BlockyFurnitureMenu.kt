package com.mineinabyss.blocky.menus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mineinabyss.blocky.systems.BlockyBlockQuery.prefabKey
import com.mineinabyss.blocky.systems.BlockyFurnitureQuery
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size

@Composable
fun BlockyUIScope.BlockyFurnitureMenu() {
    Grid(Modifier.size(9, 5)) {
        remember {
            BlockyFurnitureQuery
        }.sortedBy { it.prefabKey.key }.forEach { HandleMenuClicks(it.prefabKey, player) }
    }
    BackButton(Modifier.at(0, 5))
}
