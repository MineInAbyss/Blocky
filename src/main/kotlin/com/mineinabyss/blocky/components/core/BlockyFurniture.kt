package com.mineinabyss.blocky.components.core

import com.mineinabyss.blocky.serializers.BrightnessSerializer
import com.mineinabyss.blocky.serializers.Vector3fSerializer
import com.mineinabyss.blocky.systems.BlockLocation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Material
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
    val solidHitbox: Boolean = true,
    val originOffset: BlockLocation = BlockLocation(0, 0, 0),
) {
//    enum class FurnitureType {
//        ARMOR_STAND, ITEM_FRAME, GLOW_ITEM_FRAME
//    }

    enum class RotationType {
        NONE, STRICT, VERY_STRICT
    }

    @Serializable
    @SerialName("blocky:furniture_properties")
    data class FurnitureProperties(
        val displayTransform: ItemDisplayTransform = ItemDisplayTransform.NONE,
        val scale: @Serializable(Vector3fSerializer::class) Vector3f = Vector3f(1f, 1f, 1f),
        val width: Float = 1f,
        val height: Float = 1f,
        val viewRange: Float? = null,
        val brightness: @Serializable(BrightnessSerializer::class) Brightness? = null,
        val trackingRotation: Billboard? = null,
        val shadowStrength: Float? = null,
        val shadowRadius: Float? = null,
    )

    val hasStrictRotation get() = rotationType != RotationType.NONE
    val hitboxMaterial get() = if (solidHitbox) Material.BARRIER else Material.OAK_SAPLING
}
