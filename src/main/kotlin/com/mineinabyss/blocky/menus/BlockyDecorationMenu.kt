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
fun BlockyUIScope.BlockyDecorationMenu() {
    Grid(Modifier.size(5, 5)) {
        val passthroughs = BlockyTypeQuery.filter {
            it.entity.get<BlockyType>()?.blockType == BlockType.WALL &&
            it.entity.get<BlockyType>()?.blockType == BlockType.GROUND

        }
        passthroughs.forEach {
            val decoration = LootyFactory.createFromPrefab(it.key) ?: return@forEach
            Item(decoration, Modifier.clickable {
                val cursor = player.itemOnCursor
                if (cursor.type == Material.AIR) player.setItemOnCursor(decoration)
                val isEqual = player.itemOnCursor.itemMeta.customModelData == decoration.itemMeta.customModelData

                if (clickType.isShiftClick) {
                    player.setItemOnCursor(decoration)
                    player.itemOnCursor.amount = decoration.maxStackSize
                }
                else if (clickType.isLeftClick && !isEqual) player.setItemOnCursor(ItemStack(Material.AIR))
                else if (clickType.isRightClick) cursor.subtract(1)
                else if (clickType.isLeftClick && isEqual && cursor.amount < decoration.maxStackSize) cursor.amount += 1
            })
        }
    }
}