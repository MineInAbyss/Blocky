package com.mineinabyss.blocky.systems.actions

import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.features.furniture.BlockySeats
import com.mineinabyss.blocky.helpers.FurnitureHelpers
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.Bukkit
import org.bukkit.entity.ItemDisplay

fun GearyModule.createFurnitureSeatSetter() = observe<OnSet>()
    .involving(query<BukkitEntity, BlockyFurniture, BlockySeats>())
    .exec { (entity, _, seats) ->
        val display = entity as? ItemDisplay ?: return@exec
        val seats = seats

        FurnitureHelpers.clearFurnitureSeats(display)
        Bukkit.getScheduler().scheduleSyncDelayedTask(blocky.plugin, {
            FurnitureHelpers.spawnFurnitureSeat(display, seats)
        }, 1L)
    }
