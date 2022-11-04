package com.mineinabyss.blocky.helpers

import com.jeff_media.morepersistentdatatypes.DataType
import com.mineinabyss.blocky.api.events.furniture.BlockyFurnitureBreakEvent
import com.mineinabyss.blocky.api.events.furniture.BlockyFurniturePlaceEvent
import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.blocky.components.core.BlockyBarrierHitbox
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.core.BlockyInfo
import com.mineinabyss.blocky.components.features.BlockyLight
import com.mineinabyss.blocky.components.features.BlockySeat
import com.mineinabyss.blocky.components.features.BlockySeatLocations
import com.mineinabyss.blocky.systems.BlockLocation
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.events.call
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.idofront.spawning.spawn
import com.mineinabyss.looty.LootyFactory
import io.th0rgal.protectionlib.ProtectionLib
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Rotation
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot

val FURNITURE_ORIGIN = NamespacedKey(blockyPlugin, "furniture_origin")
fun getTargetBlock(placedAgainst: Block, blockFace: BlockFace): Block? {

    return if (placedAgainst.isReplaceable) placedAgainst else {
        val target = placedAgainst.getRelative(blockFace)
        if (!target.type.isAir && target.type != Material.WATER) null else target
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
    player: Player
) {
    val furniture = get<BlockyFurniture>() ?: return
    if (!furniture.hasEnoughSpace(loc, yaw)) return
    if (loc.block.getRelative(BlockFace.DOWN).isVanillaNoteBlock) return
    if (!ProtectionLib.canBuild(player, loc)) return

    val lootyItem = get<PrefabKey>()?.let { LootyFactory.createFromPrefab(it) } ?: return
    val newFurniture = when (furniture.furnitureType) {
        BlockyFurniture.FurnitureType.ITEM_FRAME -> {
            loc.spawn<ItemFrame>()?.apply {
                isVisible = false
                isFixed = false
                isPersistent = true
                itemDropChance = 0F
                isCustomNameVisible = false
                this.rotation = rotation
                setItem(lootyItem.clone().editItemMeta { displayName(Component.text("")) }, false)
            }
        }

        BlockyFurniture.FurnitureType.ARMOR_STAND -> {
            loc.spawn<ArmorStand>()?.apply {
                isVisible = false
                isPersistent = true
                isCustomNameVisible = false
                setRotation(yaw, 0F)
                this.equipment.setItem(
                    EquipmentSlot.HEAD,
                    lootyItem.clone().editItemMeta { displayName(Component.text("")) })
            }
        }
    } ?: return

    //TODO This doesnt look nice, find a better way that isnt Class.method(value)
    newFurniture.toGeary().apply {
        this.getOrSetPersisting { BlockyFurniture() }
        this.getOrSetPersisting { BlockySeatLocations() }
    }

    val furniturePlaceEvent = BlockyFurniturePlaceEvent(newFurniture, player).run { this.call(); this }
    if (furniturePlaceEvent.isCancelled) {
        newFurniture.remove()
        return
    }

    if (furniture.collisionHitbox.isNotEmpty()) {
        placeBarrierHitbox(yaw, loc, player)
    } else if (has<BlockyLight>())
        handleLight.createBlockLight(loc, get<BlockyLight>()!!.lightLevel)
}

fun GearyEntity.placeBarrierHitbox(yaw: Float, loc: Location, player: Player) {
    val furniture = get<BlockyFurniture>() ?: return
    val locations = getLocations(yaw, loc, furniture.collisionHitbox).toMutableList()
    locations.forEach { adjacentLoc ->
        adjacentLoc.block.setType(Material.BARRIER, false)

        if (has<BlockyLight>())
            handleLight.createBlockLight(adjacentLoc, get<BlockyLight>()!!.lightLevel)
        if (has<BlockySeat>()) {
            spawnFurnitureSeat(
                adjacentLoc, (player.location.yaw - 180), getOrSetPersisting { BlockySeat() }.heightOffset
            )
        }
        adjacentLoc.block.persistentDataContainer.set(FURNITURE_ORIGIN, DataType.LOCATION, loc)
    }

    set(BlockyBarrierHitbox(locations))
    set(BlockySeatLocations(locations))

}

fun Entity.removeBlockyFurniture(player: Player?) {
    this.toGearyOrNull()?.get<BlockyFurniture>() ?: return
    val furnitureBreakEvent = BlockyFurnitureBreakEvent(this, player).run { this.call(); this }
    if (furnitureBreakEvent.isCancelled) return

    this.removeAssosiatedSeats()
    this.clearAssosiatedBarrierChunkEntries()
    handleFurnitureDrops(player)
    handleLight.removeBlockLight(this.location)
    this.remove()
}

private fun Entity.clearAssosiatedBarrierChunkEntries() {
    toGearyOrNull().broadcastVal()?.get<BlockyBarrierHitbox>()?.barriers?.forEach barrier@{ barrierLoc ->
        barrierLoc.block.customBlockData.clear()
        barrierLoc.block.type = Material.AIR
        handleLight.removeBlockLight(barrierLoc)
    }
}

private fun Entity.handleFurnitureDrops(player: Player?) {
    this.toGeary().has<GearyEntity>() || return
    this.toGeary().get<BlockyInfo>()?.blockDrop?.handleBlockDrop(player, this.location) ?: return
}

fun Block.getBlockyFurniture(): Entity? {
    return this.persistentDataContainer.get(FURNITURE_ORIGIN, DataType.LOCATION)?.let { origin ->
        origin.world?.getNearbyEntities(this.boundingBox)?.firstOrNull { entity ->
            entity.toGearyOrNull()?.has<BlockyFurniture>() == true
        }
    }
}

private fun Entity.checkFurnitureHitbox(): Boolean {
    toGearyOrNull()?.get<BlockyBarrierHitbox>()?.barriers?.forEach { barrierLoc ->
        barrierLoc.broadcastVal()
        if (barrierLoc.toBlockLocation() == this.location.toBlockLocation()) return true
    }
    return false
}

fun spawnFurnitureSeat(loc: Location, yaw: Float, heightOffset: Double) {
    val location = loc.add(0.0, heightOffset, 0.0).toCenterLocation()
    location.yaw = yaw
    val stand = location.spawn<ArmorStand>()?.apply {
        isVisible = false
        isMarker = true
        isSilent = true
        isSmall = true
        setGravity(false)
    }
    stand?.toGeary()?.apply {
        set(BlockySeat(heightOffset))
        stand.persistentDataContainer.set(FURNITURE_ORIGIN, DataType.LOCATION, location)
    } ?: return
}

fun Block.sitOnSeat(player: Player) {
    val stand = this.blockySeat ?: return
    if (stand.passengers.isEmpty()) stand.addPassenger(player)
}

fun Entity.removeAssosiatedSeats() {
    val furniture = this.toGearyOrNull() ?: return
    furniture.get<BlockySeatLocations>()?.seats?.forEach seatLoc@{ seatLoc ->
        seatLoc.block.blockySeat?.remove()
    }
}

val Block.blockySeat get(): Entity? {
    return this.world.getNearbyEntities(this.boundingBox).firstOrNull {
        it.toGearyOrNull() != null && it.toGeary().has<BlockySeat>()
    }
}
