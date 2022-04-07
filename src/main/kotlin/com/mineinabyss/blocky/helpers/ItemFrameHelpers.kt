package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.components.*
import com.mineinabyss.blocky.systems.BlockLocation
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.geary.prefabs.helpers.prefabKeys
import com.mineinabyss.idofront.spawning.spawn
import com.mineinabyss.looty.LootyFactory
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Rotation
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.ItemFrame

fun getTargetBlock(placedAgainst: Block, blockFace: BlockFace): Block? {
    val type = placedAgainst.type
    return if (REPLACEABLE_BLOCKS.contains(type)) placedAgainst else {
        val target = placedAgainst.getRelative(blockFace)
        if (!target.type.isAir && target.type != Material.WATER) null else target
    }
}

fun BlockyEntity.hasBarrierCollision() = (collisionHitbox.isNotEmpty())

fun BlockyEntity.getHitbox(): List<BlockLocation> = collisionHitbox

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

fun BlockyEntity.hasEnoughSpace(yaw: Float, loc: Location): Boolean {
    return if (!hasBarrierCollision()) true
    else getLocations(yaw, loc, getHitbox()).stream().allMatch { adjacent -> adjacent.block.type.isAir }
}

fun GearyEntity.placeBlockyFrame(rotation: Rotation, yaw: Float, facing: BlockFace, loc: Location): ItemFrame? {
    val blockyEntity = get<BlockyEntity>() ?: return null
    if (!blockyEntity.hasEnoughSpace(yaw, loc)) return null
    val lootyItem = LootyFactory.createFromPrefab(this.prefabKeys.first()) ?: return null
    val hasBlockLight = has<BlockyLight>()
    val blockyLight = get<BlockyLight>()?.lightLevel
    val newFrame =
        loc.spawn<ItemFrame>()?.apply {
            isVisible = false
            isFixed = false
            isPersistent = true
            itemDropChance = 0F
            isCustomNameVisible = false
            setRotation(rotation)
            setFacingDirection(facing, true)
        }
    val gearyFrame = newFrame?.toGearyOrNull() ?: return null
    gearyFrame.getOrSetPersisting { get<BlockyEntity>()!! }

    if (gearyFrame.get<BlockyEntity>()!!.hasBarrierCollision()) {
        gearyFrame.getOrSetPersisting { BlockyBarrierHitbox() }
        for (adjacentLoc in getLocations(yaw, loc, get<BlockyEntity>()!!.getHitbox())) {
            val block = adjacentLoc.block
            block.setType(Material.BARRIER, false)
            gearyFrame.get<BlockyBarrierHitbox>()?.barriers?.add(block.location)
            if (hasBlockLight) createBlockLight(adjacentLoc, blockyLight!!)
            if (has<BlockySeat>()) {
                gearyFrame.getOrSetPersisting { BlockySeatLocations() }
                spawnSeat(adjacentLoc, get<BlockySeat>()!!.yaw, get<BlockySeat>()!!.heightOffset)
                gearyFrame.get<BlockySeatLocations>()?.seats?.add(adjacentLoc)
            }
        }
    } else if (hasBlockLight) createBlockLight(loc, blockyLight!!)

    return newFrame
}

fun ItemFrame.checkFrameHitbox(destination: Location): Boolean {
    val barrierBox = toGeary().get<BlockyBarrierHitbox>()?.barriers ?: return false
    barrierBox.forEach { barrierLoc -> if (barrierLoc == destination) return true }
    return false
}

fun removeSolid(world: World, blockLoc: BlockLocation, rotation: Float, gearyEntity: GearyEntity): Boolean {
    val baseLoc = blockLoc.toLocation(world)
    val blockyEntity = gearyEntity.get<BlockyEntity>() ?: return false
    val hasBlockLight = gearyEntity.has<BlockyLight>()

    for (loc in getLocations(rotation, baseLoc, blockyEntity.getHitbox())) {
        if (hasBlockLight) removeBlockLight(loc)
        loc.block.type = Material.AIR
    }

    var tempBool = false
    for (frame in baseLoc.world.getNearbyEntities(baseLoc, 1.0, 1.0, 1.0)) {
        if (frame is ItemFrame &&
            frame.location.blockX == baseLoc.blockX &&
            frame.location.blockY == baseLoc.blockY &&
            frame.location.blockZ == baseLoc.blockZ &&
            frame.toGearyOrNull()?.has<BlockyEntity>() == true
        ) {
            frame.remove()
            if (frame.toGearyOrNull()?.has<BlockyLight>() == true) removeBlockLight(baseLoc)
            baseLoc.block.type = Material.AIR
            tempBool = true
            break
        }
    }
    return tempBool
}

fun spawnSeat(loc: Location, yaw: Float, heightOffset: Double) {
    loc.toCenterLocation().spawn<ArmorStand>()?.apply {
        isVisible = false
        isMarker = true
        isSilent = true
        isSmall = true
        setGravity(false)
        toGeary().getOrSetPersisting { BlockySeat() }

        this.location.yaw = yaw
        this.location.apply { y += heightOffset }
    }
}