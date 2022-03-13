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
fun BlockyUIScope.BlockyNoteBlockMenu() {
    Grid(Modifier.size(5, 5)) {
        val blocks = BlockyTypeQuery.filter {
            it.entity.get<BlockyType>()?.blockType == BlockType.NORMAL

        }
        blocks.forEach {
            val block = LootyFactory.createFromPrefab(it.key) ?: return@forEach
            Item(block, Modifier.clickable {
                val cursor = player.itemOnCursor
                if (cursor.type == Material.AIR) player.setItemOnCursor(block)
                val isEqual = player.itemOnCursor.itemMeta.customModelData == block.itemMeta.customModelData

                if (clickType.isShiftClick) {
                    player.setItemOnCursor(block)
                    player.itemOnCursor.amount = block.maxStackSize
                }
                else if (clickType.isLeftClick && !isEqual) player.setItemOnCursor(ItemStack(Material.AIR))
                else if (clickType.isRightClick) cursor.subtract(1)
                else if (clickType.isLeftClick && isEqual && cursor.amount < block.maxStackSize) cursor.amount += 1
            })
        }
    }
}