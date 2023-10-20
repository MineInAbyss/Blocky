package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.features.BlockyDrops
import com.mineinabyss.blocky.components.features.furniture.BlockyAssociatedSeats
import com.mineinabyss.blocky.components.features.furniture.BlockyModelEngine
import com.mineinabyss.blocky.components.features.furniture.BlockySeat
import com.mineinabyss.blocky.helpers.GenericHelpers.toBlockCenterLocation
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
import org.bukkit.Rotation
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform.FIXED
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform.NONE
import org.bukkit.entity.Player
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

            furniture.properties.let { properties ->
                itemDisplayTransform = properties.displayTransform
                displayWidth = properties.displayWidth
                displayHeight = properties.displayHeight
                properties.brightness?.let { brightness = it }
                properties.trackingRotation?.let { billboard = it }
                properties.viewRange?.let { viewRange = it }
                properties.shadowRadius?.let { shadowRadius = it }
                properties.shadowStrength?.let { shadowStrength = it }

                val isFixed = itemDisplayTransform == FIXED
                transformation = transformation.apply {
                    scale.set(properties.scale ?: if (isFixed) Vector3f(0.5f, 0.5f, 0.5f) else Vector3f(1f, 1f, 1f))
                }

                val (newYaw, newPitch) = when {
                    isFixed -> getYaw(getRotation(yaw, furniture)) - 180 to -90f
                    else -> yaw to 0f
                }

                // NONE spawns into the ground, so we teleport it up
                // Rotation therefore doesn't always apply due to teleportAsync, so apply yaw/pitch to loc
                if (itemDisplayTransform == NONE) teleportAsync(loc.toCenterLocation().apply { this.yaw = newYaw; this.pitch = newPitch })
                else setRotation(newYaw, newPitch)
            }
            this.itemStack = furnitureItem
        } ?: return null

        newFurniture.toGeary().addPrefab(gearyEntity)
        gearyEntity.get<BlockyModelEngine>()?.let { meg ->
            if (!Plugins.isEnabled("ModelEngine")) return@let
            val activeModel = ModelEngineAPI.createActiveModel(meg.modelId) ?: return@let
            ModelEngineAPI.getOrCreateModeledEntity(newFurniture).apply {
                addModel(activeModel, false)
                isBaseEntityVisible = false
                isModelRotationLocked = false
            }
        }

        gearyEntity.get<BlockySeat>()?.let { seat ->
            if (furniture.collisionHitbox.isNotEmpty()) {
                getLocations(yaw, newFurniture.location, furniture.collisionHitbox)
                    .forEach { _ -> spawnFurnitureSeat(newFurniture, yaw - 180, seat.heightOffset) }
            } else spawnFurnitureSeat(newFurniture, yaw, seat.heightOffset)
        }

        return newFurniture
    }

    //TODO Fix seat breaking below 0.0 offset and remove max() check here
    fun spawnFurnitureSeat(furniture: ItemDisplay, yaw: Float, height: Double = 0.0) =
        furniture.location.toBlockCenterLocation().apply { y += max(0.0, height) }.spawn<ArmorStand> {
            isVisible = false
            isMarker = true
            isSilent = true
            isSmall = true
            setGravity(false)
            setRotation(yaw, 0F)
        }?.let { seat ->
            furniture.toGeary().getOrSetPersisting { BlockyAssociatedSeats() }._seats.add(seat.uniqueId)
            seat
        }

    internal fun ItemDisplay.handleFurnitureDrops(player: Player) {
        this.toGearyOrNull()?.get<BlockyDrops>()?.let { GenericHelpers.handleBlockDrop(it, player, location) }
    }
}
