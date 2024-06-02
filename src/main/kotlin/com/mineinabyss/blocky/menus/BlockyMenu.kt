package com.mineinabyss.blocky.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.blocky.blocky
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.click.clickable

@Composable
fun BlockyUIScope.BlockyMenu() {
    Item(blocky.config.menus.defaultMenu.blockButton.toItemStack(), Modifier.at(2,1).clickable {
        nav.open(BlockyScreen.Block())
    })

    Item(blocky.config.menus.defaultMenu.wireButton.toItemStack(), Modifier.at(4,1).clickable {
        nav.open(BlockyScreen.Wire())
    })

    Item(blocky.config.menus.defaultMenu.furnitureButton.toItemStack(), Modifier.at(6,1).clickable {
        nav.open(BlockyScreen.Furniture())
    })

    BackButton(Modifier.at(0, 4))
}
