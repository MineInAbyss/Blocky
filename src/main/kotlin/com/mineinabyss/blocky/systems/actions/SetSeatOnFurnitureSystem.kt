package com.mineinabyss.blocky.systems.actions

import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.features.furniture.BlockySeat
import com.mineinabyss.blocky.helpers.FurnitureHelpers
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.systems.builders.observeWithData
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.Bukkit
import org.bukkit.entity.ItemDisplay

fun GearyModule.createFurnitureSeatSetter() = observeWithData<OnSet>()
    .involving(query<BukkitEntity, BlockyFurniture, BlockySeat>())
    .exec { (entity, furniture, seat) ->
        val display = entity as? ItemDisplay ?: return@exec
        val furniture = furniture
        val seat = seat
        val yaw = display.location.yaw

        FurnitureHelpers.clearFurnitureSeats(display)
        Bukkit.getScheduler().scheduleSyncDelayedTask(blocky.plugin, {
            if (furniture.collisionHitbox.isNotEmpty()) {
                FurnitureHelpers.collisionHitboxLocations(yaw, display.location, furniture.collisionHitbox)
                    .forEach { loc -> FurnitureHelpers.spawnFurnitureSeat(display, loc, yaw - 180, seat.heightOffset) }
            } else FurnitureHelpers.spawnFurnitureSeat(display, display.location, yaw, seat.heightOffset)
        }, 1L)
    }
