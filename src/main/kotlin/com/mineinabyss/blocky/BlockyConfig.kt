package com.mineinabyss.blocky

import com.mineinabyss.idofront.config.IdofrontConfig
import kotlinx.serialization.Serializable

object BlockyConfig : IdofrontConfig<BlockyConfig.Data>(blockyPlugin, Data.serializer()) {
    @Serializable
    data class Data(
        val noteBlocks: BlockyNoteBlockConfig,
        val tripWires: BlockyTripwireConfig,
        val chorusPlant: BlockyChorusPlantConfig,
        val leafBlocks: BlockyLeafConfig,
        val caveVineBlocks: BlockyCaveVineConfig,
        val disableCustomSounds: Boolean = false,
    )

    @Serializable
    data class BlockyNoteBlockConfig(
        val isEnabled: Boolean = true,
        val woodPlaceSound: String = "blocky.wood.place",
        val woodBreakSound: String = "blocky.wood.break",
        val woodHitSound: String = "blocky.wood.hit",
        val woodStepSound: String = "blocky.wood.step",
        val woodFallSound: String = "blocky.wood.fall",
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
