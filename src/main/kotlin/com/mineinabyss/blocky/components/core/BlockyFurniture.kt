package com.mineinabyss.blocky.components.core

import com.mineinabyss.blocky.serializers.BrightnessSerializer
import com.mineinabyss.idofront.serialization.SerializableItemStack
import com.mineinabyss.idofront.serialization.Vector3fSerializer
import com.mineinabyss.idofront.serialization.toSerializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.data.type.Slab
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.Display.Brightness
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform
import org.bukkit.inventory.ItemStack
import org.bukkit.util.BoundingBox
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin

@Serializable
@SerialName("blocky:furniture")
data class BlockyFurniture(
    val properties: FurnitureProperties = FurnitureProperties(),
    val rotationType: RotationType = RotationType.VERY_STRICT,
    val collisionHitbox: List<CollisionHitbox> = emptyList(),
    val interactionHitbox: InteractionHitbox = InteractionHitbox(1f, 1f),
) {
    @Serializable
    @SerialName("blocky:interaction_hitbox")
    data class InteractionHitbox(val width: Float, val height: Float, val outline: SerializableItemStack = ItemStack(Material.GLASS).toSerializable()) {
        fun toBoundingBox(location: Location) = BoundingBox.of(location, width.times(0.7), height.times(0.7), width.times(0.7))
    }

    @Serializable
    @SerialName("blocky:collision_hitbox")
    data class CollisionHitbox(val location: BlockLocation, val type: CollisionHitboxType = CollisionHitboxType.FULL)

    @Serializable
    data class BlockLocation(val x: Double, val y: Double, val z: Double) {

        fun add(location: Location) = location.clone().add(x, y, z)

        fun groundRotate(angle: Float): BlockLocation {
            val fixedAngle = 360 - angle
            val radians = Math.toRadians(fixedAngle.toDouble())

            return BlockLocation((cos(radians) * x - sin(radians) * z), y,
                (sin(radians) * x - cos(radians) * z).apply { if (fixedAngle % 180 > 1) this * -1 }.toDouble()
            )
        }
    }

    enum class RotationType {
        NONE, STRICT, VERY_STRICT
    }

    enum class CollisionHitboxType {
        FULL, HALF;

        fun toBlockData(location: Location) = when (this) {
            FULL -> Material.BARRIER.createBlockData()
            HALF -> (Material.PETRIFIED_OAK_SLAB.createBlockData() as Slab).apply {
                type = if (location.y - location.blockY < 0.5) Slab.Type.BOTTOM else Slab.Type.TOP
            }
        }
    }

    @Serializable
    @SerialName("blocky:furniture_properties")
    data class FurnitureProperties(
        val displayTransform: ItemDisplayTransform = ItemDisplayTransform.NONE,
        val scale: @Serializable(Vector3fSerializer::class) Vector3f = Vector3f(1f, 1f, 1f),
        val displayWidth: Float = 0f,
        val displayHeight: Float = 0f,
        val trackingRotation: Billboard = Billboard.FIXED,
        val brightness: @Serializable(BrightnessSerializer::class) Brightness? = null,
        val viewRange: Float? = null,
        val shadowStrength: Float? = null,
        val shadowRadius: Float? = null,
    )

    val hasStrictRotation get() = rotationType != RotationType.NONE
}
