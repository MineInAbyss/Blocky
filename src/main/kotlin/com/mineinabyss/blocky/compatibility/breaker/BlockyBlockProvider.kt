package com.mineinabyss.blocky.compatibility.breaker

import com.mineinabyss.blocky.helpers.prefabKey
import com.mineinabyss.idofront.messaging.broadcastVal
import eu.asangarin.breaker.api.IBlockProvider
import org.bukkit.block.Block

object BlockyBlockProvider : IBlockProvider {
    override fun matches(block: Block, configuration: String): Boolean {
        return "BLOCKY_" + block.prefabKey?.full.broadcastVal("test: ") == configuration
    }
}
