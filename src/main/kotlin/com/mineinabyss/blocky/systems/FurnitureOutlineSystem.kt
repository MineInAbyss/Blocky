package com.mineinabyss.blocky.systems

import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.helpers.FurniturePacketHelpers
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.systems.builders.system
import com.mineinabyss.geary.systems.query.ListenerQuery
import com.mineinabyss.idofront.time.ticks
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player


fun GearyModule.createFurnitureOutlineSystem() = system(
    object : ListenerQuery() {
        val player by get<Player>()
    }
).every(1.ticks).exec {
    if (!blocky.config.furniture.showHitboxOutline || !player.isConnected) return@exec

    val location = player.eyeLocation
    val direction = location.direction.clone().multiply(0.1)
    val result = player.rayTraceBlocks(5.0)
    val distanceEyeToRaycastBlock = result?.hitBlock?.let { location.distance(it.location) } ?: (5.0 * 5.0)

    while (location.toBlockLocation().distanceSquared(player.eyeLocation) < distanceEyeToRaycastBlock) {
        location.getNearbyEntities(5.0, 5.0, 5.0).filterIsInstance<ItemDisplay>().firstOrNull {
            it.toGearyOrNull()?.get<BlockyFurniture>()?.interactionHitbox?.any { i ->
                it.boundingBox.overlaps(i.toBoundingBox(location))
            } == true
        }?.let {
            FurniturePacketHelpers.sendHitboxOutlinePacket(it, player)
            return@exec
        }
        location.add(direction)
    }
    FurniturePacketHelpers.removeHitboxOutlinePacket(player)

}
