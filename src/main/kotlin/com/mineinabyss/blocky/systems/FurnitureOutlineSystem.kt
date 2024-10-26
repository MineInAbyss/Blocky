package com.mineinabyss.blocky.systems

import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.helpers.FurniturePacketHelpers
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.time.ticks
import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.bukkit.attribute.Attribute
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import kotlin.jvm.optionals.getOrNull


fun Geary.createFurnitureOutlineSystem() =
    system(query<Player>()).every(4.ticks).exec { (player) ->
        if (blocky.config.furniture.showOutlines && player.isConnected) findTargetFurnitureHitbox(player)?.let {
            FurniturePacketHelpers.sendHitboxOutlinePacket(it, player)
        } ?: FurniturePacketHelpers.removeHitboxOutlinePacket(player)
    }

private fun findTargetFurnitureHitbox(player: Player): ItemDisplay? {
    val maxReachDistance = player.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE)?.value?.takeIf { it in 1.0..120.0 } ?: return null
    val nmsPlayer = (player as CraftPlayer).handle
    val (start, direction) = nmsPlayer.getEyePosition(1.0f) to nmsPlayer.lookAngle
    val distanceDirection = direction.scale(maxReachDistance)
    val end = start.add(distanceDirection)

    val aabb = nmsPlayer.boundingBox.expandTowards(distanceDirection).inflate(1.0)
    val entities = nmsPlayer.level().getEntities(nmsPlayer, aabb) { it.type == EntityType.ITEM_DISPLAY }

    var closestEntity: ItemDisplay? = null
    var closestDistance = Double.MAX_VALUE

    entities.asSequence().mapNotNull { it.bukkitEntity as? ItemDisplay }.forEach { bukkitEntity ->
        bukkitEntity.toGearyOrNull()?.get<BlockyFurniture>()?.interactionHitbox?.forEach { hitbox ->
            val (width, height) = hitbox.width.toDouble() to hitbox.height.toDouble()
            val hitboxCenter = hitbox.location(bukkitEntity).add(0.0, hitbox.height / 2.0, 0.0).let { Vec3(it.x, it.y, it.z) }
            AABB.ofSize(hitboxCenter, width, height, width).clip(start, end).getOrNull()?.let { rayTrace ->
                val distanceTo = start.distanceToSqr(rayTrace)
                if (distanceTo < closestDistance) {
                    closestDistance = distanceTo
                    closestEntity = bukkitEntity
                }
            }
        }
    }

    return closestEntity
}
