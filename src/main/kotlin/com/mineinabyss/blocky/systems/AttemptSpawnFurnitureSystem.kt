package com.mineinabyss.blocky.systems

import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.papermc.tracking.entities.components.AttemptSpawn
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.idofront.spawning.spawn
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.entity.ItemDisplay

class AttemptSpawnFurnitureSystem : GearyListener() {
    private val Pointers.furniture by get<BlockyFurniture>().on(target)

    //    private val Pointers.color by get<BlockyFurniture.Color>().orNull().on(event)
    private val Pointers.attemptSpawn by get<AttemptSpawn>().on(event)

    val Pointers.family by family {
        not { has<BukkitEntity>() }
    }.on(target)


    @OptIn(UnsafeAccessors::class)
    override fun Pointers.handle() {
        val color = event.entity.get<BlockyFurniture.Color>()
        val spawnLoc = attemptSpawn.location

        spawnLoc.spawn<ItemDisplay> {
            isPersistent = furniture.properties.persistent

            furniture.properties.let { properties ->
                itemDisplayTransform = properties.displayTransform
                displayWidth = properties.displayWidth
                displayHeight = properties.displayHeight
                brightness = properties.brightness
                billboard = properties.trackingRotation
                properties.viewRange?.let { viewRange = it }
                properties.shadowRadius?.let { shadowRadius = it }
                properties.shadowStrength?.let { shadowStrength = it }
                transformation = transformation.apply { scale.set(properties.scale) }
            }
            if (color != null) {
                target.entity.setPersisting(color)
            }
            target.entity.set<BukkitEntity>(this)
//            this.itemStack = furnitureItem
        } ?: return
    }
}
