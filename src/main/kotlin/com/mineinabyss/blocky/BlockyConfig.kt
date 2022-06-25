package com.mineinabyss.blocky

import com.mineinabyss.idofront.config.IdofrontConfig
import kotlinx.serialization.Serializable
import org.bukkit.Sound

object BlockyConfig : IdofrontConfig<BlockyConfig.Data>(blockyPlugin, Data.serializer()) {
    @Serializable
    data class Data(
        val woodPlaceSound: Sound,
        val woodBreakSound: Sound,
        val woodHitSound: Sound,
        val woodStepSound: Sound,
        val woodFallSound: Sound,
    )
}
