package com.mineinabyss.blocky.listeners

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.helpers.FurniturePacketHelpers
import com.mineinabyss.idofront.time.ticks
import io.papermc.paper.event.player.PlayerTrackEntityEvent
import io.papermc.paper.event.player.PlayerUntrackEntityEvent
import kotlinx.coroutines.delay
import org.bukkit.entity.ItemDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityTeleportEvent

class BlockyFurniturePacketListener : Listener {

    init {
        // TODO Intercept and cancel update packet if its a barrier hitbox
        // Blocky saends using the section-update so this wont break anything
        /*blocky.plugin.interceptClientbound { packet: Packet<*>, player: Player? ->
            if (player == null || packet !is ClientboundBlockUpdatePacket) return@interceptClientbound packet
            if (packet.pos)
        }*/
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun PlayerTrackEntityEvent.onTrackEntity() {
        val baseEntity = entity as? ItemDisplay ?: return
        blocky.plugin.launch {
            delay(2.ticks)
            FurniturePacketHelpers.sendInteractionHitboxPackets(baseEntity, player)
            FurniturePacketHelpers.sendCollisionHitboxPacket(baseEntity, player)
            FurniturePacketHelpers.sendLightPacket(baseEntity, player)
        }
    }

    @EventHandler
    fun PlayerUntrackEntityEvent.onUntrackEntity() {
        val baseEntity = entity as? ItemDisplay ?: return
        FurniturePacketHelpers.removeInteractionHitboxPacket(baseEntity, player)
        FurniturePacketHelpers.removeHitboxOutlinePacket(baseEntity, player)
        FurniturePacketHelpers.removeCollisionHitboxPacket(baseEntity, player)
        FurniturePacketHelpers.removeLightPacket(baseEntity, player)
    }

    @EventHandler
    fun EntityRemoveFromWorldEvent.onRemoveEntity() {
        val baseEntity = entity as? ItemDisplay ?: return
        FurniturePacketHelpers.removeInteractionHitboxPacket(baseEntity)
        FurniturePacketHelpers.removeHitboxOutlinePacket(baseEntity)
        FurniturePacketHelpers.removeCollisionHitboxPacket(baseEntity)
        FurniturePacketHelpers.removeLightPacket(baseEntity)
    }

    @EventHandler
    fun EntityTeleportEvent.onTeleportEntity() {
        val baseEntity = entity as? ItemDisplay ?: return
        FurniturePacketHelpers.removeInteractionHitboxPacket(baseEntity)
        FurniturePacketHelpers.removeHitboxOutlinePacket(baseEntity)
        FurniturePacketHelpers.removeCollisionHitboxPacket(baseEntity)
        FurniturePacketHelpers.removeLightPacket(baseEntity)

        blocky.plugin.launch {
            delay(2.ticks)
            FurniturePacketHelpers.sendInteractionHitboxPackets(baseEntity)
            FurniturePacketHelpers.sendCollisionHitboxPacket(baseEntity)
            FurniturePacketHelpers.sendLightPacket(baseEntity)
        }
    }


}