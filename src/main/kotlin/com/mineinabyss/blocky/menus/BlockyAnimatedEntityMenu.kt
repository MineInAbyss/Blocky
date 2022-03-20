package com.mineinabyss.blocky.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.blocky.BlockyTypeQuery
import com.mineinabyss.blocky.BlockyTypeQuery.key
import com.mineinabyss.blocky.components.BlockyEntity
import com.mineinabyss.blocky.components.EntityType
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.looty.LootyFactory
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

@Composable
fun BlockyUIScope.BlockyAnimatedEntityMenu() {
    Grid(Modifier.size(5, 5)) {
        val interactables = BlockyTypeQuery.filter {
            it.entity.get<BlockyEntity>()?.entityType == EntityType.MODEL_ENGINE
        }
        interactables.forEach {
            val interactable = LootyFactory.createFromPrefab(it.key) ?: return@forEach
            Item(interactable, Modifier.clickable {
                val cursor = player.itemOnCursor
                if (cursor.type == Material.AIR) player.setItemOnCursor(interactable)
                val isEqual = player.itemOnCursor.itemMeta.customModelData == interactable.itemMeta.customModelData

                if (clickType.isShiftClick) {
                    player.setItemOnCursor(interactable)
                    player.itemOnCursor.amount = interactable.maxStackSize
                }
                else if (clickType.isLeftClick && !isEqual) player.setItemOnCursor(ItemStack(Material.AIR))
                else if (clickType.isRightClick) cursor.subtract(1)
                else if (clickType.isLeftClick && isEqual && cursor.amount < interactable.maxStackSize) cursor.amount += 1
            })
        }
    }
    BackButton(Modifier.at(0, 5))
}