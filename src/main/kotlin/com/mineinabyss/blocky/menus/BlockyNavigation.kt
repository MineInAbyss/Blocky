package com.mineinabyss.blocky.menus

import androidx.compose.runtime.*
import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.helpers.composables.Button
import com.mineinabyss.blocky.systems.blockPrefabs
import com.mineinabyss.blocky.systems.furniturePrefabs
import com.mineinabyss.blocky.systems.plantPrefabs
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.papermc.withGeary
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
import com.mineinabyss.idofront.textcomponents.miniMsg
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.key.Key
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
                player.withGeary {
                    when (screen) {
                        is BlockyScreen.Block -> blockPrefabs
                        is BlockyScreen.Wire -> plantPrefabs
                        is BlockyScreen.Furniture -> furniturePrefabs
                        else -> return@remember emptyList()
                    }.sortedBy { it.prefabKey.full }.map { getAddon(ItemTracking).createItem(it.prefabKey) }
                }
            }
            val hasMultiplePages by remember(screen) { mutableStateOf(items.size.toDouble().div(9 * 5) > 1) }
            var title by remember(screen) { mutableStateOf(handleTitle(screen, 0, hasMultiplePages)) }
            var line by remember(screen) { mutableStateOf(0) }

            Chest(setOf(player), title, Modifier.height(screen.height), onClose = { owner.exit() }) {
                when (screen) {
                    is BlockyScreen.Default -> BlockyMenu()
                    else -> {
                        Scrollable(
                            items, line, ScrollDirection.VERTICAL,
                            nextButton = {
                                ScrollDownButton(Modifier
                                    .at(5, 0)
                                    .clickable { line++; title = handleTitle(screen, line, hasMultiplePages) }
                                )
                            },
                            previousButton = {
                                ScrollUpButton(Modifier
                                    .at(2, 0)
                                    .clickable { line--; title = handleTitle(screen, line, hasMultiplePages) }
                                )
                            },
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

private fun handleTitle(screen: BlockyScreen, page: Int, hasMultiplePages: Boolean): Component {
    if (screen is BlockyScreen.Default) return screen.title
    return Component.textOfChildren(screen.title, buildString {
        if (!hasMultiplePages) return@buildString
        if (page > 0) append(":space_-132::blocky_scrolling_up::space_36:")
        else append(":space_-80:")
        append(":blocky_scrolling_down:")
    }.miniMsg())
}

val emptyItemModel = Key.key("minecraft:empty")
@Composable
fun ScrollDownButton(modifier: Modifier = Modifier) {
    Item(ItemStack.of(Material.PAPER).apply {
        setData(DataComponentTypes.ITEM_NAME, "<green><b>Scroll Down".miniMsg())
        setData(DataComponentTypes.ITEM_MODEL, emptyItemModel)
    }, modifier)
}

@Composable
fun ScrollUpButton(modifier: Modifier = Modifier) {
    Item(ItemStack(Material.PAPER).apply {
        setData(DataComponentTypes.ITEM_NAME, "<blue><b>Scroll Up".miniMsg())
        setData(DataComponentTypes.ITEM_MODEL, emptyItemModel)
    }, modifier)
}

@Composable
fun BlockyUIScope.BackButton(modifier: Modifier = Modifier, onClick: () -> Unit = { nav.back() }) {
    Button(onClick = onClick, modifier = modifier) {
        Item(ItemStack(Material.PAPER).apply {
            setData(DataComponentTypes.ITEM_NAME, "<red><b>Back".miniMsg())
            setData(DataComponentTypes.ITEM_MODEL, emptyItemModel)
        })
    }
}
