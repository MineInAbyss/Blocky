package com.mineinabyss.blocky.systems

import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.World

@Serializable
/*class BlockLocation (
    private var x: Int = 0,
    private var y: Int = 0,
    private var z: Int = 0,
){
    fun getBlockLocation(x: Int, y: Int, z: Int) {
        this.x = x
        this.y = y
        this.z = z
    }

    fun getBlockLocation(loc: Location) {
        x = loc.blockX
        y = loc.blockY
        z = loc.blockZ
    }

    fun BlockLocation(hitboxMap: Map<String, Any>) {
        x = (hitboxMap["x"] as Int)
        y = (hitboxMap["y"] as Int)
        z = (hitboxMap["z"] as Int)
    }

    fun add(loc: Location): Location {
        return loc.clone().add(x.toDouble(), y.toDouble(), z.toDouble())
    }

    fun toLocation(world: World): Location {
        return Location(world, x.toDouble(), y.toDouble(), z.toDouble())
    }

    fun groundRotate(angle: Float): BlockLocation {
        val output = BlockLocation(0,0,0)
        val fixedAngle = 360 - angle
        val radians = Math.toRadians(fixedAngle.toDouble())
        output.x = (cos(radians) * x - sin(radians) * z).roundToInt()
        output.z = (sin(radians) * x - cos(radians) * z).roundToInt()
        if (fixedAngle % 180 > 1) output.z = -output.z
        return output
    }
}*/

class BlockLocation {
    var x: Int
        private set
    var y: Int
        private set
    var z: Int
        private set

    constructor(x: Int, y: Int, z: Int) {
        this.x = x
        this.y = y
        this.z = z
    }

    constructor(location: Location) {
        x = location.blockX
        y = location.blockY
        z = location.blockZ
    }

    constructor(serializedBlockLocation: String) {
        val values = serializedBlockLocation.split(",").toTypedArray()
        x = values[0].toInt()
        y = values[1].toInt()
        z = values[2].toInt()
    }

    constructor(coordinatesMap: Map<String?, Any?>) {
        x = (coordinatesMap["x"] as Int?)!!
        y = (coordinatesMap["y"] as Int?)!!
        z = (coordinatesMap["z"] as Int?)!!
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

    fun toLocation(world: World?): Location {
        return Location(world, x.toDouble(), y.toDouble(), z.toDouble())
    }

    fun groundRotate(angle: Float): BlockLocation {
        val output = BlockLocation(x, y, z)
        val fixedAngle = 360 - angle
        val radians = Math.toRadians(fixedAngle.toDouble())
        output.x = Math.round(Math.cos(radians) * x - Math.sin(radians) * z).toInt()
        output.z = Math.round(Math.sin(radians) * x - Math.cos(radians) * z).toInt()
        if (fixedAngle % 180 > 1) output.z = -output.z
        return output
    }
}