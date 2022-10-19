package com.mineinabyss.blocky.compatibility

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent
import com.mineinabyss.blocky.systems.BlockyTypeQuery
import com.mineinabyss.blocky.systems.BlockyTypeQuery.prefabKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class WorldEditListener : Listener {

    @EventHandler //TODO This will add infinite tabcompletions probably a better way
    fun AsyncTabCompleteEvent.onTabcomplete() {
        if (!buffer.startsWith("//") || !isCommand) return
        val arg = buffer.substringAfterLast(" ").lowercase()

        completions.addAll(BlockyTypeQuery.filter {
            it.prefabKey.key.startsWith(arg) || it.prefabKey.full.startsWith(arg)
        }.map { it.prefabKey.toString() })
    }

}
