package com.mineinabyss.blocky.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.blocky.BlockyTypeQuery
import com.mineinabyss.blocky.BlockyTypeQuery.key
import com.mineinabyss.blocky.components.BlockType
import com.mineinabyss.blocky.components.BlockyType
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.looty.LootyFactory
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

@Composable
fun BlockyUIScope.BlockyPassthroughMenu() {
    Grid(Modifier.size(5, 5)) {
        val passthroughs = BlockyTypeQuery.filter {
            it.entity.get<BlockyType>()?.blockType == BlockType.PASSTHROUGH

        }
        passthroughs.forEach {
            val passthrough = LootyFactory.createFromPrefab(it.key) ?: return@forEach
            Item(passthrough, Modifier.clickable {
                val cursor = player.itemOnCursor
                if (cursor.type == Material.AIR) player.setItemOnCursor(passthrough)
                val isEqual = player.itemOnCursor.itemMeta.customModelData == passthrough.itemMeta.customModelData

                if (clickType.isShiftClick) {
                    player.setItemOnCursor(passthrough)
                    player.itemOnCursor.amount = passthrough.maxStackSize
                }
                else if (clickType.isLeftClick && !isEqual) player.setItemOnCursor(ItemStack(Material.AIR))
                else if (clickType.isRightClick) cursor.subtract(1)
                else if (clickType.isLeftClick && isEqual && cursor.amount < passthrough.maxStackSize) cursor.amount += 1
            })
        }
    }
}