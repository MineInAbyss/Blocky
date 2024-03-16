package com.mineinabyss.blocky.systems

import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.tracking.entities.components.AttemptSpawn
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import com.mineinabyss.idofront.spawning.spawn
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.entity.ItemDisplay

fun GearyModule.createFurnitureSpawner() = listener(
    object : ListenerQuery() {
        val furniture by get<BlockyFurniture>()
        val attemptSpawn by event.get<AttemptSpawn>()
        val color by event.get<BlockyFurniture.Color>().orNull()

        override fun ensure() = this { not { has<BukkitEntity>() } }
    }
).exec {
    val spawnLoc = attemptSpawn.location

    spawnLoc.spawn<ItemDisplay> {
        val props = furniture.properties

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

        color?.let { entity.setPersisting<BlockyFurniture.Color>(it) }
        entity.set<BukkitEntity>(this)
    }
}
