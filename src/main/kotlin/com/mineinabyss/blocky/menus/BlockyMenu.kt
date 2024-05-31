package com.mineinabyss.blocky.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.helpers.gearyInventory
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.click.clickable
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

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

@Composable
fun HandleMenuClicks(key: PrefabKey, player: Player) {
    val block = gearyItems.createItem(key)
    Item(block, Modifier.clickable {
        when (clickType) {
            ClickType.LEFT -> when {
                cursor == null -> whoClicked.setItemOnCursor(block)
                player.gearyInventory?.itemOnCursor?.prefabs?.first()?.get<PrefabKey>() == key -> cursor?.add(1)
                else -> whoClicked.setItemOnCursor(block?.asQuantity(1))
            }
            ClickType.RIGHT -> when (cursor) {
                null -> whoClicked.setItemOnCursor(block?.asQuantity(1))
                else -> cursor?.subtract(1)
            }
            ClickType.MIDDLE -> whoClicked.setItemOnCursor(block?.asQuantity(block.maxStackSize))
            ClickType.SHIFT_LEFT -> whoClicked.setItemOnCursor(block?.asQuantity(block.maxStackSize))
            ClickType.SHIFT_RIGHT -> whoClicked.setItemOnCursor(block?.asQuantity(block.maxStackSize))
            ClickType.DROP -> block?.let { player.world.dropItemNaturally(player.location, it) }
            ClickType.CONTROL_DROP -> block?.asQuantity(block.maxStackSize)?.let { player.world.dropItemNaturally(player.location, it) }
            else -> return@clickable
        }
    })
}
