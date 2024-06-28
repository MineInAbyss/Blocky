package com.mineinabyss.blocky.menus

import androidx.compose.runtime.*
import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.helpers.composables.Button
import com.mineinabyss.blocky.systems.blockPrefabs
import com.mineinabyss.blocky.systems.furniturePrefabs
import com.mineinabyss.blocky.systems.plantPrefabs
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.guiy.components.CreativeItem
import com.mineinabyss.guiy.components.HorizontalGrid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.VerticalGrid
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.components.lists.NavbarPosition
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
            var title by remember(screen) { mutableStateOf(screen.handleTitle(0)) }
            Chest(setOf(player), title, Modifier.height(screen.height), onClose = { owner.exit() }) {
                when (screen) {
                    is BlockyScreen.Default -> BlockyMenu()
                    else -> {
                        var line by remember(screen) { mutableStateOf(0) }
                        val items = remember(screen) {
                            when (screen) {
                                is BlockyScreen.Block -> blockPrefabs
                                is BlockyScreen.Wire -> plantPrefabs
                                is BlockyScreen.Furniture -> furniturePrefabs
                                else -> return@remember emptyList()
                            }.sortedBy { it.prefabKey.full }.map { gearyItems.createItem(it.prefabKey) }
                        }

                        Scrollable(
                            items, line, 8, 5,
                            nextButton = { ScrollDownButton(Modifier.at(0, 3).clickable { line++; title = screen.handleTitle(line) }) },
                            previousButton = { ScrollUpButton(Modifier.at(0, 1).clickable { line--; title = screen.handleTitle(line) }) },
                            NavbarPosition.START, null
                        ) { pageItems ->
                            VerticalGrid(Modifier.size(8, 5)) {
                                pageItems.forEach { CreativeItem(it) }
                            }
                        }

                        BackButton(Modifier.at(0, 5))
                    }
                }
            }
        }
    }
}

private fun BlockyScreen.handleTitle(page: Int): Component {
    if (this is BlockyScreen.Default) return title
    return Component.textOfChildren(title, buildString {
        append(":space_-26:")
        if (page > 0) append(":blocky_scrolling_up::space_-18:")
        append(":blocky_scrolling_down:")
    }.miniMsg())
}

@Composable
fun PaginatedNextButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier) {
        Item(ItemStack(Material.PAPER).editItemMeta {
            itemName("<green><b>Next".miniMsg())
            setCustomModelData(0)
        }, modifier)
    }
}

@Composable
fun PaginatedPrevButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier) {
        Item(ItemStack(Material.PAPER).editItemMeta {
            itemName("<red><b>Previous".miniMsg())
            setCustomModelData(0)
        }, modifier)
    }
}

@Composable
fun ScrollDownButton(modifier: Modifier = Modifier) {
    Item(ItemStack(Material.PAPER).editItemMeta {
        itemName("<green><b>Scroll Down".miniMsg())
        setCustomModelData(0)
    }, modifier)
}

@Composable
fun ScrollUpButton(modifier: Modifier = Modifier) {
    Item(ItemStack(Material.PAPER).editItemMeta {
        itemName("<blue><b>Scroll Up".miniMsg())
        isHideTooltip = true
        setCustomModelData(0)
    }, modifier)
}

@Composable
fun BlockyUIScope.BackButton(modifier: Modifier = Modifier, onClick: () -> Unit = { nav.back() }) {
    Button(onClick = onClick, modifier = modifier) {
        Item(ItemStack(Material.PAPER).editItemMeta {
            itemName("<red><b>Back".miniMsg())
            setCustomModelData(0)
        })
    }
}
