package com.mineinabyss.blocky.menus

import androidx.compose.runtime.*
import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.helpers.composables.Button
import com.mineinabyss.blocky.systems.blockPrefabs
import com.mineinabyss.blocky.systems.furniturePrefabs
import com.mineinabyss.blocky.systems.plantPrefabs
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.guiy.components.CreativeItem
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.VerticalGrid
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.components.lists.NavbarPosition
import com.mineinabyss.guiy.components.lists.ScrollDirection
import com.mineinabyss.guiy.components.lists.Scrollable
import com.mineinabyss.guiy.inventory.LocalGuiyOwner
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.click.clickable
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.guiy.navigation.Navigator
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.textcomponents.miniMsg
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

sealed class BlockyScreen(val title: Component, val height: Int) {
    class Default : BlockyScreen(blocky.config.menus.defaultMenu.title, blocky.config.menus.defaultMenu.height)
    class Block : BlockyScreen(blocky.config.menus.blockMenu.title, blocky.config.menus.blockMenu.height)
    class Wire : BlockyScreen(blocky.config.menus.wireMenu.title, blocky.config.menus.wireMenu.height)
    class Furniture : BlockyScreen(blocky.config.menus.furnitureMenu.title, blocky.config.menus.furnitureMenu.height)

}

typealias BlockyNav = Navigator<BlockyScreen>

class BlockyUIScope(val player: Player) {
    val nav = BlockyNav { BlockyScreen.Default() }
}

@Composable
fun BlockyMainMenu(player: Player) {
    val owner = LocalGuiyOwner.current
    BlockyUIScope(player).apply {
        nav.withScreen(setOf(player), onEmpty = owner::exit) { screen ->
            val items = remember(screen) {
                when (screen) {
                    is BlockyScreen.Block -> blockPrefabs
                    is BlockyScreen.Wire -> plantPrefabs
                    is BlockyScreen.Furniture -> furniturePrefabs
                    else -> return@remember emptyList()
                }.sortedBy { it.prefabKey.full }.map { gearyItems.createItem(it.prefabKey) }
            }
            val hasMultiplePages by remember(screen) { mutableStateOf(items.size.toDouble().div(9 * 5) > 1) }
            var title by remember(screen) { mutableStateOf(blocky.plugin.handleTitle(screen, 0, hasMultiplePages)) }
            var line by remember(screen) { mutableStateOf(0) }

            Chest(setOf(player), title, Modifier.height(screen.height), onClose = { owner.exit() }) {
                when (screen) {
                    is BlockyScreen.Default -> BlockyMenu()
                    else -> {
                        Scrollable(
                            items, line, ScrollDirection.VERTICAL,
                            nextButton = { ScrollDownButton(Modifier.at(5, 0).clickable { line++; title = blocky.plugin.handleTitle(screen, line, hasMultiplePages) }) },
                            previousButton = { ScrollUpButton(Modifier.at(2, 0).clickable { line--; title = blocky.plugin.handleTitle(screen, line, hasMultiplePages) }) },
                            NavbarPosition.BOTTOM, null
                        ) { pageItems ->
                            VerticalGrid(Modifier.size(9, 5)) {
                                pageItems.forEach { CreativeItem(it) }
                            }
                        }

                        BackButton(Modifier.at(8, 5))
                    }
                }
            }
        }
    }
}

@Composable
fun ScrollDownButton(modifier: Modifier = Modifier) {
    Item(ItemStack(Material.PAPER).editItemMeta {
        itemName("<green><b>Scroll Down".miniMsg())
        setCustomModelData(1)
    }, modifier)
}

@Composable
fun ScrollUpButton(modifier: Modifier = Modifier) {
    Item(ItemStack(Material.PAPER).editItemMeta {
        itemName("<blue><b>Scroll Up".miniMsg())
        setCustomModelData(1)
    }, modifier)
}

@Composable
fun BlockyUIScope.BackButton(modifier: Modifier = Modifier, onClick: () -> Unit = { nav.back() }) {
    Button(onClick = onClick, modifier = modifier) {
        Item(ItemStack(Material.PAPER).editItemMeta {
            itemName("<red><b>Back".miniMsg())
            setCustomModelData(1)
        })
    }
}
