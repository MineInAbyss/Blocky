package com.mineinabyss.blocky.components.core

import com.mineinabyss.blocky.serializers.BrightnessSerializer
import com.mineinabyss.idofront.serialization.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.Display.Brightness
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform
import org.bukkit.inventory.ItemStack
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin

@Serializable
@SerialName("blocky:furniture")
data class BlockyFurniture(
    val properties: FurnitureProperties = FurnitureProperties(),
    val rotationType: RotationType = RotationType.VERY_STRICT,
    val collisionHitbox: Set<CollisionHitbox> = emptySet(),
    val interactionHitbox: Set<InteractionHitbox> = setOf(InteractionHitbox(width = 1f, height = 1f)).takeIf { collisionHitbox.isEmpty() } ?: emptySet(),
) {
    @Serializable
    @SerialName("blocky:interaction_hitbox")
    data class InteractionHitbox(
        val offset: @Serializable(VectorSerializer::class) Vector = Vector(),
        val width: Float = 1f,
        val height: Float = 1f,
        val outline: SerializableItemStack = ItemStack(Material.GLASS).toSerializable()
    ) {

        fun toBoundingBox(location: Location) = BoundingBox.of(location, width.times(0.7), height.times(0.7), width.times(0.7))
        fun location(furniture: ItemDisplay): Location {
            return furniture.location.add(offset(furniture.yaw))
        }

        fun offset(furnitureYaw: Float): Vector {
            val angleRad = Math.toRadians(furnitureYaw.toDouble())

            // Get the coordinates relative to the local y-axis
            val x = cos(angleRad) * offset.x + sin(angleRad) * offset.z
            val y = offset.y
            val z = sin(angleRad) * offset.x + cos(angleRad) * offset.z

            return Vector(x, y, z)
        }
    }

    @JvmInline
    @Serializable
    @SerialName("blocky:collision_hitbox")
    value class CollisionHitbox(val location: BlockLocation)

    @Serializable
    class BlockLocation(val x: Int = 0, val y: Int = 0, val z: Int = 0) {

        fun add(location: Location) = location.clone().add(x.toDouble(), y.toDouble(), z.toDouble())

        fun groundRotate(angle: Float): BlockLocation {
            val fixedAngle = 360 - angle
            val radians = Math.toRadians(fixedAngle.toDouble())

            return BlockLocation(
                (round(cos(radians) * x - sin(radians) * z).toInt()),
                y,
                (round(sin(radians) * x - cos(radians) * z).toInt()).let { it.takeUnless { fixedAngle % 180 > 1 } ?: (it * -1) }
            )
        }
    }

    enum class RotationType {
        NONE, STRICT, VERY_STRICT
    }

    @Serializable
    @SerialName("blocky:furniture_properties")
    data class FurnitureProperties(
        val persistent: Boolean = true,
        val itemStack: SerializableItemStack? = null,
        val displayTransform: ItemDisplayTransform = ItemDisplayTransform.NONE,
        val scale: @Serializable(Vector3fSerializer::class) Vector3f = Vector3f(1f, 1f, 1f),
        val translation: @Serializable(Vector3fSerializer::class) Vector3f = Vector3f(),
        val displayWidth: Float = 0f,
        val displayHeight: Float = 0f,
        val trackingRotation: Billboard = Billboard.FIXED,
        val brightness: @Serializable(BrightnessSerializer::class) Brightness? = null,
        val viewRange: Float? = null,
        val shadowStrength: Float? = null,
        val shadowRadius: Float? = null,
    )

    val hasStrictRotation get() = rotationType != RotationType.NONE

    @Serializable
    @SerialName("blocky:prevent_itemstack_update")
    data class PreventItemStackUpdate(
        val forceWhenDifferentMaterial: Boolean = true,
    )

    @Serializable
    @SerialName("blocky:furniture_color")
    data class Color(val color: @Serializable(with = ColorSerializer::class) org.bukkit.Color)
}
