package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.api.events.furniture.FurnitureBreakEvent
import com.mineinabyss.blocky.api.events.furniture.FurniturePlaceEvent
import com.mineinabyss.blocky.components.core.BlockyBarrierHitbox
import com.mineinabyss.blocky.components.core.BlockyEntity
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
import com.mineinabyss.idofront.spawning.spawn
import com.mineinabyss.looty.LootyFactory
import com.mineinabyss.looty.tracking.toGearyOrNull
import io.th0rgal.protectionlib.ProtectionLib
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Rotation
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.Event

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

fun BlockyEntity.hasEnoughSpace(loc: Location, yaw: Float): Boolean {
    return if (collisionHitbox.isEmpty()) true
    else collisionHitbox.let { getLocations(yaw, loc, it).stream().allMatch { adjacent -> adjacent.block.type.isAir } }
}

fun GearyEntity.placeBlockyFrame(
    rotation: Rotation,
    yaw: Float,
    facing: BlockFace,
    loc: Location,
    player: Player
) {
    if (!has<BlockyEntity>()) return
    if (get<BlockyEntity>()?.hasEnoughSpace(loc, yaw) != true) return
    if (loc.block.getRelative(BlockFace.DOWN).isVanillaNoteBlock()) return
    if (!ProtectionLib.canBuild(player, loc)) return

    val lootyItem = get<PrefabKey>()?.let { LootyFactory.createFromPrefab(it) } ?: return
    val newFrame =
        loc.spawn<ItemFrame>()?.apply {
            isVisible = false
            isFixed = false
            isPersistent = true
            itemDropChance = 0F
            isCustomNameVisible = false
            setItem(lootyItem.editItemMeta { displayName(Component.empty()) }, false)
            setRotation(rotation)
            setFacingDirection(facing, true)
        }

    val gearyItem = newFrame?.item?.toGearyOrNull(player) ?: return
    val gearyFrame = newFrame.toGeary()
    gearyFrame.getOrSetPersisting { BlockySeatLocations() }
    gearyFrame.getOrSetPersisting { BlockyBarrierHitbox() }

    val furniturePlaceEvent = FurniturePlaceEvent(newFrame, player).run { this.call(); this }
    if (furniturePlaceEvent.isCancelled) {
        newFrame.remove()
        return
    }

    if (gearyItem.get<BlockyEntity>()?.collisionHitbox?.isNotEmpty() == true) {
        newFrame.placeBarrierHitbox(yaw, loc, player)
    } else if (gearyItem.has<BlockyLight>()) handleLight.createBlockLight(
        loc,
        gearyItem.get<BlockyLight>()!!.lightLevel
    )
}

fun ItemFrame.placeBarrierHitbox(yaw: Float, loc: Location, player: Player) {
    val gearyItem = item.toGearyOrNull(player) ?: return
    toGearyOrNull() ?: return

    getLocations(yaw, loc, gearyItem.get<BlockyEntity>()?.collisionHitbox!!).forEach { adjacentLoc ->
        adjacentLoc.block.setType(Material.BARRIER, false)
        toGeary().get<BlockyBarrierHitbox>()?.barriers?.add(adjacentLoc.block.location)

        if (gearyItem.has<BlockyLight>())
            handleLight.createBlockLight(adjacentLoc, gearyItem.get<BlockyLight>()!!.lightLevel)
        if (gearyItem.has<BlockySeat>()) {
            spawnSeat(adjacentLoc, (player.location.yaw - 180), gearyItem.get<BlockySeat>()?.heightOffset ?: 0.0)
            toGeary().get<BlockySeatLocations>()?.seats?.add(adjacentLoc)
        }
    }
}

fun ItemFrame.removeBlockyFrame(player: Player?, event: Event) {
    this.toGearyOrNull()?.get<BlockyEntity>() ?: return

    val furnitureBreakEvent = FurnitureBreakEvent(this, player).run { this.call(); this }
    if (furnitureBreakEvent.isCancelled) return

    this.removeAssosiatedSeats()
    this.clearAssosiatedBarrierChunkEntries(event)
    handleFurnitureDrops(player)
    handleLight.removeBlockLight(this.location)
    this.remove()
}

private fun ItemFrame.clearAssosiatedBarrierChunkEntries(event: Event) {
    toGearyOrNull()?.get<BlockyBarrierHitbox>()?.barriers?.forEach barrier@{ barrierLoc ->
        barrierLoc.block.clearCustomBlockData(event)
        barrierLoc.block.type = Material.AIR
        handleLight.removeBlockLight(barrierLoc)
    }
}

fun Block.getAssociatedBlockyFrame(radius: Double): ItemFrame? {
    return location.getNearbyEntitiesByType(ItemFrame::class.java, radius)
        .firstOrNull { it.toGearyOrNull() != null && it.toGeary().checkFrameHitbox(location) }
}

private fun ItemFrame.handleFurnitureDrops(player: Player?) {
    this.toGeary().has<GearyEntity>() || return
    this.toGeary().get<BlockyInfo>()?.blockDrop?.handleBlockDrop(player, this.location) ?: return
}

private fun GearyEntity.checkFrameHitbox(destination: Location): Boolean {
    val barrierBox = get<BlockyBarrierHitbox>()?.barriers ?: return false
    barrierBox.forEach { barrierLoc -> if (barrierLoc == destination) return true }
    return false
}

fun spawnSeat(loc: Location, yaw: Float, heightOffset: Double) {
    loc.add(0.0, heightOffset, 0.0)
    loc.yaw = yaw
    val stand = loc.toCenterLocation().spawn<ArmorStand>()?.apply {
        isVisible = false
        isMarker = true
        isSilent = true
        isSmall = true
        setGravity(false)
    }
    stand?.toGeary()?.getOrSetPersisting { BlockySeat() } ?: return
}

fun Block.sitOnSeat(player: Player) {
    val frame = this.getAssociatedBlockyFrame(0.5) ?: return
    if (frame.toGearyOrNull() == null) return
    if (frame.toGeary().checkFrameHitbox(this.location) && frame.toGeary().has<BlockySeatLocations>()) {
        val stand = this.location.toCenterLocation().getNearbyEntitiesByType(ArmorStand::class.java, 0.5).firstOrNull()
            ?: return
        if (stand.passengers.isEmpty()) stand.addPassenger(player)
    }
}

fun ItemFrame.removeAssosiatedSeats() {
    val frame = this.toGearyOrNull() ?: return
    frame.get<BlockySeatLocations>()?.seats?.forEach seatLoc@{ seatLoc ->
        seatLoc.getNearbyEntitiesByType(
            ArmorStand::class.java,
            frame.get<BlockySeat>()?.heightOffset?.times(2) ?: return@seatLoc
        ).forEach seat@{ stand -> if (stand.toGeary().has<BlockySeat>()) stand.remove() }
    }
}
