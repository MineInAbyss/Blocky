package com.mineinabyss.blocky.helpers

import com.jeff_media.morepersistentdatatypes.DataType
import com.mineinabyss.blocky.api.events.furniture.BlockyFurnitureBreakEvent
import com.mineinabyss.blocky.api.events.furniture.BlockyFurniturePlaceEvent
import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.blocky.components.core.BlockyBarrierHitbox
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.core.BlockyInfo
import com.mineinabyss.blocky.components.core.BlockySound
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
import io.th0rgal.protectionlib.ProtectionLib
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.*
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.PotionMeta
import kotlin.time.Duration.Companion.seconds

val FURNITURE_ORIGIN = NamespacedKey(blockyPlugin, "furniture_origin")
fun getTargetBlock(placedAgainst: Block, blockFace: BlockFace): Block? {

    return if (placedAgainst.isReplaceable) placedAgainst else {
        val target = placedAgainst.getRelative(blockFace)
        if (!target.type.isAir && target.isReplaceable) null else target
    }
}

fun getLocations(rotation: Float, center: Location, relativeCoordinates: List<BlockLocation>): List<Location> {
    val output: MutableList<Location> = ArrayList()
    for (modifier in relativeCoordinates) output.add(modifier.groundRotate(rotation).add(center))
    return output
}

fun getRotation(yaw: Float, restricted: Boolean): Rotation {
    var id = ((Location.normalizeYaw(yaw) + 180) * 8 / 360 + 0.5).toInt() % 8
    if (restricted && id % 2 != 0) id -= 1
    return Rotation.values()[id]
}

fun getYaw(rotation: Rotation): Float {
    return listOf(*Rotation.values()).indexOf(rotation) * 360f / 8f
}

fun BlockyFurniture.hasEnoughSpace(loc: Location, yaw: Float): Boolean {
    return if (collisionHitbox.isEmpty()) true
    else collisionHitbox.let { getLocations(yaw, loc, it).stream().allMatch { adjacent -> adjacent.block.type.isAir } }
}

fun GearyEntity.placeBlockyFurniture(
    rotation: Rotation,
    yaw: Float,
    loc: Location,
    blockFace: BlockFace,
    player: Player,
    item: ItemStack = player.inventory.itemInMainHand,
) {
    val furniture = get<BlockyFurniture>() ?: return
    val placableOn = getOrSetPersisting { BlockyPlacableOn() }
    val blockPlaceEvent = BlockPlaceEvent(loc.block, loc.block.state, loc.block, item, player, true, EquipmentSlot.HAND)

    if (!furniture.hasEnoughSpace(loc, yaw)) blockPlaceEvent.isCancelled = true
    else if (!ProtectionLib.canBuild(player, loc) || !blockPlaceEvent.canBuild()) blockPlaceEvent.isCancelled = true
    else if (!placableOn.isPlacableOn(loc.block, blockFace)) blockPlaceEvent.isCancelled = true
    else if (loc.block.getRelative(BlockFace.DOWN).isVanillaNoteBlock) blockPlaceEvent.isCancelled = true
    if (blockPlaceEvent.isCancelled) return

    val lootyItem = get<PrefabKey>()?.let { LootyFactory.createFromPrefab(it) }?.editItemMeta {
        displayName(Component.empty())
        ((item.itemMeta as? LeatherArmorMeta)?.color ?: (item.itemMeta as? PotionMeta)?.color)?.let { color ->
            (this as? LeatherArmorMeta)?.setColor(color) ?: (this as? PotionMeta)?.setColor(color) ?: return@editItemMeta
        }  ?: return@editItemMeta
    } ?: return

    val newFurniture = when (furniture.furnitureType) {
        BlockyFurniture.FurnitureType.ITEM_FRAME -> {
            loc.spawn<ItemFrame>()?.apply {
                isVisible = true
                isFixed = true
                isPersistent = true
                itemDropChance = 0F
                isCustomNameVisible = false
                this.rotation = rotation
                setItem(lootyItem, false)
            }
        }

        BlockyFurniture.FurnitureType.GLOW_ITEM_FRAME -> {
            loc.spawn<GlowItemFrame>()?.apply {
                isVisible = true
                isFixed = true
                isPersistent = true
                itemDropChance = 0F
                isCustomNameVisible = false
                this.rotation = rotation
                setItem(lootyItem, false)
            }
        }

        BlockyFurniture.FurnitureType.ARMOR_STAND -> {
            loc.toBlockCenterLocation().spawn<ArmorStand>()?.apply {
                isVisible = false
                isPersistent = true
                isCustomNameVisible = false
                setRotation(yaw, 0F)
                this.equipment.setItem(EquipmentSlot.HEAD, lootyItem)
            }
        }
    } ?: return

    newFurniture.toGeary().apply {
        this.setPersisting(furniture)
        this.setPersisting(placableOn)
        this.setPersisting(this@placeBlockyFurniture.getOrSetPersisting { BlockyInfo(blockBreakTime = 2.seconds) })
        this.setPersisting(this@placeBlockyFurniture.getOrSetPersisting { BlockySeat() })
        this.setPersisting(this@placeBlockyFurniture.getOrSetPersisting { BlockyLight() })
        this.setPersisting(this@placeBlockyFurniture.getOrSetPersisting { BlockySound() })
    }
    val furniturePlaceEvent = BlockyFurniturePlaceEvent(newFurniture, player)

    furniturePlaceEvent.call()
    if (furniturePlaceEvent.isCancelled) {
        newFurniture.remove()
        return
    }

    newFurniture.toGeary {
        if (get<BlockyFurniture>()?.collisionHitbox?.isNotEmpty() == true) {
            placeBarrierHitbox(yaw, loc, player)
        } else if (has<BlockyLight>())
            handleLight.createBlockLight(loc, get<BlockyLight>()!!.lightLevel)
    }

    player.swingMainHand()
    if (player.gameMode != GameMode.CREATIVE) player.inventory.itemInMainHand.subtract()
}

fun GearyEntity.placeBarrierHitbox(yaw: Float, loc: Location, player: Player) {
    val furniture = get<BlockyFurniture>() ?: return
    val locations = getLocations(yaw, loc, furniture.collisionHitbox).toMutableList()
    locations.forEach { adjacentLoc ->
        adjacentLoc.block.setType(Material.BARRIER, false)

        if (has<BlockyLight>())
            handleLight.createBlockLight(adjacentLoc, get<BlockyLight>()!!.lightLevel)
        if (has<BlockySeat>()) {
            spawnFurnitureSeat(adjacentLoc, (player.location.yaw - 180), get<BlockySeat>()?.heightOffset ?: 0.0)
        }
        adjacentLoc.block.persistentDataContainer.set(FURNITURE_ORIGIN, DataType.LOCATION, loc)
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
    this.toGeary().has<GearyEntity>() || return
    this.toGeary().get<BlockyInfo>()?.blockDrop?.handleBlockDrop(player, this.location) ?: return
}

val Block.blockyFurniture get(): Entity? {
    return this.persistentDataContainer.get(FURNITURE_ORIGIN, DataType.LOCATION)?.let { origin ->
        origin.world?.getNearbyEntities(this.boundingBox)?.firstOrNull { entity ->
            entity.toGearyOrNull()?.has<BlockyFurniture>() == true
        }
    }
}

private fun Entity.checkFurnitureHitbox(): Boolean {
    toGearyOrNull()?.get<BlockyBarrierHitbox>()?.barriers?.forEach { barrierLoc ->
        if (barrierLoc.toBlockLocation() == this.location.toBlockLocation()) return true
    }
    return false
}

fun spawnFurnitureSeat(loc: Location, yaw: Float, heightOffset: Double) {
    val location = loc.add(0.0, heightOffset, 0.0).toCenterLocation()
    location.yaw = yaw
    location.spawn<ArmorStand>()?.apply {
        isVisible = false
        isMarker = true
        isSilent = true
        isSmall = true
        setGravity(false)
        toGeary().setPersisting(BlockySeat(heightOffset))
        persistentDataContainer.set(FURNITURE_ORIGIN, DataType.LOCATION, location)
    } ?: return
}

fun Player.sitOnBlockySeat(block: Block) {
    val stand = block.blockySeat ?: return
    if (stand.passengers.isEmpty()) stand.addPassenger(this)
}

fun Entity.removeAssosiatedSeats() {
    this.toGearyOrNull()?.get<BlockySeatLocations>()?.seats?.forEach seatLoc@{ seatLoc ->
        seatLoc.block.blockySeat?.remove()
    }
}

val Block.blockySeat
    get(): Entity? {
        return this.world.getNearbyEntities(this.boundingBox).firstOrNull {
            it.toGearyOrNull() != null && it.toGeary().has<BlockySeat>() && !it.toGeary().has<BlockyFurniture>()
        }
    }
