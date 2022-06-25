package com.mineinabyss.blocky

import com.mineinabyss.idofront.config.IdofrontConfig
import kotlinx.serialization.Serializable

object BlockyConfig : IdofrontConfig<BlockyConfig.Data>(blockyPlugin, Data.serializer()) {
    @Serializable
    data class Data(
        val blockSounds: BlockySoundConfig,
        val leafBlocks: BlockyLeafConfig
    )

    @Serializable
    data class BlockySoundConfig(
        val woodPlaceSound: String = "block.stone.place",
        val woodBreakSound: String = "block.stone.break",
        val woodHitSound: String = "block.stone.hit",
        val woodStepSound: String = "block.stone.step",
        val woodFallSound: String = "block.stone.fall",
    )

    @Serializable
    data class BlockyLeafConfig(
        val disableAllLeafDecay: Boolean = false,
        val shouldReserveOnePersistentLeafPerType: Boolean = true, // if true 54 leaf blocks else 63 leaf blocks
    )
}
