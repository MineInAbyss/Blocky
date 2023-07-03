package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.api.BlockyFurnitures.removeFurniture
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.core.BlockyFurnitureHitbox
import com.mineinabyss.blocky.components.features.BlockyDrops
import com.mineinabyss.blocky.components.features.BlockyLight
import com.mineinabyss.blocky.components.features.furniture.BlockyAssociatedSeats
import com.mineinabyss.blocky.components.features.furniture.BlockyModelEngine
import com.mineinabyss.blocky.components.features.furniture.BlockySeat
import com.mineinabyss.blocky.helpers.GenericHelpers.toBlockCenterLocation
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.helpers.addPrefab
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.plugin.Plugins
import com.mineinabyss.idofront.spawning.spawn
import com.ticxo.modelengine.api.ModelEngineAPI
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Rotation
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.inventory.meta.PotionMeta
import org.joml.Vector3f
import kotlin.math.max

object FurnitureHelpers {
    fun getTargetBlock(placedAgainst: Block, blockFace: BlockFace): Block? {

        return if (placedAgainst.isReplaceable) placedAgainst else {
            val target = placedAgainst.getRelative(blockFace)
            if (!target.type.isAir && target.isReplaceable) null else target
        }
    }

    fun getLocations(
        rotation: Float,
        center: Location,
        hitbox: List<BlockyFurniture.CollisionHitbox>
    ): Map<BlockyFurniture.CollisionHitboxType, List<Location>> =
        BlockyFurniture.CollisionHitboxType.values().associateWith {
            hitbox.filter { c -> c.type == it }.map { c -> c.location.groundRotate(rotation).add(center) }
        }

    fun getRotation(yaw: Float, nullFurniture: BlockyFurniture?): Rotation {
        val furniture = nullFurniture ?: BlockyFurniture()
        val rotationDegree = if (furniture.rotationType == BlockyFurniture.RotationType.STRICT) 0 else 1
        val id = (((Location.normalizeYaw(yaw) + 180) * 8 / 360 + 0.5).toInt() % 8).apply {
            if (furniture.hasStrictRotation && this % 2 != 0) this - rotationDegree
        }
        return Rotation.values()[id]
    }

    fun getYaw(rotation: Rotation) = listOf(*Rotation.values()).indexOf(rotation) * 360f / 8f

    fun hasEnoughSpace(blockyFurniture: BlockyFurniture, loc: Location, yaw: Float): Boolean {
        return if (blockyFurniture.collisionHitbox.isEmpty()) true
        else blockyFurniture.collisionHitbox.let {
            getLocations(yaw, loc, it).values.flatten().stream().allMatch { adjacent -> adjacent.block.type.isAir }
        }
    }


    internal fun placeBlockyFurniture(
        prefabKey: PrefabKey,
        loc: Location,
        yaw: Float = loc.yaw,
        item: ItemStack?
    ): ItemDisplay? {
        val gearyEntity = prefabKey.toEntityOrNull() ?: return null
        val itemStack = item ?: gearyEntity.get<SetItem>()?.item?.toItemStack() ?: return null
        val furniture = gearyEntity.get<BlockyFurniture>() ?: return null
        val furnitureItem = itemStack.clone().editItemMeta {
            displayName(Component.empty())
            (this as? LeatherArmorMeta)?.setColor((itemStack.itemMeta as? LeatherArmorMeta)?.color)
                ?: (this as? PotionMeta)?.setColor((itemStack.itemMeta as? PotionMeta)?.color)
                ?: (this as? MapMeta)?.setColor((itemStack.itemMeta as? MapMeta)?.color) ?: return@editItemMeta
        }

        val newFurniture = loc.toBlockCenterLocation().spawn<ItemDisplay> {
            isPersistent = true
            setRotation(yaw, 0F)

            furniture.properties.let { p ->
                itemDisplayTransform = p.displayTransform
                displayWidth = p.displayWidth
                displayHeight = p.displayHeight
                p.brightness?.let { brightness = it }
                p.trackingRotation?.let { billboard = it }
                p.viewRange?.let { viewRange = it }
                p.shadowRadius?.let { shadowRadius = it }
                p.shadowStrength?.let { shadowStrength = it }

                val isFixed: Boolean = itemDisplayTransform == ItemDisplay.ItemDisplayTransform.FIXED
                transformation = transformation.apply {
                    scale.set(p.scale ?: if (isFixed) Vector3f(0.5f, 0.5f, 0.5f) else Vector3f(1f, 1f, 1f))
                }

                if (itemDisplayTransform == ItemDisplay.ItemDisplayTransform.NONE) teleportAsync(location.toCenterLocation())
                if (isFixed) setRotation(getYaw(getRotation(yaw, furniture)) - 180, -90f)
                else setRotation(getYaw(getRotation(yaw, furniture)), 0f)
            }
            this.itemStack = furnitureItem
        } ?: return null

        // Spawn Interaction Entity if defined and remove both entities if it fails
        val interaction = furniture.interactionHitbox?.let {
            newFurniture.location.toBlockCenterLocation().spawn<Interaction> {
                isPersistent = true
                interactionWidth = furniture.interactionHitbox.width
                interactionHeight = furniture.interactionHitbox.height
            } ?: run {
                removeFurniture(newFurniture)
                return null
            }
        }

        newFurniture.toGeary().apply {
            addPrefab(gearyEntity)
            interaction?.let { setPersisting(BlockyFurnitureHitbox(_interactionHitbox = interaction.uniqueId)) }
        }
        interaction?.toGeary()?.setPersisting(BlockyFurnitureHitbox(_baseEntity = newFurniture.uniqueId))

        gearyEntity.get<BlockyModelEngine>()?.let { meg ->
            if (!Plugins.isEnabled<ModelEngineAPI>()) return@let
            val activeModel = ModelEngineAPI.createActiveModel(meg.modelId) ?: return@let
            ModelEngineAPI.getOrCreateModeledEntity(newFurniture).apply {
                addModel(activeModel, false)
                isBaseEntityVisible = false
                isModelRotationLock = true
            }
        }

        newFurniture.toGeary().let { newGeary ->
            if (furniture.collisionHitbox.isNotEmpty()) {
                newGeary.placeFurnitureHitbox(newFurniture, newFurniture.location.yaw)
            } else if (furniture.interactionHitbox != null) {
                newGeary.get<BlockyLight>()?.lightLevel?.let { BlockLight.createBlockLight(loc, it) }
                newGeary.get<BlockySeat>()?.let { spawnFurnitureSeat(newFurniture, yaw) }
            } else newGeary.get<BlockyLight>()?.lightLevel?.let { BlockLight.createBlockLight(loc, it) }
        }

        return newFurniture
    }

    private fun GearyEntity.placeFurnitureHitbox(baseEntity: ItemDisplay, yaw: Float) {
        val furniture = get<BlockyFurniture>() ?: return
        val locations = getLocations(yaw, baseEntity.location, furniture.collisionHitbox)

        locations.forEach { (type, locs) ->
            locs.forEach { loc ->
                loc.block.setBlockData(type.toBlockData(loc), false)
                loc.block.persistentDataContainer.encode(BlockyFurnitureHitbox(_baseEntity = baseEntity.uniqueId))
                this.get<BlockyLight>()?.lightLevel?.let { BlockLight.createBlockLight(loc, it) }
                this.get<BlockySeat>()?.let {
                    spawnFurnitureSeat(baseEntity, yaw - 180, it.heightOffset)
                }
            }
        }

        if (locations.isNotEmpty()) {
            this.getOrSetPersisting { BlockyFurnitureHitbox() }.hitbox.addAll(locations.values.flatten())
        }
    }

    internal fun clearAssosiatedHitboxChunkEntries(entity: Entity) {
        entity.toGearyOrNull()?.get<BlockyFurnitureHitbox>()?.hitbox?.forEach { hitboxLoc ->
            hitboxLoc.block.customBlockData.clear()
            hitboxLoc.block.type = Material.AIR
            BlockLight.removeBlockLight(hitboxLoc)
        }
    }

    internal fun handleFurnitureDrops(entity: Entity, player: Player?) {
        entity.toGearyOrNull()?.get<BlockyDrops>()?.let { GenericHelpers.handleBlockDrop(it, player, entity.location) }
    }

    //TODO Fix seat breaking below 0.0 offset and remove max() check here
    fun spawnFurnitureSeat(furniture: ItemDisplay, yaw: Float, height: Double = 0.0): ArmorStand? {
        return furniture.location.toBlockCenterLocation().apply { y += max(0.0, height) }.spawn<ArmorStand> {
            isVisible = false
            isMarker = true
            isSilent = true
            isSmall = true
            setGravity(false)
            setRotation(yaw, 0F)
        }?.let { seat ->
            furniture.toGeary().getOrSetPersisting { BlockyAssociatedSeats() }._seats.add(seat.uniqueId)
            seat.toGeary().setPersisting(BlockyFurnitureHitbox(_baseEntity = furniture.uniqueId))
            seat
        }
    }
}
