package com.mineinabyss.blocky.api

import com.mineinabyss.blocky.api.BlockyBlocks.gearyEntity
import com.mineinabyss.blocky.api.events.furniture.BlockyFurnitureBreakEvent
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.core.BlockyFurnitureHitbox
import com.mineinabyss.blocky.components.features.furniture.BlockyAssociatedSeats
import com.mineinabyss.blocky.components.features.furniture.BlockyModelEngine
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.has
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.helpers.prefabs
import com.mineinabyss.idofront.events.call
import io.th0rgal.protectionlib.ProtectionLib
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack


object BlockyFurnitures {

    val Entity.prefabKey get() = this.toGearyOrNull()?.prefabs?.firstOrNull()?.get<PrefabKey>()

    val Entity.isModelEngineFurniture: Boolean get() = this.toGearyOrNull()?.isModelEngineFurniture ?: false
    val GearyEntity.isModelEngineFurniture: Boolean get() = this.has<BlockyModelEngine>()

    val Block.isFurnitureHitbox get() = this.persistentDataContainer.has<BlockyFurnitureHitbox>()
    val Entity.isFurnitureHitbox: Boolean
        get() = this is Interaction && this.toGearyOrNull()?.get<BlockyFurnitureHitbox>()?.baseEntity != null

    val Block.isBlockyFurniture get() = this.gearyEntity?.isBlockyFurniture ?: false
    val GearyEntity.isBlockyFurniture get() = has<BlockyFurniture>() || this.isModelEngineFurniture
    val Entity.isBlockyFurniture: Boolean
        get() = when (this) {
            is ItemDisplay -> this.toGearyOrNull()?.isBlockyFurniture == true
            is Interaction -> isFurnitureHitbox && this.baseFurniture?.toGearyOrNull()?.isBlockyFurniture == true
            else -> false
        } || isModelEngineFurniture

    val ItemStack.blockyFurniture get() = this.decode<BlockyFurniture>()
    val PrefabKey.blockyFurniture get() = this.toEntityOrNull()?.get<BlockyFurniture>()
    val Block.blockyFurniture get() = this.gearyEntity?.get<BlockyFurniture>()

    val Interaction.baseFurniture: ItemDisplay?
        get() = this.toGearyOrNull()?.get<BlockyFurnitureHitbox>()?.baseEntity
    val Block.baseFurniture: ItemDisplay?
        get() = this.persistentDataContainer.decode<BlockyFurnitureHitbox>()?.baseEntity

    val ItemDisplay.interactionEntity: Interaction?
        get() = this.toGearyOrNull()?.get<BlockyFurnitureHitbox>()?.interactionHitbox
    val ItemDisplay.seats: List<Entity>
        get() = this.toGearyOrNull()?.get<BlockyAssociatedSeats>()?.seats ?: emptyList()

    val Block.blockySeat
        get() = this.baseFurniture?.seats?.minByOrNull { it.location.distanceSquared(this.location) }
    val Interaction.blockySeat
        get() = this.baseFurniture?.seats?.minByOrNull { it.location.distanceSquared(this.location) }

    fun placeFurniture(prefabKey: PrefabKey, location: Location, yaw: Float, itemStack: ItemStack) =
        placeBlockyFurniture(prefabKey, location, yaw, itemStack)

    fun placeFurniture(prefabKey: PrefabKey, location: Location, yaw: Float): ItemDisplay? {
        val itemStack = prefabKey.toEntityOrNull()?.get<SetItem>()?.item?.toItemStackOrNull() ?: return null
        return placeBlockyFurniture(prefabKey, location, yaw, itemStack)
    }

    fun removeFurniture(location: Location) {
        location.block.baseFurniture?.let { removeFurniture(it) }
    }

    fun removeFurniture(furniture: ItemDisplay, player: Player? = null): Boolean {
        if (!furniture.isBlockyFurniture) return false
        player?.let {
            val furnitureBreakEvent = BlockyFurnitureBreakEvent(furniture, player)
            if (!ProtectionLib.canBreak(player, furniture.location)) furnitureBreakEvent.isCancelled = true
            furnitureBreakEvent.call()
            if (furnitureBreakEvent.isCancelled) return false
        }

        furniture.interactionEntity?.remove()
        furniture.seats.forEach(Entity::remove)
        furniture.clearAssosiatedHitboxChunkEntries()
        furniture.handleFurnitureDrops(player)
        handleLight.removeBlockLight(furniture.location)
        furniture.remove()
        return true
    }
}
