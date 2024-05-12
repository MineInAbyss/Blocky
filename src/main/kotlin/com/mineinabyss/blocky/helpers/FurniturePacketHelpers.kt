@file:Suppress("UnstableApiUsage")

package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.api.BlockyFurnitures.isBlockyFurniture
import com.mineinabyss.blocky.api.BlockyFurnitures.isModelEngineFurniture
import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.features.BlockyLight
import com.mineinabyss.blocky.components.features.furniture.BlockyModelEngine
import com.mineinabyss.blocky.helpers.FurnitureHelpers.collisionHitboxPositions
import com.mineinabyss.blocky.helpers.GenericHelpers.toEntity
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.ticxo.modelengine.api.ModelEngineAPI
import io.papermc.paper.math.BlockPosition
import it.unimi.dsi.fastutil.ints.IntList
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundBundlePacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.Vec3
import org.bukkit.Material
import org.bukkit.block.data.type.Light
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.joml.Vector3f
import java.util.*

/**
 * Typealias to make it clear that this is a UUID for a furniture entity.
 */
typealias FurnitureUUID = UUID
data class FurnitureSubEntity(val furnitureUUID: FurnitureUUID, val entityIds: IntList) {
    val furniture get() = furnitureUUID.toEntity() as? ItemDisplay
}
data class FurnitureSubEntityPacket(val entityId: Int, val addEntity: ClientboundAddEntityPacket, val metadata: ClientboundSetEntityDataPacket) {
    fun bundlePacket(): ClientboundBundlePacket {
        return ClientboundBundlePacket(listOf(addEntity, metadata))
    }
}
object FurniturePacketHelpers {

    const val INTERACTION_WIDTH_ID = 8
    const val INTERACTION_HEIGHT_ID = 9
    const val ITEM_DISPLAY_ITEMSTACK_ID = 23

    private val collisionHitboxPosMap = mutableMapOf<FurnitureUUID, MutableSet<BlockPos>>()
    private val interactionHitboxIds = mutableSetOf<FurnitureSubEntity>()
    private val interactionHitboxPacketMap = mutableMapOf<FurnitureUUID, MutableSet<FurnitureSubEntityPacket>>()
    private val outlineIds = mutableSetOf<FurnitureSubEntity>()
    private val outlinePacketMap = mutableMapOf<FurnitureUUID, MutableSet<FurnitureSubEntityPacket>>()
    private val outlinePlayerMap = mutableMapOf<UUID, UUID>()

    fun baseFurnitureFromInteractionHitbox(id: Int) =
        interactionHitboxIds.firstOrNull { id in it.entityIds }?.furniture

    fun baseFurnitureFromCollisionHitbox(pos: BlockPos) =
        collisionHitboxPosMap.entries.firstOrNull { pos in it.value }?.key?.toEntity() as? ItemDisplay

    /**
     * Sends a packet to show the interaction hitbox of the given furniture to the given player.
     * @param furniture The furniture to show the interaction hitbox of.
     */
    fun sendInteractionEntityPacket(furniture: ItemDisplay, player: Player) {
        if (!furniture.isBlockyFurniture) return
        // Don't send interactionEntity packet if modelengine furniture with hitbox
        if (furniture.isModelEngineFurniture) {
            val modelId = furniture.toGeary().get<BlockyModelEngine>()?.modelId ?: return
            val blueprint = ModelEngineAPI.getBlueprint(modelId) ?: return
            if (blueprint.mainHitbox != null || blueprint.subHitboxes.isNotEmpty()) return
        }

        val interactionHitboxes = furniture.toGeary().get<BlockyFurniture>()?.interactionHitbox ?: return
            interactionHitboxPacketMap.computeIfAbsent(furniture.uniqueId) {
                val entityIds = interactionHitboxIds.firstOrNull { it.furnitureUUID == furniture.uniqueId }?.entityIds ?: List(interactionHitboxes.size) { Entity.nextEntityId() }.apply {
                    interactionHitboxIds += FurnitureSubEntity(furniture.uniqueId, IntList.of(*toIntArray()))
                }
                mutableSetOf<FurnitureSubEntityPacket>().apply {
                    interactionHitboxes.zip(entityIds).forEach { (hitbox, entityId) ->
                        val loc = hitbox.location(furniture)
                        val addEntityPacket = ClientboundAddEntityPacket(
                            entityId, UUID.randomUUID(),
                            loc.x, loc.y, loc.z, loc.pitch, loc.yaw,
                            EntityType.INTERACTION, 0, Vec3.ZERO, 0.0
                        )

                        val metadataPacket = ClientboundSetEntityDataPacket(
                            entityId, listOf(
                                SynchedEntityData.DataValue(INTERACTION_WIDTH_ID, EntityDataSerializers.FLOAT, hitbox.width),
                                SynchedEntityData.DataValue(INTERACTION_HEIGHT_ID, EntityDataSerializers.FLOAT, hitbox.height)
                            )
                        )

                        add(FurnitureSubEntityPacket(entityId, addEntityPacket, metadataPacket))
                    }
                }
            }.forEach { (player as CraftPlayer).handle.connection.send(it.bundlePacket()) }
    }

    /**
     * Sends a packet to remove the interaction hitbox of the given furniture to all players in the world.
     * @param furniture The furniture to remove the interaction hitbox of.
     */
    fun removeInteractionHitboxPacket(furniture: ItemDisplay) {
        furniture.world.players.forEach { player ->
            removeInteractionHitboxPacket(furniture, player)
        }
        interactionHitboxIds.removeIf { it.furnitureUUID == furniture.uniqueId }
        interactionHitboxPacketMap.remove(furniture.uniqueId)
    }

    /**
     * Sends a packet to remove the interaction hitbox of the given furniture to the given player.
     * @param furniture The furniture to remove the interaction hitbox of.
     */
    fun removeInteractionHitboxPacket(furniture: ItemDisplay, player: Player) {
        val entityIds = interactionHitboxIds.firstOrNull { it.furnitureUUID == furniture.uniqueId }?.entityIds ?: return
        (player as CraftPlayer).handle.connection.send(ClientboundRemoveEntitiesPacket(*entityIds.toIntArray()))
    }

    fun sendHitboxOutlinePacket(furniture: ItemDisplay, player: Player) {
        if (outlinePlayerMap[player.uniqueId] == furniture.uniqueId) return
        removeHitboxOutlinePacket(player)
        outlinePlayerMap[player.uniqueId] = furniture.uniqueId

        val interactionHitboxes = furniture.toGeary().get<BlockyFurniture>()?.interactionHitbox ?: return
        val outlineType = blocky.config.furniture.hitboxOutlines.entityType() ?: return
        val outlineContent = blocky.config.furniture.hitboxOutlines.outlineContent() ?: return
        val entityIds = outlineIds.firstOrNull { it.furnitureUUID == furniture.uniqueId }?.entityIds ?: List(interactionHitboxes.size) { Entity.nextEntityId() }.apply {
            outlineIds += FurnitureSubEntity(furniture.uniqueId, IntList.of(*toIntArray()))
        }

        outlinePacketMap.computeIfAbsent(furniture.uniqueId) {
            mutableSetOf<FurnitureSubEntityPacket>().apply {
                interactionHitboxes.zip(entityIds).forEach { (hitbox, entityId) ->
                    val loc = hitbox.location(furniture).add(0.0,hitbox.height / 2.0, 0.0).apply {
                        if (blocky.config.furniture.hitboxOutlines.type == FurnitureOutlineType.BLOCK)
                            toBlockLocation()
                    }
                    val addEntityPacket = ClientboundAddEntityPacket(
                        entityId, UUID.randomUUID(),
                        loc.x, loc.y, loc.z, 0.0f, 0.0f,
                        outlineType, 0, Vec3.ZERO, 0.0
                    )

                    val metadataPacket = ClientboundSetEntityDataPacket(
                        entityId, listOf(
                            outlineContent,
                            SynchedEntityData.DataValue(12, EntityDataSerializers.VECTOR3, Vector3f(hitbox.width, hitbox.height, hitbox.width)),
                            SynchedEntityData.DataValue(24, EntityDataSerializers.INT, furniture.itemDisplayTransform.ordinal)
                        )
                    )

                    add(FurnitureSubEntityPacket(entityId, addEntityPacket, metadataPacket))
                }
            }
        }.forEach { (player as CraftPlayer).handle.connection.send(ClientboundBundlePacket(listOf(it.addEntity, it.metadata))) }
    }

    fun removeHitboxOutlinePacket(furniture: ItemDisplay) {
        furniture.world.players.forEach {
            removeHitboxOutlinePacket(furniture, it)
        }
    }

    fun removeHitboxOutlinePacket(furniture: ItemDisplay, player: Player) {
        val displayEntityPacket = ClientboundRemoveEntitiesPacket(outlineIds.firstOrNull { it.furnitureUUID == furniture.uniqueId }?.entityIds ?: return)
        (player as CraftPlayer).handle.connection.send(displayEntityPacket)
        outlineIds.removeIf { it.furnitureUUID == furniture.uniqueId }
        outlinePlayerMap.remove(player.uniqueId)
    }

    fun removeHitboxOutlinePacket(player: Player) {
        val entityIds = outlineIds.firstOrNull { it.furnitureUUID == (outlinePlayerMap[player.uniqueId] ?: return) }?.entityIds ?: return
        val displayEntityPacket = ClientboundRemoveEntitiesPacket(entityIds)
        (player as CraftPlayer).handle.connection.send(displayEntityPacket)
        outlinePlayerMap.remove(player.uniqueId)
    }


    /**
     * Sends a packet to show the collision hitbox of the given furniture to the given player.
     * @param baseEntity The furniture to show the collision hitbox of.
     */
    fun sendCollisionHitboxPacket(baseEntity: ItemDisplay, player: Player) {
        val furniture = baseEntity.toGeary().get<BlockyFurniture>() ?: return
        val positions = collisionHitboxPositions(baseEntity.yaw, baseEntity.location, furniture.collisionHitbox)
            .associateWith { Material.BARRIER.createBlockData() }.toMutableMap()
        player.sendMultiBlockChange(positions)
        positions.map { it.key.toBlock() }.forEach {
            collisionHitboxPosMap.compute(baseEntity.uniqueId) { _, blockPos ->
                blockPos?.plus(BlockPos(it.blockX(), it.blockY(), it.blockZ()))?.toMutableSet()
                    ?: mutableSetOf(BlockPos(it.blockX(), it.blockY(), it.blockZ()))
            }
        }
    }

    /**
     * Sends a packet to remove the collision hitbox of the given furniture to all players in the world.
     * @param baseEntity The furniture to remove the collision hitbox of.
     */
    fun removeCollisionHitboxPacket(baseEntity: ItemDisplay) {
        baseEntity.world.players.forEach {
            removeCollisionHitboxPacket(baseEntity, it)
        }
        collisionHitboxPosMap.remove(baseEntity.uniqueId)
    }

    /**
     * Sends a packet to remove the collision hitbox of the given furniture to the given player.
     * @param baseEntity The furniture to remove the collision hitbox of.
     */
    fun removeCollisionHitboxPacket(baseEntity: ItemDisplay, player: Player) {
        val furniture = baseEntity.toGeary().get<BlockyFurniture>() ?: return
        val positions = collisionHitboxPositions(baseEntity.yaw, baseEntity.location, furniture.collisionHitbox)
            .associateWith { Material.AIR.createBlockData() }.toMutableMap()
        player.sendMultiBlockChange(positions)
    }

    /**
     * Sends the light packets for this furniture to a specific player
     * @param baseEntity The furniture to send the light packets for
     */
    fun sendLightPacket(baseEntity: ItemDisplay, player: Player) {
        val furniture = baseEntity.toGeary().get<BlockyFurniture>() ?: return
        val light = baseEntity.toGeary().get<BlockyLight>() ?: return

        val collisionHitboxPositions = collisionHitboxPositions(
            baseEntity.yaw, baseEntity.location, furniture.collisionHitbox
        ).associateWith {
            Material.LIGHT.createBlockData { (it as Light).level = light.lightLevel }
        }.toMutableMap()

        player.sendMultiBlockChange(collisionHitboxPositions)
    }

    /**
     * Removes the light packets for this furniture to all players in the world
     * @param baseEntity The furniture to remove the light packets for
     */
    fun removeLightPacket(baseEntity: ItemDisplay) {
        baseEntity.world.players.forEach {
            removeLightPacket(baseEntity, it)
        }
    }

    /**
     * Removes the light packets for this furniture to a specific player
     * @param baseEntity The furniture to remove the light packets for
     */
    fun removeLightPacket(baseEntity: ItemDisplay, player: Player) {
        val furniture = baseEntity.toGeary().get<BlockyFurniture>() ?: return
        val collisionHitboxPositions = collisionHitboxPositions(baseEntity.yaw, baseEntity.location, furniture.collisionHitbox)
            .associateWith { Material.AIR.createBlockData() }.toMutableMap()

        player.sendMultiBlockChange(collisionHitboxPositions)
    }
}
