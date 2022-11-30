package com.mineinabyss.blocky.systems

import kotlinx.serialization.Serializable
import org.bukkit.Location
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

//TODO Make this more of my own
@Serializable
data class BlockLocation(val x: Int, val y: Int, val z: Int) {

    fun add(location: Location) = location.clone().add(x.toDouble(), y.toDouble(), z.toDouble())

    fun groundRotate(angle: Float): BlockLocation {
        val fixedAngle = 360 - angle
        val radians = Math.toRadians(fixedAngle.toDouble())

        return BlockLocation((cos(radians) * x - sin(radians) * z).roundToInt(), y,
            (sin(radians) * x - cos(radians) * z).roundToInt().apply {
                if (fixedAngle % 180 > 1) this * -1
            })
    }
}
