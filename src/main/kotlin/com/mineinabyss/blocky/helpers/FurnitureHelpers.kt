package com.mineinabyss.blocky.helpers

import com.jeff_media.morepersistentdatatypes.DataType
import com.mineinabyss.blocky.api.events.furniture.BlockyFurnitureBreakEvent
import com.mineinabyss.blocky.api.events.furniture.BlockyFurniturePlaceEvent
import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.blocky.components.core.*
import com.mineinabyss.blocky.components.features.BlockyLight
import com.mineinabyss.blocky.components.features.BlockyPlacableOn
import com.mineinabyss.blocky.components.features.BlockySeat
import com.mineinabyss.blocky.components.features.BlockySeatLocations
import com.mineinabyss.blocky.systems.BlockLocation
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.events.call
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.spawning.spawn
import com.mineinabyss.looty.LootyFactory
import com.ticxo.modelengine.api.ModelEngineAPI
import io.th0rgal.protectionlib.ProtectionLib
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.*
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.PotionMeta
import kotlin.math.max

val FURNITURE_ORIGIN = NamespacedKey(blockyPlugin, "furniture_origin")
fun getTargetBlock(placedAgainst: Block, blockFace: BlockFace): Block? {

    return if (placedAgainst.isReplaceable) placedAgainst else {
        val target = placedAgainst.getRelative(blockFace)
        if (!target.type.isAir && target.isReplaceable) null else target
    }
}

fun getLocations(rotation: Float, center: Location, relativeCoordinates: List<BlockLocation>): MutableList<Location> {
    return mutableListOf<Location>().apply {
        relativeCoordinates.forEach { blockLoc ->
            this += blockLoc.groundRotate(rotation).add(center)
        }
    }
}

fun getRotation(yaw: Float, nullFurniture: BlockyFurniture?): Rotation {
    val furniture = nullFurniture ?: BlockyFurniture(BlockyFurniture.FurnitureType.ARMOR_STAND)
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

fun GearyEntity.placeBlockyFurniture(
    player: Player,
    loc: Location,
    blockFace: BlockFace,
    item: ItemStack = player.inventory.itemInMainHand,
) {
    val furniture = get<BlockyFurniture>()
    val info = get<BlockyInfo>()
    val placableOn = get<BlockyPlacableOn>()
    val light = get<BlockyLight>()
    val seat = get<BlockySeat>()
    val sounds = get<BlockySound>()
    val modelengine = get<BlockyModelEngine>()
    val blockPlaceEvent = BlockPlaceEvent(loc.block, loc.block.state, loc.block, item, player, true, EquipmentSlot.HAND)

    val yaw = if (furniture?.furnitureType != BlockyFurniture.FurnitureType.ARMOR_STAND || furniture.hasStrictRotation)
        getYaw(getRotation(player.location.yaw, furniture)) else player.location.yaw - 180

    when {
        furniture?.hasEnoughSpace(loc, yaw) == false -> blockPlaceEvent.isCancelled = true
        !ProtectionLib.canBuild(player, loc) || !blockPlaceEvent.canBuild() -> blockPlaceEvent.isCancelled = true
        placableOn?.isPlacableOn(loc.block, blockFace) == false -> blockPlaceEvent.isCancelled = true
        loc.block.getRelative(BlockFace.DOWN).isVanillaNoteBlock -> blockPlaceEvent.isCancelled = true
    }
    if (blockPlaceEvent.isCancelled) return
    val lootyItem = get<PrefabKey>()?.let {
        LootyFactory.createFromPrefab(it)?.editItemMeta {
            (this as? LeatherArmorMeta)?.setColor((item.itemMeta as? LeatherArmorMeta)?.color)
                ?: (this as? PotionMeta)?.setColor((item.itemMeta as? PotionMeta)?.color) ?: return@editItemMeta
        }
    } ?: return

    val newFurniture = when {
        furniture?.furnitureType == BlockyFurniture.FurnitureType.ITEM_FRAME -> {
            loc.toBlockCenterLocation().spawn<ItemFrame> {
                isVisible = false
                isFixed = false
                isPersistent = true
                itemDropChance = 0F
                isCustomNameVisible = false
                setFacingDirection(blockFace)
                this.rotation = rotation
                setItem(lootyItem, false)
            }
        }

        furniture?.furnitureType == BlockyFurniture.FurnitureType.GLOW_ITEM_FRAME -> {
            loc.toBlockCenterLocation().spawn<GlowItemFrame> {
                isVisible = false
                isFixed = false
                isPersistent = true
                itemDropChance = 0F
                isCustomNameVisible = false
                setFacingDirection(blockFace)
                this.rotation = rotation
                setItem(lootyItem, false)
            }
        }

        this.has<BlockyModelEngine>() || furniture?.furnitureType == BlockyFurniture.FurnitureType.ARMOR_STAND -> {
            loc.toBlockCenterLocation().spawn<ArmorStand> {
                isVisible = false
                isPersistent = true
                isCustomNameVisible = false
                addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING)
                setRotation(yaw, 0F)
                setGravity(false)
                this.equipment.setItem(EquipmentSlot.HEAD, lootyItem)
            }
        }

        else -> return
    } ?: return

    newFurniture.toGeary().apply {
        furniture?.let { setPersisting(it) }
        info?.let { this.setPersisting(it) }
        light?.let { this.setPersisting(it) }
        seat?.let { this.setPersisting(it) }
        placableOn?.let { this.setPersisting(it) }
        sounds?.let { this.setPersisting(it) }
        modelengine?.let { this.setPersisting(it) }
    }

    modelengine?.let { meg ->
        if (!blockyPlugin.server.pluginManager.isPluginEnabled("ModelEngine")) return@let
        val activeModel = ModelEngineAPI.createActiveModel(meg.modelId) ?: return@let
        ModelEngineAPI.getOrCreateModeledEntity(newFurniture).apply {
            addModel(activeModel, false)
            isBaseEntityVisible = false
            isModelRotationLock = true
        }
    }

    val furniturePlaceEvent = BlockyFurniturePlaceEvent(newFurniture, player).run { call(); this }
    if (furniturePlaceEvent.isCancelled) {
        newFurniture.remove()
        return
    }

    newFurniture.toGeary {
        if (this.get<BlockyFurniture>()?.collisionHitbox?.isNotEmpty() == true) {
            this.placeBarrierHitbox(yaw, loc, player)
        } else if (has<BlockyLight>())
            handleLight.createBlockLight(loc, this.get<BlockyLight>()!!.lightLevel)
    }

    player.swingMainHand()
    if (player.gameMode != GameMode.CREATIVE) player.inventory.itemInMainHand.subtract()
}

private fun GearyEntity.placeBarrierHitbox(yaw: Float, loc: Location, player: Player) {
    val furniture = get<BlockyFurniture>() ?: return
    val locations = getLocations(yaw, loc, furniture.collisionHitbox)
    locations.forEach { l ->
        val location = l.toBlockCenterLocation()
        location.yaw = yaw
        location.block.setType(Material.BARRIER, false)


        this.get<BlockyLight>()?.let { handleLight.createBlockLight(location, it.lightLevel) }
        this.get<BlockySeat>()
            ?.let { spawnFurnitureSeat(location.apply { y += max(0.0, it.heightOffset) }, player.location.yaw - 180) }

        location.block.persistentDataContainer.set(FURNITURE_ORIGIN, DataType.LOCATION, loc)
    }

    if (locations.isNotEmpty()) {
        setPersisting(BlockySeatLocations(locations))
        setPersisting(BlockyBarrierHitbox(locations))
    }
}

fun Entity.removeBlockyFurniture(player: Player?) {
    this.toGearyOrNull()?.get<BlockyFurniture>() ?: return
    val furnitureBreakEvent = BlockyFurnitureBreakEvent(this, player)
    if (!ProtectionLib.canBreak(player, location)) furnitureBreakEvent.isCancelled = true
    if (furnitureBreakEvent.isCancelled) return
    furnitureBreakEvent.call()

    this.removeAssosiatedSeats()
    this.clearAssosiatedBarrierChunkEntries()
    handleFurnitureDrops(player)
    handleLight.removeBlockLight(this.location)
    this.toGearyOrNull()?.clear()
    this.remove()
}

private fun Entity.clearAssosiatedBarrierChunkEntries() {
    toGearyOrNull()?.get<BlockyBarrierHitbox>()?.barriers?.forEach barrier@{ barrierLoc ->
        barrierLoc.block.customBlockData.clear()
        barrierLoc.block.type = Material.AIR
        handleLight.removeBlockLight(barrierLoc)
    }
}

private fun Entity.handleFurnitureDrops(player: Player?) {
    this.toGearyOrNull()?.get<BlockyInfo>()?.blockDrop?.handleBlockDrop(player, this.location) ?: return
}

val Block.blockyFurniture
    get(): Entity? {
        return this.persistentDataContainer.get(FURNITURE_ORIGIN, DataType.LOCATION)?.let { origin ->
            origin.world?.getNearbyEntities(origin.block.boundingBox)?.firstOrNull { entity ->
                entity.toGearyOrNull()?.has<BlockyFurniture>() == true
            }
        }
    }

//TODO Fix seat breaking below 0.0 offset and remove max() check here
fun spawnFurnitureSeat(location: Location, yaw: Float) {
    location.spawn<ArmorStand>() {
        isVisible = false
        isMarker = true
        isSilent = true
        isSmall = true
        setGravity(false)
        setRotation(yaw, 0F)
    }?.apply {
        toGeary().setPersisting(BlockySeat(location.y - location.toBlockCenterLocation().y))
        persistentDataContainer.set(FURNITURE_ORIGIN, DataType.LOCATION, location)
    } ?: return
}

fun Player.sitOnBlockySeat(block: Block) {
    block.blockySeat?.let {
        if (this.passengers.isEmpty()) it.addPassenger(this)
    }
}

fun Entity.removeAssosiatedSeats() {
    this.toGearyOrNull()?.get<BlockySeatLocations>()?.seats?.forEach { seatLoc ->
        seatLoc.block.blockySeat?.remove()
    }
}

val Block.blockySeat
    get(): Entity? {
        return this.world.getNearbyEntities(this.boundingBox.expand(1.0)).firstOrNull {
            it.toGearyOrNull()?.let { g ->
                g.has<BlockySeat>() && !g.has<BlockyFurniture>()
            } ?: false
        }
    }
