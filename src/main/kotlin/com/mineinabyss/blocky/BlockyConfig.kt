package com.mineinabyss.blocky

import com.mineinabyss.idofront.config.IdofrontConfig
import kotlinx.serialization.Serializable

object BlockyConfig : IdofrontConfig<BlockyConfig.Data>(blockyPlugin, Data.serializer()) {
    @Serializable
    data class Data(
        val noteBlocks: BlockyNoteBlockConfig,
        val tripWires: BlockyTripwireConfig,
        val chorusPlant: BlockyChorusPlantConfig,
        val leafBlocks: BlockyLeafConfig
    )

    @Serializable
    data class BlockyNoteBlockConfig(
        val isEnabled: Boolean = true,
        val woodPlaceSound: String = "block.stone.place",
        val woodBreakSound: String = "block.stone.break",
        val woodHitSound: String = "block.stone.hit",
        val woodStepSound: String = "block.stone.step",
        val woodFallSound: String = "block.stone.fall",
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
        val disableAllLeafDecay: Boolean = false,
        val shouldReserveOnePersistentLeafPerType: Boolean = true, // if true 54 leaf blocks else 63 leaf blocks
    )
}
