package com.mineinabyss.blocky.menus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mineinabyss.blocky.helpers.ui.Navigator
import com.mineinabyss.blocky.helpers.ui.composables.Button
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.textcomponents.miniMsg
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

sealed class BlockyScreen(val title: Component, val height: Int) {
    object Default : BlockyScreen("${Space.of(-12)}:something:".miniMsg(), 5)
    object NoteBlock : BlockyScreen("${Space.of(-12)}:something:".miniMsg(), 6)
    object Wire : BlockyScreen("${Space.of(-12)}:something:".miniMsg(), 6)
    object Furniture : BlockyScreen("${Space.of(-12)}:something:".miniMsg(), 6)

}

typealias BlockyNav = Navigator<BlockyScreen>

class BlockyUIScope(
    val player: Player,
) {
    val nav = BlockyNav { BlockyScreen.Default }
}

@Composable
fun GuiyOwner.BlockyMainMenu(player: Player) {
    val scope = remember { BlockyUIScope(player) }
    scope.apply {
        nav.withScreen(setOf(player), onEmpty = ::exit) { screen ->
            Chest(setOf(player), screen.title,
                Modifier.height(screen.height),
                onClose = { player.closeInventory() }) {
                when (screen) {
                    BlockyScreen.Default -> BlockyMenu()
                    BlockyScreen.NoteBlock -> BlockyNoteBlockMenu()
                    BlockyScreen.Wire -> BlockyWireMenu()
                    BlockyScreen.Furniture -> BlockyFurnitureMenu()
                }
            }
        }
    }
}

@Composable
fun BlockyUIScope.BackButton(modifier: Modifier = Modifier) {
    Button(onClick = { nav.back() }, modifier = modifier) {
        Item(ItemStack(Material.STONE)/*HeadLib.STONE_ARROW_LEFT.toItemStack("Back")*/)
    }
}
