package com.mineinabyss.blocky.systems

import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.helpers.FurniturePacketHelpers
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.systems.builders.system
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.time.ticks
import net.minecraft.world.entity.EntitySelector
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import java.util.*


fun GearyModule.createFurnitureOutlineSystem() =
    system(query<Player>()).every(1.ticks).exec { (player) ->
        if (blocky.config.furniture.showOutlines && player.isConnected) findTargetFurnitureHitbox(player, 5.0)?.let {
            FurniturePacketHelpers.sendHitboxOutlinePacket(it, player)
        } ?: FurniturePacketHelpers.removeHitboxOutlinePacket(player)
    }

private fun findTargetFurnitureHitbox(player: Player, maxDistance: Double): ItemDisplay? {
    if (maxDistance < 1 || maxDistance > 120) return null
    val nmsPlayer = (player as CraftPlayer).handle
    val start = nmsPlayer.getEyePosition(1.0f)
    val direction = nmsPlayer.lookAngle
    val distanceDirection = Vec3(direction.x * maxDistance, direction.y * maxDistance, direction.z * maxDistance)
    val end = start.add(distanceDirection)
    val entities = nmsPlayer.level().getEntities(
        nmsPlayer,
        nmsPlayer.boundingBox.expandTowards(distanceDirection).inflate(1.0, 1.0, 1.0),
        EntitySelector.NO_SPECTATORS
    )
    var distance = 0.0
    val entityIterator: Iterator<net.minecraft.world.entity.Entity> = entities.iterator()

    var baseEntity: ItemDisplay? = null
    while (true) {
        var entity: net.minecraft.world.entity.Entity
        var rayTrace: Vec3
        var distanceTo: Double
        do {
            var rayTraceResult: Optional<Vec3> = Optional.empty()
            do {
                if (!entityIterator.hasNext()) return baseEntity

                entity = entityIterator.next()
                val bukkitEntity = entity.bukkitEntity as? ItemDisplay ?: continue
                // If entity is furniture, check all interactionHitboxes if their "bounding box" is colliding
                bukkitEntity.toGearyOrNull()?.get<BlockyFurniture>()?.interactionHitbox?.firstOrNull { hitbox ->
                    val hitboxLoc = hitbox.location(bukkitEntity).add(0.0, hitbox.height / 2.0, 0.0)
                    val hitboxVec = Vec3(hitboxLoc.x(), hitboxLoc.y(), hitboxLoc.z())
                    val hitboxAABB = AABB.ofSize(
                        hitboxVec,
                        hitbox.width.toDouble(),
                        hitbox.height.toDouble(),
                        hitbox.width.toDouble()
                    )
                    rayTraceResult = hitboxAABB.clip(start, end)
                    rayTraceResult.isPresent
                }
            } while (rayTraceResult.isEmpty)

            rayTrace = rayTraceResult.get()
            distanceTo = start.distanceToSqr(rayTrace)
        } while (!(distanceTo < distance) && distance != 0.0)

        baseEntity = entity.bukkitEntity as? ItemDisplay
        distance = distanceTo
    }
}
