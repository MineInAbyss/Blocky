package com.mineinabyss.blocky.systems

import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.geary.annotations.optin.DangerousComponentOperation
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.features.entities.sounds.Sounds
import com.mineinabyss.geary.papermc.tracking.entities.components.AttemptSpawn
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.geary.systems.builders.observeWithData
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.spawning.spawn
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.entity.ItemDisplay

@OptIn(DangerousComponentOperation::class)
fun GearyModule.createFurnitureSpawner() = observeWithData<AttemptSpawn>()
    .involving(query<BlockyFurniture, BlockyFurniture.Color>())
    .exec { (furniture, color) ->
        val spawnLoc = event.location

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

            color.let { entity.setPersisting(it) }
            entity.set<BukkitEntity>(this)
        }
    }
