package com.mineinabyss.blocky.helpers

import com.jeff_media.morepersistentdatatypes.DataType
import com.mineinabyss.blocky.api.BlockyFurnitures.blockySeat
import com.mineinabyss.blocky.api.BlockyFurnitures.removeBlockyFurniture
import com.mineinabyss.blocky.api.events.furniture.BlockyFurniturePlaceEvent
import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.core.BlockyFurnitureHitbox
import com.mineinabyss.blocky.components.core.BlockyInfo
import com.mineinabyss.blocky.components.core.BlockyModelEngine
import com.mineinabyss.blocky.components.features.BlockyLight
import com.mineinabyss.blocky.components.features.BlockyPlacableOn
import com.mineinabyss.blocky.components.features.BlockySeat
import com.mineinabyss.blocky.components.features.BlockySeatLocations
import com.mineinabyss.blocky.systems.BlockLocation
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.prefabs.helpers.addPrefab
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.spawning.spawn
import com.ticxo.modelengine.api.ModelEngineAPI
import io.th0rgal.protectionlib.ProtectionLib
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.*
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.inventory.meta.PotionMeta
import org.joml.Vector3f
import kotlin.math.max

val FURNITURE_ORIGIN = NamespacedKey(blocky.plugin, "furniture_origin")
fun getTargetBlock(placedAgainst: Block, blockFace: BlockFace): Block? {

    return if (placedAgainst.isReplaceable) placedAgainst else {
        val target = placedAgainst.getRelative(blockFace)
        if (!target.type.isAir && target.isReplaceable) null else target
    }
}

fun getLocations(rotation: Float, center: Location, relativeCoordinates: List<BlockLocation>): MutableList<Location> {
    return mutableListOf<Location>().apply {
        relativeCoordinates.forEach { blockLoc ->
            add(blockLoc.groundRotate(rotation).add(center))
        }
    }
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

fun BlockyFurniture.hasEnoughSpace(loc: Location, yaw: Float): Boolean {
    return if (collisionHitbox.isEmpty()) true
    else collisionHitbox.let { getLocations(yaw, loc, it).stream().allMatch { adjacent -> adjacent.block.type.isAir } }
}

val ItemDisplay.interactionEntity: Interaction?
    get() = this.toGearyOrNull()?.get<BlockyFurnitureHitbox>()?.interactionHitbox?.let { Bukkit.getEntity(it) as? Interaction }
val Interaction.baseFurniture: ItemDisplay?
    get() = this.toGearyOrNull()?.get<BlockyFurnitureHitbox>()?.baseEntity?.let { Bukkit.getEntity(it) as? ItemDisplay }

fun GearyEntity.placeBlockyFurniture(
    loc: Location,
    player: Player,
    hand: EquipmentSlot,
    item: ItemStack,
    blockFace: BlockFace,
): ItemDisplay? {
    val furniture = get<BlockyFurniture>() ?: return null
    val rotation = getRotation(player.location.yaw, furniture)
    val yaw = if (furniture.hasStrictRotation) getYaw(rotation) else player.location.yaw - 180

    when {
        !furniture.hasEnoughSpace(loc, yaw) -> return null
        !ProtectionLib.canBuild(player, loc) -> return null
        get<BlockyPlacableOn>()?.isPlacableOn(loc.block, blockFace) == false -> return null
        loc.block.getRelative(BlockFace.DOWN).isVanillaNoteBlock -> return null
    }

    val lootyItem = get<ItemStack>()?.clone()?.editItemMeta {
        displayName(Component.empty())
        (this as? LeatherArmorMeta)?.setColor((item.itemMeta as? LeatherArmorMeta)?.color)
            ?: (this as? PotionMeta)?.setColor((item.itemMeta as? PotionMeta)?.color)
            ?: (this as? MapMeta)?.setColor((item.itemMeta as? MapMeta)?.color) ?: return@editItemMeta
    } ?: return null

    val newFurniture = loc.toBlockCenterLocation().spawn<ItemDisplay> {
        isPersistent = true
        setRotation(yaw, 0F)
        setGravity(false)

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

            setRotation(getYaw(rotation.rotateClockwise().rotateClockwise().rotateClockwise().rotateClockwise()), if (isFixed) 90f else 0f)
            if (itemDisplayTransform == ItemDisplay.ItemDisplayTransform.NONE)
                teleport(location.toCenterLocation())

        }
        this.itemStack = lootyItem
    } ?: return null

    // Spawn Interaction Entity if defined and remove both entities if it fails
    val interaction = furniture.interactionHitbox?.let {
        newFurniture.location.toBlockCenterLocation().spawn<Interaction> {
            isPersistent = true
            interactionWidth = furniture.interactionHitbox.width
            interactionHeight = furniture.interactionHitbox.height
        } ?: run {
            newFurniture.removeBlockyFurniture()
            return null
        }
    }

    newFurniture.toGeary().apply {
        addPrefab(this@placeBlockyFurniture)
        //TODO Fix PrefabKey when Offz adds helper methods
        setPersisting(BlockyFurnitureHitbox(interactionHitbox = interaction?.uniqueId))
    }
    interaction?.toGeary()?.setPersisting(BlockyFurnitureHitbox(baseEntity = newFurniture.uniqueId))

    get<BlockyModelEngine>()?.let { meg ->
        if (!blocky.plugin.server.pluginManager.isPluginEnabled("ModelEngine")) return@let
        val activeModel = ModelEngineAPI.createActiveModel(meg.modelId) ?: return@let
        ModelEngineAPI.getOrCreateModeledEntity(newFurniture).apply {
            addModel(activeModel, false)
            isBaseEntityVisible = false
            isModelRotationLock = true
        }
    }

    if (!BlockyFurniturePlaceEvent(newFurniture, player).callEvent()) {
        newFurniture.removeBlockyFurniture()
        return null
    }

    newFurniture.toGeary().let { gearyEntity ->
        if (furniture.collisionHitbox.isNotEmpty()) {
            gearyEntity.placeFurnitureHitbox(newFurniture.location.yaw, loc, player)
        } else if (furniture.interactionHitbox != null) {
            gearyEntity.get<BlockyLight>()?.lightLevel?.let { handleLight.createBlockLight(loc, it) }
            gearyEntity.get<BlockySeat>()?.let {
                spawnFurnitureSeat(newFurniture, player.location.yaw - 180)
            }
        } else gearyEntity.get<BlockyLight>()?.lightLevel?.let { handleLight.createBlockLight(loc, it) }

    }

    player.swingHand(hand)
    if (player.gameMode != GameMode.CREATIVE) player.inventory.itemInMainHand.subtract()
    return newFurniture
}

private fun GearyEntity.placeFurnitureHitbox(yaw: Float, originLocation: Location, player: Player) {
    val furniture = get<BlockyFurniture>() ?: return
    val locations = getLocations(yaw, originLocation, furniture.collisionHitbox)

    locations.forEach { loc ->
        loc.block.setType(Material.BARRIER, false)
        this.get<BlockyLight>()?.lightLevel?.let { handleLight.createBlockLight(loc, it) }
        this.get<BlockySeat>()?.let {
            spawnFurnitureSeat(loc.toBlockCenterLocation().apply { y += max(0.0, it.heightOffset) }, player.location.yaw - 180)
        }

        loc.block.persistentDataContainer.set(FURNITURE_ORIGIN, DataType.LOCATION, originLocation)
    }

    if (locations.isNotEmpty()) {
        setPersisting(BlockySeatLocations(locations))
        setPersisting(BlockyFurnitureHitbox(locations))
    }
}

internal fun Entity.clearAssosiatedHitboxChunkEntries() {
    toGearyOrNull()?.get<BlockyFurnitureHitbox>()?.hitbox?.forEach { hitboxLoc ->
        hitboxLoc.block.customBlockData.clear()
        hitboxLoc.block.type = Material.AIR
        handleLight.removeBlockLight(hitboxLoc)
    }
}

internal fun Entity.handleFurnitureDrops(player: Player?) {
    this.toGearyOrNull()?.get<BlockyInfo>()?.blockDrop?.handleBlockDrop(player, this.location) ?: return
}

//TODO Fix seat breaking below 0.0 offset and remove max() check here
fun spawnFurnitureSeat(furniture: ItemDisplay, yaw: Float): ArmorStand? {
    return furniture.location.spawn<ArmorStand> {
        isVisible = false
        isMarker = true
        isSilent = true
        isSmall = true
        setGravity(false)
        setRotation(yaw, 0F)
    }?.apply {
        toGeary().setPersisting(BlockySeat(location.y - location.toBlockCenterLocation().y))
        persistentDataContainer.set(FURNITURE_ORIGIN, DataType.UUID, furniture.uniqueId)
    }
}
fun spawnFurnitureSeat(location: Location, yaw: Float): ArmorStand? {
    return location.spawn<ArmorStand> {
        isVisible = false
        isMarker = true
        isSilent = true
        isSmall = true
        setGravity(false)
        setRotation(yaw, 0F)
    }?.apply {
        toGeary().setPersisting(BlockySeat(location.y - location.toBlockCenterLocation().y))
        persistentDataContainer.set(FURNITURE_ORIGIN, DataType.LOCATION, location)
    }
}

fun ItemDisplay.removeAssosiatedSeats() {
    this.toGearyOrNull()?.get<BlockySeatLocations>()?.seats?.forEach { seatLoc ->
        seatLoc.block.blockySeat?.remove()
    }
}

fun Entity.removeInteractionHitbox() {
    this.toGearyOrNull()?.get<BlockyFurnitureHitbox>()?.interactionHitbox?.let {
        Bukkit.getEntity(it)?.remove()
    }
}
