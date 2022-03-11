package com.mineinabyss.blocky.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.idofront.items.editItemMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Composable
fun GuiyOwner.BlockyTypeScreen(player: Player) {
    Chest(setOf(player), ":something:", Modifier.height(5), onClose = { player.closeInventory() }) {
        Item(ItemStack(Material.NOTE_BLOCK).editItemMeta {
            displayName(Component.text("Cubeoid Blocks").color(TextColor.color(75, 49, 35)))
        }, Modifier.at(1,2).clickable {
            guiy { BlockyNoteBlockMenu(player) }
        })

        Item(ItemStack(Material.TRIPWIRE_HOOK).editItemMeta {
            displayName(Component.text("Passthrough Blocks").color(TextColor.color(127, 162, 86)))
        }, Modifier.at(3,2).clickable {
            guiy { BlockyPassthroughMenu(player) }
        })

        Item(ItemStack(Material.ARMOR_STAND).editItemMeta {
            displayName(Component.text("Misc Entities/Blocks").color(TextColor.color(233, 30, 99)))
            //setCustomModelData(something)
        }, Modifier.at(5,2).clickable {
            guiy { BlockyMiscMenu(player) }
        })

        Item(ItemStack(Material.ARMOR_STAND).editItemMeta {
            displayName(Component.text("Interactable Entities/Blocks").color(TextColor.color(79, 128, 195)))
            //setCustomModelData(something)
        }, Modifier.at(7,2).clickable {
            guiy { BlockyInteractableMenu(player) }
        })
    }
}