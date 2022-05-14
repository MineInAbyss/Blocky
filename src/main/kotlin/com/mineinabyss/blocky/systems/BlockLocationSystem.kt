package com.mineinabyss.blocky.systems

import kotlinx.serialization.Serializable
import org.bukkit.Location
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Serializable
class BlockLocation {
    private var x: Int
    private var y: Int
    private var z: Int

    constructor(x: Int, y: Int, z: Int) {
        this.x = x
        this.y = y
        this.z = z
    }

    override fun toString(): String {
        return "$x,$y,$z"
    }

    fun add(blockLocation: BlockLocation): BlockLocation {
        val output = BlockLocation(x, y, z)
        output.x += blockLocation.x
        output.y += blockLocation.y
        output.z += blockLocation.z
        return output
    }

    fun add(location: Location): Location {
        return location.clone().add(x.toDouble(), y.toDouble(), z.toDouble())
    }

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