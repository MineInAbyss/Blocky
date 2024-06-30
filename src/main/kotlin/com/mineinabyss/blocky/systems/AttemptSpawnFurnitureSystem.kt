package com.mineinabyss.blocky.systems

import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.tracking.entities.components.AttemptSpawn
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.geary.systems.builders.observeWithData
import com.mineinabyss.geary.systems.query.Query
import com.mineinabyss.idofront.spawning.spawn
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.entity.ItemDisplay

fun GearyModule.createFurnitureSpawner() = observeWithData<AttemptSpawn>()
    .exec(object : Query() {
        val furniture by get<BlockyFurniture>()
        val color by get<BlockyFurniture.Color>().orNull()
    }) {
        event.location.spawn<ItemDisplay> {
            val props = it.furniture.properties

            isPersistent = props.persistent
            itemDisplayTransform = props.displayTransform
            displayWidth = props.displayWidth
            displayHeight = props.displayHeight
            brightness = props.brightness
            billboard = props.trackingRotation
            props.viewRange?.let { viewRange = it }
            props.shadowRadius?.let { shadowRadius = it }
            props.shadowStrength?.let { shadowStrength = it }
            transformation = transformation.apply { scale.set(props.scale) }

            it.color?.let { entity.setPersisting(it) }
            entity.set<BukkitEntity>(this)
        }
    }
