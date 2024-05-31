package com.mineinabyss.blocky.helpers.composables

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.canvases.LocalInventory
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.click.clickable
import org.bukkit.Sound
import org.bukkit.entity.Player

@Composable
inline fun Button(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    crossinline onClick: () -> Unit = {},
    playSound: Boolean = true,
    crossinline content: @Composable (enabled: Boolean) -> Unit,
) {
    val inv = LocalInventory.current
    Row(modifier.clickable {
        val player = whoClicked as? Player ?: return@clickable
        if (playSound) player.playSound(player.location, if (enabled) Sound.ITEM_ARMOR_EQUIP_GENERIC else Sound.BLOCK_LEVER_CLICK, 1f, 1f)
        if (enabled) onClick()
    }) {
        content(enabled)
    }
}
