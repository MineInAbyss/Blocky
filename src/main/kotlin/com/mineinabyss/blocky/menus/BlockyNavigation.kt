package com.mineinabyss.blocky.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.blocky.helpers.composables.Button
import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.guiy.canvas.CurrentPlayer
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.navigation.LocalBackGestureDispatcher
import com.mineinabyss.guiy.navigation.NavHost
import com.mineinabyss.guiy.navigation.composable
import com.mineinabyss.guiy.navigation.rememberNavController
import com.mineinabyss.guiy.viewmodel.viewModel
import com.mineinabyss.idofront.textcomponents.miniMsg
import net.kyori.adventure.key.Key

@Composable
fun BlockyMainMenu() {
    val player = CurrentPlayer
    val nav = rememberNavController()
    viewModel { BlockyViewModel(player.world.toGeary()) }
    NavHost(nav, startDestination = BlockyScreen.Default) {
        composable<BlockyScreen.Default> {
            BlockyMenu(navigateToPrefabPicker = { nav.navigate(BlockyScreen.PrefabPicker(it)) })
        }
        composable<BlockyScreen.PrefabPicker> { PrefabPickerScreen(it) }
    }
}

val emptyItemModel = Key.key("minecraft:empty")

@Composable
fun BackButton(modifier: Modifier = Modifier) {
    val gestureDispatcher = LocalBackGestureDispatcher.current
    Button(onClick = { gestureDispatcher.onBack() }, modifier = modifier) {
        Text("<red><b>Back".miniMsg())
    }
}
