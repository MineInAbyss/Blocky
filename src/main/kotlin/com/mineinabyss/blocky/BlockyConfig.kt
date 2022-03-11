package com.mineinabyss.blocky

import com.mineinabyss.idofront.config.IdofrontConfig
import kotlinx.serialization.Serializable

object BlockyConfig : IdofrontConfig<BlockyConfig.Data>(blockyPlugin, Data.serializer()) {
    @Serializable
    data class Data(
        val debug: Boolean = false
    )
}