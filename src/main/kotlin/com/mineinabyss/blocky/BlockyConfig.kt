package com.mineinabyss.blocky

import kotlinx.serialization.Serializable

@Serializable
data class BlockyConfig(
    val menus: BlockyMenus,
    val noteBlocks: BlockyNoteBlockConfig,
    val tripWires: BlockyTripwireConfig,
    val caveVineBlocks: BlockyCaveVineConfig,
    val slabBlocks: BlockySlabConfig,
    val stairBlocks: BlockyStairConfig,
    val disableCustomSounds: Boolean = false,
    val MoreCreativeTabsMod: MoreCreativeTabsModConfig,
) {

    @Serializable
    data class BlockyMenus(
        val defaultMenu: BlockyMenu = BlockyMenu(height = 5),
        val blockMenu: BlockyMenu = BlockyMenu(),
        val wireMenu: BlockyMenu = BlockyMenu(),
        val furnitureMenu: BlockyMenu = BlockyMenu()
    )

    @Serializable data class BlockyMenu(val title: String = "", val height: Int = 6)
    @Serializable data class MoreCreativeTabsModConfig(val generateJsonForMod: Boolean = false)
    @Serializable data class BlockyNoteBlockConfig(val isEnabled: Boolean = true, val restoreFunctionality: Boolean = false)
    @Serializable data class BlockyTripwireConfig(val isEnabled: Boolean = true)
    @Serializable data class BlockyCaveVineConfig(val isEnabled: Boolean = false)
    @Serializable data class BlockySlabConfig(val isEnabled: Boolean = false)
    @Serializable data class BlockyStairConfig(val isEnabled: Boolean = false)
}
