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

    private val Pointers.attemptSpawn by get<AttemptSpawn>().on(event)
    private val Pointers.color by get<BlockyFurniture.Color>().orNull().on(event)

    val Pointers.family by family {
        not { has<BukkitEntity>() }
    }.on(target)


    @OptIn(UnsafeAccessors::class)
    override fun Pointers.handle() {
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

            color?.let { target.entity.setPersisting<BlockyFurniture.Color>(it) }
            target.entity.set<BukkitEntity>(this)
        } ?: return
    }
}
