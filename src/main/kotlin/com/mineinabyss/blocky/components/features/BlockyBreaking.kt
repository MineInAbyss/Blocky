package com.mineinabyss.blocky.components.features

import com.mineinabyss.blocky.api.BlockyBlocks.gearyEntity
import com.mineinabyss.blocky.components.core.BlockyInfo
import com.mineinabyss.blocky.components.features.mining.BlockyMining
import com.mineinabyss.blocky.components.features.mining.ToolType
import com.mineinabyss.blocky.helpers.GenericHelpers
import com.mineinabyss.blocky.helpers.gearyInventory
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.helpers.prefabs
import com.mineinabyss.idofront.serialization.DurationSerializer
import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Serializable
@SerialName("blocky:breaking")
data class BlockyBreaking(
    val baseDuration: @Serializable(DurationSerializer::class) Duration = 3.seconds,
    val modifiers: BlockyModifiers = BlockyModifiers()
) {
    fun calculateBreakTime(block: Block, player: Player, hand: EquipmentSlot, itemInHand: ItemStack?): Duration {
        if (block.gearyEntity?.get<BlockyInfo>()?.isUnbreakable == true) return Duration.INFINITE
        if (itemInHand == null || itemInHand.type.isAir) return baseDuration

        if (modifiers.heldItems.isNotEmpty()) {
            val heldPrefab = player.gearyInventory?.get(hand)?.prefabs?.first()?.get<PrefabKey>()
            val modifier = modifiers.heldItems.firstOrNull {
                it.item.prefab?.let { p -> p == heldPrefab?.full } ?: false
                        //TODO This could be improved. isSimilar cares about durability which we don't want though
                        || (it.item.type == itemInHand.type)
            }
            if (modifier != null) return maxOf(baseDuration - modifier.value, Duration.ZERO)
        }
        if (modifiers.heldTypes.isNotEmpty()) {
            val heldTypes = player.gearyInventory?.get(hand)?.get<BlockyMining>()?.toolTypes ?: setOf(GenericHelpers.getVanillaToolTypes(itemInHand))
            val modifier = modifiers.heldTypes.firstOrNull { it.toolType in heldTypes }
            if (modifier != null) return maxOf(baseDuration - modifier.value, Duration.ZERO)
        }

        //TODO: state modifiers

        return baseDuration
    }

    @Serializable
    @SerialName("blocky:modifier")
    data class BlockyModifiers(
        val heldItems: Set<BlockySerializableItemModifier> = setOf(),
        val heldTypes: Set<BlockyToolModifier> = setOf(),
        val states: Set<BlockyStateModifier> = setOf(),
    ) {

        @Serializable
        @SerialName("blocky:serializable_item_modifier")
        data class BlockySerializableItemModifier(val item: SerializableItemStack, val value: @Serializable(
            DurationSerializer::class) Duration
        )

        @Serializable
        @SerialName("blocky:tool_modifier")
        data class BlockyToolModifier(val toolType: ToolType, val value: @Serializable(DurationSerializer::class) Duration)

        @Serializable
        @SerialName("blocky:state_modifier")
        data class BlockyStateModifier(val state: String, val value: @Serializable(DurationSerializer::class) Duration)
    }
}
