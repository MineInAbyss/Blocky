package com.mineinabyss.blocky.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.miniMsg
import com.mineinabyss.looty.LootyFactory
import com.mineinabyss.looty.tracking.toGearyFromUUIDOrNull
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

@Composable
fun BlockyUIScope.BlockyMenu() {
    Item(ItemStack(Material.NOTE_BLOCK).editItemMeta {
        displayName("<#4b3123><b>Blocks".miniMsg())
    }, Modifier.at(1,2).clickable {
        nav.open(BlockyScreen.NoteBlock)
    })

    Item(ItemStack(Material.TRIPWIRE_HOOK).editItemMeta {
        displayName("<#7fa256><b>Plants".miniMsg())
    }, Modifier.at(3,2).clickable {
        nav.open(BlockyScreen.Decoration)
    })

    Item(ItemStack(Material.ARMOR_STAND).editItemMeta {
        displayName("<#e91e63><b>Furniture".miniMsg())
    }, Modifier.at(5,2).clickable {
        nav.open(BlockyScreen.JavaEntity)
    })

    Item(ItemStack(Material.ARMOR_STAND,2).editItemMeta {
        displayName("<#4f80c3><b>ModelEngine Furniture / Blocks".miniMsg())
    }, Modifier.at(7,2).clickable {
        nav.open(BlockyScreen.AnimatedEntity)
    })

    BackButton(Modifier.at(0, 4))
}

@Composable
fun HandleMenuClicks(key: PrefabKey, player: Player) {
    val block = LootyFactory.createFromPrefab(key)
    Item(block, Modifier.clickable {
        when (clickType) {
            ClickType.LEFT -> {
                if (cursor == null) cursor = block
                else if (cursor?.toGearyFromUUIDOrNull() == block?.toGearyFromUUIDOrNull()) cursor?.add(1)
                else cursor = block?.asQuantity(1)
            }
            ClickType.RIGHT -> {
                if (cursor == null) cursor = block?.asQuantity(1)
                else cursor?.subtract(1)
            }
            ClickType.MIDDLE -> cursor = block?.asQuantity(block.maxStackSize)
            ClickType.SHIFT_LEFT -> cursor = block?.asQuantity(block.maxStackSize)
            ClickType.SHIFT_RIGHT -> cursor = block?.asQuantity(block.maxStackSize)
            ClickType.DROP -> block?.let { player.world.dropItemNaturally(player.location, it) }
            ClickType.CONTROL_DROP -> block?.asQuantity(block.maxStackSize)?.let { player.world.dropItemNaturally(player.location, it) }
            else -> return@clickable
        }
    })
}
