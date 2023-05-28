package com.mineinabyss.blocky.compatibility.breaker

import com.mineinabyss.blocky.helpers.prefabKey
import eu.asangarin.breaker.api.IBlockProvider
import org.bukkit.block.Block

object BlockyBlockProvider : IBlockProvider {
    override fun matches(block: Block, configuration: String): Boolean {
        return "blocky_" + block.prefabKey?.full == configuration
    }
}
