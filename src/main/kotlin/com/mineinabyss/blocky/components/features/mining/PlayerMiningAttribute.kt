package com.mineinabyss.blocky.components.features.mining

import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.serialization.AttributeModifierSerializer
import kotlinx.serialization.Serializable
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player

@Serializable
data class PlayerMiningAttribute(val modifier: @Serializable(AttributeModifierSerializer::class) AttributeModifier) {
    fun addTransientModifier(player: Player) {
        player.getAttribute(Attribute.BLOCK_BREAK_SPEED)?.addTransientModifier(modifier)
    }

    fun removeModifier(player: Player) {
        player.getAttribute(Attribute.BLOCK_BREAK_SPEED)?.removeModifier(modifier)
        player.toGearyOrNull()?.remove<PlayerMiningAttribute>()
    }
}

val Player.miningAttribute get() = toGearyOrNull()?.get<PlayerMiningAttribute>()
