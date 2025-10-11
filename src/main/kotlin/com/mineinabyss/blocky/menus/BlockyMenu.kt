package com.mineinabyss.blocky.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.blocky.blocky
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.click.clickable
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.guiy.modifiers.placement.absolute.at

@Composable
fun BlockyMenu(navigateToPrefabPicker: (BlockyCategory) -> Unit) {
    val menu = blocky.config.menus.defaultMenu
    Chest(menu.title, Modifier.height(menu.height)) {
        Item(menu.blockButton.toItemStack(), Modifier.at(2, 1).clickable {
            navigateToPrefabPicker(BlockyCategory.BLOCK)
        })

        Item(menu.wireButton.toItemStack(), Modifier.at(4, 1).clickable {
            navigateToPrefabPicker(BlockyCategory.WIRE)
        })

        Item(menu.furnitureButton.toItemStack(), Modifier.at(6, 1).clickable {
            navigateToPrefabPicker(BlockyCategory.FURNITURE)
        })
    }
}
