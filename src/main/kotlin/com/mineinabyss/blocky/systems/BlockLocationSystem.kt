package com.mineinabyss.blocky.systems

import kotlinx.serialization.Serializable
import org.bukkit.Location
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

//TODO Make this more of my own
@Serializable
class BlockLocation(
    var x: Int,
    var y: Int,
    var z: Int,
) {

    override fun toString(): String {
        return "$x,$y,$z"
    }

    fun add(blockLocation: BlockLocation) =
        BlockLocation(x, y, z).apply {
            x += blockLocation.x
            y += blockLocation.y
            z += blockLocation.z
        }

    fun add(location: Location) = location.clone().add(x.toDouble(), y.toDouble(), z.toDouble())

    fun groundRotate(angle: Float): BlockLocation {
        val output = BlockLocation(x, y, z)
        val fixedAngle = 360 - angle
        val radians = Math.toRadians(fixedAngle.toDouble())

        output.x = (cos(radians) * x - sin(radians) * z).roundToInt()
        output.z = (sin(radians) * x - cos(radians) * z).roundToInt()
        if (fixedAngle % 180 > 1) output.z = -output.z
        return output
    }
}
