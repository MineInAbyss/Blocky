package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.helpers.createPayload
import com.mineinabyss.blocky.registryTagMap
import net.minecraft.core.Registry
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class BlockyNMSListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun PlayerJoinEvent.removeDefaultTools() {
        val craftPlayer = player as CraftPlayer
        val packet = ClientboundUpdateTagsPacket(mapOf(Registry.BLOCK_REGISTRY to createPayload(registryTagMap)))
        craftPlayer.handle.connection.send(packet)
    }
}
