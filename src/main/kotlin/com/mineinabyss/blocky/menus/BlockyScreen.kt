package com.mineinabyss.blocky.menus

sealed interface BlockyScreen {
    data object Default : BlockyScreen
    data class PrefabPicker(val category: BlockyCategory): BlockyScreen
}

enum class BlockyCategory {
    BLOCK, WIRE, FURNITURE;
}