package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.registryTagMap
import it.unimi.dsi.fastutil.ints.IntList
import net.minecraft.core.registries.Registries
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagNetworkSerialization
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class BlockyNMSListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun PlayerJoinEvent.removeDefaultTools() {
        if (!blocky.config.noteBlocks.isEnabled) return
        val packet = ClientboundUpdateTagsPacket(mapOf(Registries.BLOCK to createPayload(registryTagMap)))
        (player as CraftPlayer).handle.connection.send(packet)
    }

    private fun createPayload(map: Map<ResourceLocation, IntList>): TagNetworkSerialization.NetworkPayload {
        return TagNetworkSerialization.NetworkPayload::class.java.declaredConstructors.first()
            .also { it.isAccessible = true }
            .newInstance(map) as TagNetworkSerialization.NetworkPayload
    }
}
