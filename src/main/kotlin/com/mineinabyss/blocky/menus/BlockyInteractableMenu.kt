package com.mineinabyss.blocky.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.blocky.BlockyTypeQuery
import com.mineinabyss.blocky.components.BlockType
import com.mineinabyss.blocky.components.BlockyType
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.looty.ecs.components.LootyType
import org.bukkit.Material

@Composable
fun BlockyUIScope.BlockyInteractableMenu() {
    Grid(Modifier.size(5, 5)) {
        BlockyTypeQuery.filter {
            it.entity.get<BlockyType>()?.blockType == BlockType.INTERACTABLE
        }.forEach {
            val item = it.entity.get<LootyType>()?.createItem()!!
            Item(it.entity.get<LootyType>()?.createItem()!!, Modifier.clickable {
                if (player.itemOnCursor.type == Material.AIR) player.setItemOnCursor(item)
                else if (player.itemOnCursor == item) player.itemOnCursor.amount += 1
            })
        }
    }
}