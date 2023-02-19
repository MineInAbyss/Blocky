package com.mineinabyss.blocky

import kotlinx.serialization.Serializable

val blockyConfig get() = blockyPlugin.config.data
@Serializable
data class BlockyConfig(
    val menus: BlockyMenus,
    val noteBlocks: BlockyNoteBlockConfig,
    val tripWires: BlockyTripwireConfig,
    val leafBlocks: BlockyLeafConfig,
    val caveVineBlocks: BlockyCaveVineConfig,
    val slabBlocks: BlockySlabConfig,
    val stairBlocks: BlockyStairConfig,
    val disableCustomSounds: Boolean = false,
    val MoreCreativeTabsMod: MoreCreativeTabsModConfig,
) {

    @Serializable
    data class BlockyMenus(
        val defaultMenu: BlockyMenu,
        val blockMenu: BlockyMenu,
        val wireMenu: BlockyMenu,
        val furnitureMenu: BlockyMenu,
    )

    @Serializable
    data class BlockyMenu(val title: String, val height: Int)

    @Serializable
    data class MoreCreativeTabsModConfig(
        val generateJsonForMod: Boolean = false,
    )

    @Serializable
    data class BlockyNoteBlockConfig(
        val isEnabled: Boolean = true,
        val restoreFunctionality: Boolean = false,
    )

    @Serializable
    data class BlockyTripwireConfig(
        val isEnabled: Boolean = true,
    )

    @Serializable
    data class BlockyLeafConfig(
        val isEnabled: Boolean = true,
        val disableBurnForBlockyLeaves: Boolean = false,
        val disableAllLeafDecay: Boolean = false,
        val shouldReserveOnePersistentLeafPerType: Boolean = true, // if true 54 leaf blocks else 63 leaf blocks
    )

    @Serializable
    data class BlockyCaveVineConfig(
        val isEnabled: Boolean = false,
    )

    @Serializable
    data class BlockySlabConfig(
        val isEnabled: Boolean = false,
    )

    @Serializable
    data class BlockyStairConfig(
        val isEnabled: Boolean = false,
    )
}
