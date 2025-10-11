package com.mineinabyss.blocky.menus

import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.systems.BlockyPrefabs
import com.mineinabyss.blocky.systems.blockPrefabs
import com.mineinabyss.blocky.systems.furniturePrefabs
import com.mineinabyss.blocky.systems.plantPrefabs
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.guiy.viewmodel.GuiyViewModel
import com.mineinabyss.idofront.textcomponents.miniMsg
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack

data class PrefabPickerState(
    val title: Component,
    val height: Int,
)

class BlockyViewModel(
    val geary: Geary,
) : GuiyViewModel() {
    fun getItemsForCategory(blockyScreen: BlockyCategory): List<ItemStack> = getItemsForPrefabs(
        when (blockyScreen) {
            BlockyCategory.BLOCK -> blockPrefabs
            BlockyCategory.WIRE -> plantPrefabs
            BlockyCategory.FURNITURE -> furniturePrefabs
        }
    )

    fun decorateTitle(title: Component, page: Int, hasMultiplePages: Boolean): Component {
        return Component.textOfChildren(title, buildString {
            if (!hasMultiplePages) return@buildString
            if (page > 0) append(":space_-132::blocky_scrolling_up::space_36:")
            else append(":space_-80:")
            append(":blocky_scrolling_down:")
        }.miniMsg())
    }

    fun getPickerState(page: Int, screen: BlockyCategory, items: List<ItemStack>): PrefabPickerState {
        val menus = blocky.config.menus
        val config = when (screen) {
            BlockyCategory.BLOCK -> menus.blockMenu
            BlockyCategory.WIRE -> menus.furnitureMenu
            BlockyCategory.FURNITURE -> menus.wireMenu
        }
        val hasMultiplePages = items.size.toDouble().div(9 * (config.height - 1)) > 1
        return PrefabPickerState(
            decorateTitle(config.title, page, hasMultiplePages),
            config.height,
        )
    }


    private fun getItemsForPrefabs(prefabs: List<BlockyPrefabs>): List<ItemStack> = with(geary) {
        prefabs.sortedBy { it.prefabKey.full }
            .mapNotNull { getAddon(ItemTracking).createItem(it.prefabKey) }
    }
}