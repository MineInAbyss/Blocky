package com.mineinabyss.blocky.api

import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.core.BlockyModelEngine
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.looty.tracking.toGearyFromUUIDOrNull
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.ItemFrame
import org.bukkit.inventory.ItemStack


object BlockyFurnitures {

    val ItemStack.furnitureType: BlockyFurniture.FurnitureType?
        get() = toGearyFromUUIDOrNull()?.get<BlockyFurniture>()?.furnitureType

    val Entity.furnitureType: BlockyFurniture.FurnitureType?
        get() = toGearyOrNull()?.get<BlockyFurniture>()?.furnitureType

    val Entity.furnitureItem: ItemStack? get() {
        return when (this) {
            is ItemFrame -> this.item
            is ArmorStand -> this.equipment.helmet
            else -> null
        }
    }

    val Entity.isModelEngineEntity: Boolean get() = this.toGearyOrNull()?.isModelEngineEntity ?: false
    val GearyEntity.isModelEngineEntity: Boolean get() = this.has<BlockyModelEngine>()
}
