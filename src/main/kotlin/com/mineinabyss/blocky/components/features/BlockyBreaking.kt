package com.mineinabyss.blocky.components.features

import com.mineinabyss.blocky.components.features.mining.ToolType
import com.mineinabyss.idofront.serialization.SerializableItemStack
import com.mineinabyss.idofront.serialization.toSerializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.attribute.AttributeModifier
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlotGroup
import java.util.*

@Serializable
@SerialName("blocky:breaking")
data class BlockyBreaking(
    val hardness: Double,
    val modifiers: BlockyModifiers = BlockyModifiers()
) {
    private fun defaultBlockHardness(block: Block): Double {
        return when (block.type) {
            Material.NOTE_BLOCK -> 0.8
            else -> 1.0
        }
    }

    /**
     * Calculates the AttributeModifier that would correctly change the breaking-speed based on the BlockyBreaking-hardness
     * This method takes into account the base-value of the PLAYER_BLOCK_BREAKING_SPEED attribute, as well as any existing modifiers
     * It then handles it based on the default hardness of the block
     */
    fun createBreakingModifier(player: Player, block: Block): AttributeModifier {
        return AttributeModifier.deserialize(
            mapOf(
                "slot" to EquipmentSlotGroup.HAND,
                "uuid" to UUID.nameUUIDFromBytes(block.toString().toByteArray()).toString(),
                "name" to "blocky:custom_break_speed",
                "operation" to AttributeModifier.Operation.MULTIPLY_SCALAR_1.ordinal,
                "amount" to (defaultBlockHardness(block) / hardness) - 1 + player.blockStateModifiers()
            )
        )
    }

    private fun Player.blockStateModifiers(): Double {
        var modifier = 0.0

        modifier += modifiers.heldTypes.find { it.toolType.contains(inventory.itemInMainHand) }?.value ?: 0.0
        modifier += modifiers.heldItems.find {
            if (it.item.prefab != null) it.item.prefab == inventory.itemInMainHand.toSerializable().prefab
            else it.item.type == inventory.itemInMainHand.type
        }?.value ?: 0.0

        return modifier
    }

    @Serializable
    @SerialName("blocky:modifier")
    data class BlockyModifiers(
        val heldItems: Set<BlockySerializableItemModifier> = setOf(),
        val heldTypes: Set<BlockyToolModifier> = setOf(),
    ) {
        @Serializable
        data class BlockySerializableItemModifier(
            val item: SerializableItemStack,
            val value: Double
        )

        @Serializable
        data class BlockyToolModifier(
            val toolType: ToolType,
            val value: Double
        )
    }
}
