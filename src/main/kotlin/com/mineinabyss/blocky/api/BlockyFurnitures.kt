package com.mineinabyss.blocky.api

import com.mineinabyss.blocky.api.BlockyBlocks.gearyEntity
import com.mineinabyss.blocky.api.events.furniture.BlockyFurnitureBreakEvent
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.core.BlockyFurnitureHitbox
import com.mineinabyss.blocky.components.features.furniture.BlockyModelEngine
import com.mineinabyss.blocky.components.features.furniture.BlockySeat
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
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

    val Block.isFurnitureHitbox: Boolean get() = this.persistentDataContainer.has(FURNITURE_ORIGIN)
    val Entity.isFurnitureHitbox: Boolean
        get() = this is Interaction && this.toGearyOrNull()?.get<BlockyFurnitureHitbox>()?.baseEntity != null

    val Block.isBlockyFurniture: Boolean get() = this.gearyEntity?.isBlockyFurniture ?: false
    val GearyEntity.isBlockyFurniture: Boolean get() = has<BlockyFurniture>() || this.isModelEngineFurniture
    val Entity.isBlockyFurniture: Boolean
        get() = when (this) {
            is ItemDisplay -> this.toGearyOrNull()?.isBlockyFurniture == true
            is Interaction -> isFurnitureHitbox && this.baseFurniture?.toGearyOrNull()?.isBlockyFurniture == true
            else -> false
        } || isModelEngineFurniture

    val ItemStack.blockyFurniture get() = this.decode<BlockyFurniture>()
    val PrefabKey.blockyFurniture get() = this.toEntityOrNull()?.get<BlockyFurniture>()
    val Location.blockyFurniture get() = this.block.gearyEntity?.get<BlockyFurniture>()
    val Block.blockyFurniture get() = this.gearyEntity?.get<BlockyFurniture>()

    val Location.blockyFurnitureEntity get() = this.block.blockyFurnitureEntity
    val Block.blockyFurnitureEntity get() = this.persistentDataContainer.decode<BlockyFurnitureHitbox>()?.baseEntity

    val Block.blockySeat
        get() = this.world.getNearbyEntities(this.boundingBox.expand(0.4)).firstOrNull {
            it.toGearyOrNull()?.let { g ->
                g.has<BlockySeat>() && !g.has<BlockyFurniture>()
            } ?: false
        }

    val Interaction.blockySeat
        get() = this.world.getNearbyEntities(this.boundingBox.expand(0.4)).firstOrNull {
            it.toGearyOrNull()?.let { g ->
                g.has<BlockySeat>() && !g.has<BlockyFurniture>()
            } ?: false
        }

    fun placeFurniture(prefabKey: PrefabKey, location: Location, yaw: Float, itemStack: ItemStack) =
        placeBlockyFurniture(prefabKey, location, yaw, itemStack)

    fun placeFurniture(prefabKey: PrefabKey, location: Location, yaw: Float) =
        placeBlockyFurniture(prefabKey, location, yaw)

    fun removeFurniture(location: Location) {
        location.blockyFurnitureEntity?.let { removeFurniture(it) }
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
        furniture.removeAssosiatedSeats()
        furniture.clearAssosiatedHitboxChunkEntries()
        furniture.handleFurnitureDrops(player)
        handleLight.removeBlockLight(furniture.location)
        furniture.remove()
        return true
    }
}
