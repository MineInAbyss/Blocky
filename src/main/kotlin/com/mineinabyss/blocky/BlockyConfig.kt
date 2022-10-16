package com.mineinabyss.blocky

import kotlinx.serialization.Serializable

val blockyConfig get() = blockyPlugin.config.data
@Serializable
data class BlockyConfig(
    val noteBlocks: BlockyNoteBlockConfig,
    val tripWires: BlockyTripwireConfig,
    val chorusPlant: BlockyChorusPlantConfig,
    val leafBlocks: BlockyLeafConfig,
    val caveVineBlocks: BlockyCaveVineConfig,
    val disableCustomSounds: Boolean = false,
) {
    @Serializable
    data class BlockyNoteBlockConfig(
        val isEnabled: Boolean = true,
    )

    @Serializable
    data class BlockyTripwireConfig(
        val isEnabled: Boolean = true,
    )

    @Serializable
    data class BlockyChorusPlantConfig(
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
}
