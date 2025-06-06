package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.features.BlockyDrops
import com.mineinabyss.blocky.components.features.furniture.BlockyAssociatedSeats
import com.mineinabyss.blocky.components.features.furniture.BlockySeats
import com.mineinabyss.blocky.helpers.GenericHelpers.toBlockCenterLocation
import com.mineinabyss.geary.papermc.toEntityOrNull
import com.mineinabyss.geary.papermc.tracking.entities.helpers.spawnFromPrefab
import com.mineinabyss.geary.papermc.tracking.entities.helpers.withGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.idofront.items.asColorable
import com.mineinabyss.idofront.operators.plus
import com.mineinabyss.idofront.spawning.spawn
import io.papermc.paper.math.Position
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.bukkit.Location
import org.bukkit.Rotation
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform.FIXED
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform.NONE
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object FurnitureHelpers {
    fun targetBlock(placedAgainst: Block, blockFace: BlockFace): Block? {
        return if (placedAgainst.isReplaceable) placedAgainst
        else placedAgainst.getRelative(blockFace).takeUnless { !it.type.isAir && it.isReplaceable }
    }

    fun collisionHitboxLocations(
        rotation: Float,
        center: Location,
        hitbox: Set<BlockyFurniture.CollisionHitbox>,
    ): ObjectArrayList<Location> =
        hitbox.mapFast { c -> c.location.groundRotate(rotation).add(center) }

    fun collisionHitboxPositions(
        rotation: Float,
        center: Location,
        hitbox: Set<BlockyFurniture.CollisionHitbox>,
    ): ObjectArrayList<Position> =
        collisionHitboxLocations(rotation, center, hitbox).mapFast { Position.block(it) }

    fun interactionHitboxLocations(
        rotation: Float,
        center: Location,
        hitbox: Set<BlockyFurniture.InteractionHitbox>,
    ): ObjectArrayList<Location> = hitbox.mapFast { i -> center.clone().plus(i.offset(rotation)) }

    fun rotation(yaw: Float, nullFurniture: BlockyFurniture?): Rotation {
        val furniture = nullFurniture ?: BlockyFurniture()
        val rotationDegree = if (furniture.rotationType == BlockyFurniture.RotationType.STRICT) 0 else 1
        val id = (((Location.normalizeYaw(yaw) + 180) * 8 / 360 + 0.5).toInt() % 8).apply {
            if (furniture.hasStrictRotation && this % 2 != 0) this - rotationDegree
        }
        return Rotation.entries[id].rotateClockwise().rotateClockwise()
    }

    fun yaw(rotation: Rotation) = Rotation.entries.indexOf(rotation) * 360f / 8f

    fun hasEnoughSpace(blockyFurniture: BlockyFurniture, loc: Location, yaw: Float): Boolean {
        if (blockyFurniture.collisionHitbox.isEmpty() && blockyFurniture.interactionHitbox.isEmpty()) return true

        return collisionHitboxLocations(yaw, loc, blockyFurniture.collisionHitbox)
            .plus(interactionHitboxLocations(yaw, loc, blockyFurniture.interactionHitbox))
            .all { adjacent -> adjacent.block.isReplaceable }
    }


    internal fun placeBlockyFurniture(
        prefabKey: PrefabKey,
        loc: Location,
        yaw: Float = loc.yaw,
        item: ItemStack?,
    ): ItemDisplay? = loc.withGeary {
        val gearyEntity = prefabKey.toEntityOrNull() ?: return null
        val furniture = gearyEntity.get<BlockyFurniture>() ?: return null

        val spawnLoc = loc.clone().toBlockCenterLocation().apply {
            if (furniture.properties.displayTransform == NONE) this.y += 0.5 * furniture.properties.scale.y
            this.yaw = yaw(rotation(yaw, furniture))
            this.pitch = if (furniture.properties.displayTransform == FIXED) 90f else 0f
        }

        // Try to get held item's color, used to dye furniture
        val color = item?.asColorable()?.color
        return spawnLoc.spawnFromPrefab(prefabKey) {
            color?.let { set(BlockyFurniture.Color(it)) }
        }.getOrThrow() as? ItemDisplay
    }

    fun spawnFurnitureSeat(furniture: ItemDisplay, seats: BlockySeats) {
        furniture.toGeary().setPersisting(
            BlockyAssociatedSeats(
                seats.offsets.mapNotNullFast { seatOffset ->
                    furniture.location.add(seatOffset).spawn<Interaction> {
                        isPersistent = false
                        interactionWidth = 0.1f
                        interactionHeight = 0.1f
                    }?.uniqueId
                }
            )
        )
    }

    fun clearFurnitureSeats(furniture: ItemDisplay) {
        val gearyFurniture = furniture.toGearyOrNull() ?: return
        gearyFurniture.get<BlockyAssociatedSeats>()?.seats?.forEach { it.remove() }
        gearyFurniture.remove<BlockyAssociatedSeats>()
    }

    internal fun ItemDisplay.handleFurnitureDrops(player: Player) {
        this.toGearyOrNull()?.get<BlockyDrops>()?.let { GenericHelpers.handleBlockDrop(it, player, location) }
    }

    internal suspend fun ItemDisplay.delayUntilTracked() = coroutineScope {
        async {
            while (toGearyOrNull() == null) delay(1)
            toGeary()
        }
    }.await()
}
