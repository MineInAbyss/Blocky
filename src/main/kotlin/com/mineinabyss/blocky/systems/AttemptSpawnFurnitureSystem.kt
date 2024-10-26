package com.mineinabyss.blocky.systems

import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.observeWithData
import com.mineinabyss.geary.papermc.tracking.entities.components.AttemptSpawn
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.spawning.spawn
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.entity.ItemDisplay

fun Geary.createFurnitureSpawner() = observeWithData<AttemptSpawn>()
    .exec(query<BlockyFurniture, BlockyFurniture.Color?>()) { (furniture, color) ->
        event.location.spawn<ItemDisplay> {
            val properties = furniture.properties

            isPersistent = properties.persistent
            itemDisplayTransform = properties.displayTransform
            displayWidth = properties.displayWidth
            displayHeight = properties.displayHeight
            brightness = properties.brightness
            billboard = properties.trackingRotation
            properties.viewRange?.let { viewRange = it }
            properties.shadowRadius?.let { shadowRadius = it }
            properties.shadowStrength?.let { shadowStrength = it }
            transformation = transformation.apply {
                scale.set(properties.scale)
                translation.set(properties.translation)
            }

            color?.let { entity.setPersisting(it) }
            entity.set<BukkitEntity>(this)
        }
    }
