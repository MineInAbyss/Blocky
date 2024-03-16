package com.mineinabyss.blocky.systems.actions

import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.features.furniture.BlockySeat
import com.mineinabyss.blocky.helpers.FurnitureHelpers
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import org.bukkit.Bukkit
import org.bukkit.entity.ItemDisplay

fun GearyModule.createFurnitureSeatSetter() = listener(
    object : ListenerQuery() {
        val display by get<ItemDisplay>()
        val furniture by get<BlockyFurniture>()
        val seat by source.get<BlockySeat>()
    }
).exec {
    val yaw = display.location.yaw
    FurnitureHelpers.clearFurnitureSeats(display)
    Bukkit.getScheduler().scheduleSyncDelayedTask(blocky.plugin, {
        if (furniture.collisionHitbox.isNotEmpty()) {
            FurnitureHelpers.collisionHitboxLocations(yaw, display.location, furniture.collisionHitbox)
                .forEach { loc -> FurnitureHelpers.spawnFurnitureSeat(display, loc, yaw - 180, seat.heightOffset) }
        } else FurnitureHelpers.spawnFurnitureSeat(display, display.location, yaw, seat.heightOffset)
    }, 1L)
}
