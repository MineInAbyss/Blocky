package com.mineinabyss.blocky.systems

import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.helpers.FurniturePacketHelpers
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.accessors.Pointer
import com.mineinabyss.idofront.time.ticks
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox


class FurnitureOutlineSystem : RepeatingSystem(1.ticks) {
    val Pointer.player by get<Player>()

    override fun Pointer.tick() {
        if (!player.isConnected) return

        val location = player.eyeLocation
        val direction = location.direction.clone().multiply(0.1)
        val result = player.rayTraceBlocks(5.0)
        val distanceEyeToRaycastBlock = result?.hitBlock?.let { location.distance(it.location) } ?: (5.0 * 5.0)

        while (location.toBlockLocation().distanceSquared(player.eyeLocation) < distanceEyeToRaycastBlock) {
            location.getNearbyEntities(5.0, 5.0, 5.0).filterIsInstance<ItemDisplay>().firstOrNull {
                it.toGearyOrNull()?.get<BlockyFurniture>()?.interactionHitbox?.let { i ->
                    it.boundingBox.overlaps(i.toBoundingBox(location))
                } == true
            }?.let {
                FurniturePacketHelpers.sendHitboxOutlinePacket(it, player)
                return
            }
            location.add(direction)
        }
        FurniturePacketHelpers.removeHitboxOutlinePacket(player)
    }
}
