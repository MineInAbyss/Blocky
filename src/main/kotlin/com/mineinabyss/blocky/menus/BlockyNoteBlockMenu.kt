package com.mineinabyss.blocky.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.blocky.BlockyTypeQuery
import com.mineinabyss.blocky.components.BlockType
import com.mineinabyss.blocky.components.BlockyType
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.looty.ecs.components.LootyType
import org.bukkit.entity.Player

@Composable
fun GuiyOwner.BlockyNoteBlockMenu(player: Player) {
    Chest(setOf(player), ":something:", Modifier.height(5), onClose = { player.closeInventory() }) {
        Grid(Modifier.size(5, 5)) {
            BlockyTypeQuery.filter {
                it.entity.get<BlockyType>()?.blockType == BlockType.NORMAL
            }.forEach {
                Item(it.entity.get<LootyType>()?.createItem()!!)
            }
        }
    }
}