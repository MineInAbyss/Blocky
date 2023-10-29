package com.mineinabyss.blocky.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.helpers.composables.Button
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.guiy.navigation.Navigator
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

sealed class BlockyScreen(val title: String, val height: Int) {
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
fun GuiyOwner.BlockyMainMenu(player: Player) {
    BlockyUIScope(player).apply {
        nav.withScreen(setOf(player), onEmpty = ::exit) { screen ->
            Chest(setOf(player), screen.title,
                Modifier.height(screen.height),
                onClose = { player.closeInventory() }) {
                when (screen) {
                    is BlockyScreen.Default -> BlockyMenu()
                    is BlockyScreen.Block -> BlockyBlockMenu()
                    is BlockyScreen.Wire -> BlockyWireMenu()
                    is BlockyScreen.Furniture -> BlockyFurnitureMenu()
                }
            }
        }
    }
}

@Composable
fun BlockyUIScope.BackButton(modifier: Modifier = Modifier) {
    Button(onClick = { nav.back() }, modifier = modifier) {
        Item(ItemStack(Material.PAPER).editItemMeta {
            displayName("<red><b>Back".miniMsg())
            setCustomModelData(1)
        })
    }
}
