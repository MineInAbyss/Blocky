package com.mineinabyss.blocky.systems

import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.helpers.FurniturePacketHelpers
import com.mineinabyss.blocky.helpers.GenericHelpers.toBlockCenterLocation
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.accessors.Pointer
import com.mineinabyss.idofront.time.ticks
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector


class FurnitureOutlineSystem : RepeatingSystem(1.ticks) {
    val Pointer.player by get<Player>()

    override fun Pointer.tick() {
        if (!player.isConnected) return
        val entities = player.getNearbyEntities(8.0, 8.0, 8.0).filterIsInstance<ItemDisplay>().toList()
        val playerDirection: Vector = player.location.getDirection().normalize()
        val boundingEntities = entities.map {
            val hitbox = it.toGearyOrNull()?.get<BlockyFurniture>()?.interactionHitbox ?: return
            val loc = it.location.toBlockCenterLocation().apply { y += hitbox.height / 2 }
            it to BoundingBox.of(
                loc,
                (hitbox.width / 2).toDouble(),
                (hitbox.height / 2).toDouble(),
                (hitbox.width / 2).toDouble()
            )
        }

        var distance = 0.0
        while (distance <= 5.0) {
            val point = player.eyeLocation.clone().add(player.location.getDirection().clone().multiply(distance))
            boundingEntities.forEach {
                if (point.toVector() !in it.second) return@forEach FurniturePacketHelpers.removeHitboxOutlinePacket(it.first, player)
                FurniturePacketHelpers.sendHitboxOutlinePacket(it.first, player)
                distance = 5.0
            }

            distance += 0.2
        }

//        player.getLineOfSight(null, 5).filter { it.type.isAir }.toList().forEach entity@{ block ->
//            entities.forEach { itemDisplay ->
//                if (itemDisplay.location.distance(player.eyeLocation) > 10.0) return@entity FurniturePacketHelpers.removeHitboxOutlinePacket(itemDisplay, player)
//                if (!itemDisplay.boundingBox.overlaps(BoundingBox.of(block, block.getRelative(BlockFace.UP)))) return@entity FurniturePacketHelpers.removeHitboxOutlinePacket(itemDisplay, player)
//
//                FurniturePacketHelpers.sendHitboxOutlinePacket(itemDisplay, player)
//            }
//        }
    }
}
