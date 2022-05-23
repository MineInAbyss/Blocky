package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.components.*
import com.mineinabyss.blocky.systems.BlockLocation
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.spawning.spawn
import com.mineinabyss.looty.LootyFactory
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Rotation
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

fun GearyEntity.placeBlockyFrame(rotation: Rotation, yaw: Float, facing: BlockFace, loc: Location): ItemFrame? {
    if (!has<BlockyEntity>()) return null
    if (get<BlockyEntity>()?.hasEnoughSpace(loc, yaw) != true) return null
    if (loc.block.getRelative(BlockFace.DOWN).isVanillaNoteBlock()) return null

    val lootyItem = get<PrefabKey>()?.let { LootyFactory.createFromPrefab(it) } ?: return null
    val newFrame =
        loc.spawn<ItemFrame>()?.apply {
            isVisible = false
            isFixed = false
            isPersistent = true
            itemDropChance = 0F
            isCustomNameVisible = false
            setItem(lootyItem.editItemMeta { displayName(Component.empty()) })
            setRotation(rotation)
            setFacingDirection(facing, true)
        }
    val gearyFrame = newFrame?.toGearyOrNull() ?: return null
    gearyFrame.setAllPersisting(getComponents())

    if (gearyFrame.get<BlockyEntity>()?.collisionHitbox?.isNotEmpty() == true) {
        getLocations(yaw, loc, gearyFrame.get<BlockyEntity>()?.collisionHitbox!!).forEach {adjacentLoc ->
            gearyFrame.setPersisting(BlockySeatLocations)
            val block = adjacentLoc.block
            block.setType(Material.BARRIER, false)
            gearyFrame.get<BlockyBarrierHitbox>()?.barriers?.add(block.location)

            if (gearyFrame.has<BlockyLight>())
                createBlockLight(adjacentLoc, gearyFrame.get<BlockyLight>()!!.lightLevel)
            if (gearyFrame.has<BlockySeat>()) {
                gearyFrame.getOrSetPersisting { BlockySeatLocations() }
                spawnSeat(adjacentLoc, gearyFrame.get<BlockySeat>()!!.yaw, gearyFrame.get<BlockySeat>()!!.heightOffset)
                gearyFrame.get<BlockySeatLocations>()?.seats?.add(adjacentLoc)
            }
        }
    } else if (gearyFrame.has<BlockyLight>()) createBlockLight(loc, gearyFrame.get<BlockyLight>()!!.lightLevel)

    return newFrame
}

fun ItemFrame.checkFrameHitbox(destination: Location): Boolean {
    val barrierBox = toGeary().get<BlockyBarrierHitbox>()?.barriers ?: return false
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
