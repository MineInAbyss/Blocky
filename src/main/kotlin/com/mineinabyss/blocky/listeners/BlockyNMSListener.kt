package com.mineinabyss.blocky.listeners

import it.unimi.dsi.fastutil.ints.IntList
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagNetworkSerialization
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class BlockyNMSListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun PlayerJoinEvent.removeDefaultTools() {
        /*if (!blockyConfig.noteBlocks.isEnabled) return
        val packet = ClientboundUpdateTagsPacket(mapOf(Registries.BLOCK. .key() to createPayload(registryTagMap)))
        (player as CraftPlayer).handle.connection.send(packet)*/
    }

    private fun createPayload(map: Map<ResourceLocation, IntList>): TagNetworkSerialization.NetworkPayload {
        return TagNetworkSerialization.NetworkPayload::class.java.declaredConstructors.first()
            .also { it.isAccessible = true }
            .newInstance(map) as TagNetworkSerialization.NetworkPayload
    }
}
