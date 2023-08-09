package com.mineinabyss.blocky.api

import com.mineinabyss.blocky.api.events.furniture.BlockyFurnitureBreakEvent
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.core.BlockyFurnitureHitbox
import com.mineinabyss.blocky.components.features.furniture.BlockyAssociatedSeats
import com.mineinabyss.blocky.components.features.furniture.BlockyModelEngine
import com.mineinabyss.blocky.helpers.BlockLight
import com.mineinabyss.blocky.helpers.FurnitureHelpers
import com.mineinabyss.blocky.helpers.GenericHelpers.toEntity
import com.mineinabyss.blocky.helpers.decode
import com.mineinabyss.blocky.helpers.persistentDataContainer
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.has
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.helpers.prefabs
import com.mineinabyss.idofront.events.call
import io.th0rgal.protectionlib.ProtectionLib
import org.bukkit.Location
import org.bukkit.Material
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

    val Block.isFurnitureHitbox get() = (this.type in setOf(Material.BARRIER, Material.PETRIFIED_OAK_SLAB)) && this.persistentDataContainer.has<BlockyFurnitureHitbox>()
    val Entity.isFurnitureHitbox: Boolean
        get() = this is Interaction && this.toGearyOrNull()?.get<BlockyFurnitureHitbox>()?.baseEntity?.toEntity() is ItemDisplay

    //TODO toGearyOrNull wouldnt work here as furniture isnt in geary
    val Block.isBlockyFurniture get() = this.toGearyOrNull()?.isBlockyFurniture ?: false
    val GearyEntity.isBlockyFurniture get() = has<BlockyFurniture>() || this.isModelEngineFurniture
    val Entity.isBlockyFurniture: Boolean
        get() = when (this) {
            is ItemDisplay -> this.toGearyOrNull()?.isBlockyFurniture == true
            is Interaction -> isFurnitureHitbox && this.baseFurniture?.toGearyOrNull()?.isBlockyFurniture == true
            else -> false
        } || isModelEngineFurniture
    val String.isBlockyFurniture get() = PrefabKey.of(this).toEntityOrNull()?.isBlockyFurniture ?: false
    val PrefabKey.isBlockyFurniture get() = this.toEntityOrNull()?.isBlockyFurniture ?: false

    val ItemStack.blockyFurniture get() = this.decode<BlockyFurniture>()
    val PrefabKey.blockyFurniture get() = this.toEntityOrNull()?.get<BlockyFurniture>()
    //TODO toGearyOrNull wouldnt work here as furniture isnt in geary
    val Block.blockyFurniture get() = this.toGearyOrNull()?.get<BlockyFurniture>()

    val Interaction.baseFurniture: ItemDisplay?
        get() = this.toGearyOrNull()?.get<BlockyFurnitureHitbox>()?.baseEntity?.toEntity() as? ItemDisplay
    val Block.baseFurniture: ItemDisplay?
        get() = this.persistentDataContainer.decode<BlockyFurnitureHitbox>()?.baseEntity?.toEntity() as? ItemDisplay

    val ItemDisplay.interactionEntity: Interaction?
        get() = this.toGearyOrNull()?.get<BlockyFurnitureHitbox>()?.interactionHitbox?.toEntity() as? Interaction
    val ItemDisplay.seats: List<Entity>
        get() = this.toGearyOrNull()?.get<BlockyAssociatedSeats>()?.seats ?: emptyList()

    val Block.blockySeat
        get() = this.baseFurniture?.seats?.minByOrNull { it.location.distanceSquared(this.location) }
    val Interaction.blockySeat
        get() = this.baseFurniture?.seats?.minByOrNull { it.location.distanceSquared(this.location) }

    fun placeFurniture(prefabKey: PrefabKey, location: Location) =
        placeFurniture(prefabKey, location, 0f)

    fun placeFurniture(prefabKey: PrefabKey, location: Location, yaw: Float, itemStack: ItemStack) =
        FurnitureHelpers.placeBlockyFurniture(prefabKey, location, yaw, itemStack)

    fun placeFurniture(prefabKey: PrefabKey, location: Location, yaw: Float): ItemDisplay? {
        val itemStack = prefabKey.toEntityOrNull()?.get<SetItem>()?.item?.toItemStackOrNull() ?: return null
        return FurnitureHelpers.placeBlockyFurniture(prefabKey, location, yaw, itemStack)
    }

    fun removeFurniture(location: Location) = location.block.baseFurniture?.let { removeFurniture(it) }

    fun removeFurniture(location: Location, player: Player) {
        location.block.baseFurniture?.let { removeFurniture(it, player) }
    }

    fun removeFurniture(furniture: ItemDisplay, player: Player? = null): Boolean {
        if (!furniture.isBlockyFurniture) return false

        player?.let {
            val furnitureBreakEvent = BlockyFurnitureBreakEvent(furniture, player)
            if (!ProtectionLib.canBreak(player, furniture.location)) furnitureBreakEvent.isCancelled = true
            furnitureBreakEvent.call()
            if (furnitureBreakEvent.isCancelled) return false
            FurnitureHelpers.handleFurnitureDrops(furniture, player)
        }

        return removeFurniture(furniture)
    }

    fun removeFurniture(furniture: ItemDisplay): Boolean {
        if (!furniture.isBlockyFurniture) return false

        furniture.interactionEntity?.remove()
        furniture.seats.forEach(Entity::remove)
        FurnitureHelpers.clearAssosiatedHitboxChunkEntries(furniture)
        BlockLight.removeBlockLight(furniture.location)
        furniture.remove()
        return true
    }
}
