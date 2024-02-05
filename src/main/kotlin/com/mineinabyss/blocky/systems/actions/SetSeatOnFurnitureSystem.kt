package com.mineinabyss.blocky.systems.actions

import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.features.furniture.BlockySeat
import com.mineinabyss.blocky.helpers.FurnitureHelpers
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import org.bukkit.Bukkit
import org.bukkit.entity.ItemDisplay

class SetSeatOnFurnitureSystem : GearyListener() {
    private val Pointers.display by get<ItemDisplay>().on(target)
    private val Pointers.furniture by get<BlockyFurniture>().on(target)
    private val Pointers.seat by get<BlockySeat>().on(source)

    override fun Pointers.handle() {
        val yaw = display.location.yaw
        FurnitureHelpers.clearFurnitureSeats(display)
        Bukkit.getScheduler().scheduleSyncDelayedTask(blocky.plugin, {
            if (furniture.collisionHitbox.isNotEmpty()) {
                FurnitureHelpers.collisionHitboxLocations(yaw, display.location, furniture.collisionHitbox)
                    .forEach { loc -> FurnitureHelpers.spawnFurnitureSeat(display, yaw - 180, seat.heightOffset, loc) }
            } else FurnitureHelpers.spawnFurnitureSeat(display, yaw, seat.heightOffset, display.location)
        }, 1L)
    }
}
