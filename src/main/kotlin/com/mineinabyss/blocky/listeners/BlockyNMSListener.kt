package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.helpers.createPayload
import com.mineinabyss.blocky.registryTagMap
import net.minecraft.core.Registry
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class BlockyNMSListener : Listener {
    @EventHandler
    fun PlayerJoinEvent.removeDefaultTools() {
        val packet = ClientboundUpdateTagsPacket(mapOf(Registry.BLOCK_REGISTRY to createPayload(registryTagMap)))
        (player as CraftPlayer).handle.connection.send(packet)
    }
}
