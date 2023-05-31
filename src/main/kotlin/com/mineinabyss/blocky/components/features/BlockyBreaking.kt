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
import org.bukkit.Material
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
    fun calculateBreakTime(block: Block, player: Player, hand: EquipmentSlot, heldItem: ItemStack?): Duration {
        val itemInHand = heldItem ?: ItemStack(Material.AIR)
        var duration = baseDuration
        if (block.gearyEntity?.get<BlockyInfo>()?.isUnbreakable == true) return Duration.INFINITE

        if (modifiers.heldItems.isNotEmpty()) {
            val heldPrefab = player.gearyInventory?.get(hand)?.prefabs?.first()?.get<PrefabKey>()
            val modifier = modifiers.heldItems.firstOrNull {
                it.item.prefab?.let { p -> p == heldPrefab?.full } ?: false
                        //TODO This could be improved. isSimilar cares about durability which we don't want though
                        || (it.item.type == itemInHand.type)
            }
            if (modifier != null) duration = maxOf(duration - modifier.value, Duration.ZERO)
        }

        if (modifiers.heldTypes.isNotEmpty()) {
            val heldTypes = player.gearyInventory?.get(hand)?.get<BlockyMining>()?.toolTypes ?: setOf(GenericHelpers.getVanillaToolTypes(itemInHand))
            val modifier = modifiers.heldTypes.firstOrNull { it.toolType in heldTypes }
            if (modifier != null) duration = maxOf(duration - modifier.value, Duration.ZERO)
        }

        //TODO: state modifiers
        /*if (player.activePotionEffects.any { it.type == PotionEffectType.FAST_DIGGING })
            duration = duration.times(0.2 * (player.activePotionEffects.first { it.type == PotionEffectType.FAST_DIGGING }.amplifier))
        if (player.isInWater && player.)*/

        return duration
    }

    @Serializable
    @SerialName("blocky:modifier")
    data class BlockyModifiers(
        val heldItems: Set<BlockySerializableItemModifier> = setOf(),
        val heldTypes: Set<BlockyToolModifier> = setOf(),
        val states: Set<BlockyStateModifier> = setOf(),
    ) {
        @Serializable data class BlockySerializableItemModifier(val item: SerializableItemStack, val value: @Serializable(DurationSerializer::class) Duration)
        @Serializable data class BlockyToolModifier(val toolType: ToolType, val value: @Serializable(DurationSerializer::class) Duration)
        @Serializable data class BlockyStateModifier(val state: BlockyStateType, val value: @Serializable(DurationSerializer::class) Duration, val operation: Operation = Operation.SUBTRACT)

        enum class Operation {
            ADD, SUBTRACT, MULTIPLY, DIVIDE
        }
        enum class BlockyStateType {
            HASTE, MINING_FATIGUE, IN_WATER, IN_WATER_NO_AFFINITY, NOT_ON_GROUND, IS_SNEAKING
        }
    }
}
