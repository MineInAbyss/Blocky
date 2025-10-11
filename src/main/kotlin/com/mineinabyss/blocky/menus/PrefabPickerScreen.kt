package com.mineinabyss.blocky.menus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mineinabyss.guiy.components.CreativeItem
import com.mineinabyss.guiy.components.VerticalGrid
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.components.lists.NavbarPosition
import com.mineinabyss.guiy.components.lists.ScrollDirection
import com.mineinabyss.guiy.components.lists.Scrollable
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.guiy.viewmodel.viewModel
import com.mineinabyss.idofront.textcomponents.miniMsg

@Composable
fun PrefabPickerScreen(
    screen: BlockyScreen.PrefabPicker,
    viewModel: BlockyViewModel = viewModel(),
) {
    val items = remember(screen) { viewModel.getItemsForCategory(screen.category) }
    var page by remember(screen) { mutableStateOf(0) }
    val prefabPickerState = remember(screen, page) { viewModel.getPickerState(page, screen.category, items) }
    Chest(prefabPickerState.title, Modifier.height(prefabPickerState.height)) {
        Scrollable(
            items,
            line = page,
            scrollDirection = ScrollDirection.VERTICAL,
            nextButton = { Text("<green><b>Scroll Down".miniMsg()) },
            previousButton = { Text("<blue><b>Scroll Up".miniMsg()) },
            onLineChange = { page = it },
            navbarPosition = NavbarPosition.BOTTOM,
            navbarBackground = null
        ) { pageItems ->
            VerticalGrid(Modifier.size(9, 5)) {
                pageItems.forEach { CreativeItem(it) }
            }
        }

        BackButton(Modifier.at(8, 5))
    }
}