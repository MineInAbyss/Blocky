package com.mineinabyss.blocky.api

import com.jeff_media.morepersistentdatatypes.DataType
import com.mineinabyss.blocky.api.BlockyBlocks.gearyEntity
import com.mineinabyss.blocky.api.events.furniture.BlockyFurnitureBreakEvent
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.core.BlockyFurnitureHitbox
import com.mineinabyss.blocky.components.core.BlockyModelEngine
import com.mineinabyss.blocky.components.features.BlockySeat
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.events.call
import com.mineinabyss.idofront.messaging.broadcastVal
import io.th0rgal.protectionlib.ProtectionLib
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack


object BlockyFurnitures {

    /*val ItemStack.furnitureType: BlockyFurniture.FurnitureType?
        get() = toGearyFromUUIDOrNull()?.get<BlockyFurniture>()?.furnitureType*/

    /*val Entity.furnitureType: BlockyFurniture.FurnitureType?
        get() = toGearyOrNull()?.get<BlockyFurniture>()?.furnitureType*/

    val Entity.furnitureItem: ItemStack? get() {
        return when (this) {
            is ItemFrame -> this.item
            is ArmorStand -> this.equipment.helmet
            else -> null
        }
    }

    val Entity.prefabKey get() = this.toGearyOrNull()?.get<PrefabKey>()

    val Entity.isModelEngineFurniture: Boolean get() = this.toGearyOrNull()?.isModelEngineFurniture ?: false
    val GearyEntity.isModelEngineFurniture: Boolean get() = this.has<BlockyModelEngine>()

    val Block.isFurnitureHitbox: Boolean get() = this.persistentDataContainer.has(FURNITURE_ORIGIN)
    val Entity.isFurnitureHitbox: Boolean get() = this is Interaction && this.toGearyOrNull()?.get<BlockyFurnitureHitbox>()?.baseEntity != null

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
    val Block.blockyFurnitureEntity get() = this.persistentDataContainer.get(FURNITURE_ORIGIN, DataType.LOCATION)?.let { origin ->
        origin.world?.getNearbyEntities(origin, 1.0,1.0,1.0)?.firstOrNull { entity ->
            entity.isBlockyFurniture
        }.broadcastVal("e: ")
    }

    val Block.blockySeat get() = this.world.getNearbyEntities(this.boundingBox.expand(0.4)).firstOrNull {
        it.toGearyOrNull()?.let { g ->
            g.has<BlockySeat>() && !g.has<BlockyFurniture>()
        } ?: false
    }

    fun Entity.removeBlockyFurniture(): Boolean {
        val furniture = (if (isFurnitureHitbox) (this as Interaction).baseFurniture else this) as? ItemDisplay ?: return false
        if (!furniture.isBlockyFurniture) return false
        furniture.interactionEntity?.remove()
        furniture.removeAssosiatedSeats()
        furniture.clearAssosiatedHitboxChunkEntries()
        handleLight.removeBlockLight(furniture.location)
        furniture.remove()
        return true
    }

    //TODO Change to force being ItemDisplay for safety
    fun Entity.removeBlockyFurniture(player: Player): Boolean {
        val furniture = (if (isFurnitureHitbox) (this as Interaction).baseFurniture else this) as? ItemDisplay ?: return false
        if (!furniture.isBlockyFurniture) return false
        val furnitureBreakEvent = BlockyFurnitureBreakEvent(this, player)
        if (!ProtectionLib.canBreak(player, location)) furnitureBreakEvent.isCancelled = true
        if (furnitureBreakEvent.isCancelled) return false
        furnitureBreakEvent.call()

        furniture.interactionEntity?.remove()
        furniture.removeAssosiatedSeats()
        furniture.clearAssosiatedHitboxChunkEntries()
        furniture.handleFurnitureDrops(player)
        handleLight.removeBlockLight(furniture.location)
        furniture.remove()
        return true
    }
}
