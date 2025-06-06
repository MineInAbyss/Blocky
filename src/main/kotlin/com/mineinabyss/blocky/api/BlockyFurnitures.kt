package com.mineinabyss.blocky.api

import com.mineinabyss.blocky.api.events.furniture.BlockyFurnitureBreakEvent
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.features.furniture.BlockyAssociatedSeats
import com.mineinabyss.blocky.components.features.furniture.BlockyModelEngine
import com.mineinabyss.blocky.helpers.FurnitureHelpers
import com.mineinabyss.blocky.helpers.FurnitureHelpers.handleFurnitureDrops
import com.mineinabyss.blocky.helpers.FurniturePacketHelpers
import com.mineinabyss.blocky.helpers.decode
import com.mineinabyss.blocky.helpers.toBlockPos
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.toEntityOrNull
import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.entities.helpers.withGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.papermc.withGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.events.call
import com.nexomc.protectionlib.ProtectionLib
import org.bukkit.GameEvent
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@SuppressWarnings("unused")
object BlockyFurnitures {

    val Entity.prefabKey get() = this.toGearyOrNull()?.prefabs?.firstOrNull()?.get<PrefabKey>()

    val Entity.isModelEngineFurniture: Boolean get() = this.toGearyOrNull()?.isModelEngineFurniture ?: false
    val GearyEntity.isModelEngineFurniture: Boolean get() = this.has<BlockyModelEngine>()

    //TODO toGearyOrNull wouldnt work here as furniture isnt in geary
    val Block.isBlockyFurniture get() = with(world.toGeary()) { toGearyOrNull()?.isBlockyFurniture ?: false }
    val GearyEntity.isBlockyFurniture get() = has<BlockyFurniture>() || this.isModelEngineFurniture
    val Entity.isBlockyFurniture: Boolean
        get() = when (this) {
            is ItemDisplay -> this.toGearyOrNull()?.isBlockyFurniture == true
            else -> false
        } || isModelEngineFurniture
    context(Geary) val String.isBlockyFurniture get() = PrefabKey.of(this).toEntityOrNull()?.isBlockyFurniture ?: false
    context(Geary)  val PrefabKey.isBlockyFurniture get() = this.toEntityOrNull()?.isBlockyFurniture ?: false

    context(Geary) val ItemStack.blockyFurniture get() = this.decode<BlockyFurniture>()
    context(Geary) val PrefabKey.blockyFurniture get() = this.toEntityOrNull()?.get<BlockyFurniture>()

    val Block.blockyFurniture get() = this.baseFurniture?.withGeary { it.toGearyOrNull()?.get<BlockyFurniture>() }

    val Block.baseFurniture: ItemDisplay?
        get() = FurniturePacketHelpers.baseFurnitureFromCollisionHitbox(this.toBlockPos())
    val Interaction.baseFurniture: ItemDisplay?
        get() = FurniturePacketHelpers.baseFurnitureFromInteractionHitbox(this.entityId)

    val ItemDisplay.seats: List<Entity>
        get() = this.toGearyOrNull()?.get<BlockyAssociatedSeats>()?.seats ?: emptyList()

    fun blockySeat(itemDisplay: ItemDisplay, location: Location = itemDisplay.location): Entity? =
        itemDisplay.seats.minByOrNull { it.location.distanceSquared(location) }

    val ItemDisplay.blockySeat
        get() = this.seats.minByOrNull { it.location.distanceSquared(this.location) }

    fun placeFurniture(prefabKey: PrefabKey, location: Location) = placeFurniture(prefabKey, location, location.yaw)

    fun placeFurniture(prefabKey: PrefabKey, location: Location, yaw: Float) =
        location.withGeary { getAddon(ItemTracking).createItem(prefabKey) }?.let { FurnitureHelpers.placeBlockyFurniture(prefabKey, location, yaw, it) }

    fun placeFurniture(prefabKey: PrefabKey, location: Location, yaw: Float, itemStack: ItemStack) =
        FurnitureHelpers.placeBlockyFurniture(prefabKey, location, yaw, itemStack)

    fun removeFurniture(location: Location) = location.block.baseFurniture?.let { removeFurniture(it) }

    fun removeFurniture(location: Location, player: Player) =
        location.block.baseFurniture?.let { removeFurniture(it, player) }

    fun removeFurniture(furniture: ItemDisplay, player: Player? = null): Boolean {
        if (!furniture.isBlockyFurniture) return false

        player?.let {
            val furnitureBreakEvent = BlockyFurnitureBreakEvent(furniture, player)
            if (!ProtectionLib.canBreak(player, furniture.location)) furnitureBreakEvent.isCancelled = true
            furnitureBreakEvent.call()
            if (furnitureBreakEvent.isCancelled) return false
            furniture.handleFurnitureDrops(player)
        }

        return removeFurniture(furniture)
    }

    fun removeFurniture(furniture: ItemDisplay): Boolean {
        if (!furniture.isBlockyFurniture) return false

        furniture.seats.filter { !it.isDead }.forEach(Entity::remove)
        if (!furniture.isDead) furniture.remove()
        furniture.world.sendGameEvent(null, GameEvent.BLOCK_PLACE, furniture.location.toVector())
        return true
    }
}
