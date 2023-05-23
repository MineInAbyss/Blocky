package com.mineinabyss.blocky.components.core

import com.mineinabyss.blocky.serializers.BrightnessSerializer
import com.mineinabyss.blocky.serializers.Vector3fSerializer
import com.mineinabyss.blocky.systems.BlockLocation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.Display.Brightness
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform
import org.joml.Vector3f

@Serializable
@SerialName("blocky:furniture")
data class BlockyFurniture(
    //val furnitureType: FurnitureType,
    val properties: FurnitureProperties = FurnitureProperties(),
    val rotationType: RotationType = RotationType.VERY_STRICT,
    val collisionHitbox: List<BlockLocation> = emptyList(),
    val interactionHitbox: InteractionHitbox = InteractionHitbox(1f, 1f),
    val originOffset: BlockLocation = BlockLocation(0, 0, 0),
) {
    @Serializable
    @SerialName("blocky:interaction_hitbox")
    data class InteractionHitbox(val width: Float, val height: Float)

    enum class RotationType {
        NONE, STRICT, VERY_STRICT
    }

    @Serializable
    @SerialName("blocky:furniture_properties")
    data class FurnitureProperties(
        val displayTransform: ItemDisplayTransform = ItemDisplayTransform.NONE,
        val scale: @Serializable(Vector3fSerializer::class) Vector3f? = null,
        val displayWidth: Float = 0f,
        val displayHeight: Float = 0f,
        val viewRange: Float? = null,
        val brightness: @Serializable(BrightnessSerializer::class) Brightness? = null,
        val trackingRotation: Billboard? = null,
        val shadowStrength: Float? = null,
        val shadowRadius: Float? = null,
    )

    val hasStrictRotation get() = rotationType != RotationType.NONE
}
