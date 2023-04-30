package com.mineinabyss.blocky.api

import com.jeff_media.morepersistentdatatypes.DataType
import com.mineinabyss.blocky.api.BlockyBlocks.gearyEntity
import com.mineinabyss.blocky.api.events.furniture.BlockyFurnitureBreakEvent
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.core.BlockyModelEngine
import com.mineinabyss.blocky.components.features.BlockySeat
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.events.call
import com.mineinabyss.looty.tracking.toGearyFromUUIDOrNull
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
    val Entity.isFurnitureHitbox: Boolean get() = this.vehicle?.isBlockyFurniture == true
    val Block.isBlockyFurniture: Boolean get() = this.gearyEntity?.isBlockyFurniture ?: false
    val Entity.isBlockyFurniture: Boolean get() = this.toGearyOrNull()?.isBlockyFurniture == true || this.isModelEngineFurniture
    val GearyEntity.isBlockyFurniture: Boolean get() = has<BlockyFurniture>() || this.isModelEngineFurniture

    val ItemStack.blockyFurniture get() = this.toGearyFromUUIDOrNull()?.get<BlockyFurniture>()
    val PrefabKey.blockyFurniture get() = this.toEntityOrNull()?.get<BlockyFurniture>()
    val Location.blockyFurniture get() = this.block.gearyEntity?.get<BlockyFurniture>()
    val Block.blockyFurniture get() = this.gearyEntity?.get<BlockyFurniture>()

    val Location.blockyFurnitureEntity get() = this.block.blockyFurnitureEntity
    val Block.blockyFurnitureEntity get() = this.persistentDataContainer.get(FURNITURE_ORIGIN, DataType.LOCATION)?.let { origin ->
        origin.world?.getNearbyEntities(origin.block.boundingBox)?.firstOrNull { entity ->
            entity is ItemDisplay && entity.toGearyOrNull()?.has<BlockyFurniture>() == true
        }
    }

    val Block.blockySeat get() = this.world.getNearbyEntities(this.boundingBox.expand(0.4)).firstOrNull {
        it.toGearyOrNull()?.let { g ->
            g.has<BlockySeat>() && !g.has<BlockyFurniture>()
        } ?: false
    }


    fun Entity.removeBlockyFurniture(player: Player?) {
        this.toGearyOrNull()?.get<BlockyFurniture>() ?: return
        val furnitureBreakEvent = BlockyFurnitureBreakEvent(this, player)
        if (!ProtectionLib.canBreak(player, location)) furnitureBreakEvent.isCancelled = true
        if (furnitureBreakEvent.isCancelled) return
        furnitureBreakEvent.call()

        this.passengers.filterIsInstance<Interaction>().forEach(Interaction::remove)
        this.removeAssosiatedSeats()
        this.clearAssosiatedHitboxChunkEntries()
        handleFurnitureDrops(player)
        handleLight.removeBlockLight(this.location)
        this.toGearyOrNull()?.clear()
        this.remove()
    }
}
