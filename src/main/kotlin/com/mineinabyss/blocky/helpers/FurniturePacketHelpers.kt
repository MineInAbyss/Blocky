@file:Suppress("UnstableApiUsage")

package com.mineinabyss.blocky.helpers

import com.comphenix.protocol.events.PacketContainer
import com.mineinabyss.blocky.api.BlockyFurnitures
import com.mineinabyss.blocky.api.BlockyFurnitures.isBlockyFurniture
import com.mineinabyss.blocky.api.BlockyFurnitures.isModelEngineFurniture
import com.mineinabyss.blocky.api.events.furniture.BlockyFurnitureInteractEvent
import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.features.BlockyLight
import com.mineinabyss.blocky.components.features.furniture.BlockyModelEngine
import com.mineinabyss.blocky.helpers.FurnitureHelpers.getLocations
import com.mineinabyss.blocky.helpers.GenericHelpers.toBlockCenterLocation
import com.mineinabyss.blocky.helpers.GenericHelpers.toEntity
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.protocolburrito.dsl.protocolManager
import com.mineinabyss.protocolburrito.dsl.sendTo
import com.mineinabyss.protocolburrito.packets.ServerboundPlayerActionPacketWrap
import com.mineinabyss.protocolburrito.packets.ServerboundUseItemOnPacketWrap
import com.ticxo.modelengine.api.ModelEngineAPI
import io.papermc.paper.math.Position
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.Vec3
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.data.type.Light
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.joml.Vector3f
import java.util.*

/**
 * Typealias to make it clear that this is a UUID for a furniture entity.
 */
typealias FurnitureUUID = UUID
object FurniturePacketHelpers {

    const val INTERACTION_WIDTH_ID = 8
    const val INTERACTION_HEIGHT_ID = 9
    const val ITEM_DISPLAY_ITEMSTACK_ID = 23

    val collisionHitboxPosMap = mutableMapOf<FurnitureUUID, MutableSet<BlockPos>>()
    val interactionHitboxIdMap = mutableMapOf<FurnitureUUID, Int>()
    val interactionHitboxPacketMap = mutableMapOf<FurnitureUUID, Pair<ClientboundAddEntityPacket, ClientboundSetEntityDataPacket?>>()
    val hitboxOutlineIdMap = mutableMapOf<ItemDisplay, Int>()

    fun getBaseFurnitureFromInteractionEntity(id: Int) =
        interactionHitboxIdMap.entries.firstOrNull { it.value == id }?.key?.toEntity() as? ItemDisplay

    fun getBaseFurnitureFromCollisionHitbox(pos: BlockPos) =
        collisionHitboxPosMap.entries.firstOrNull { pos in it.value }?.key?.toEntity() as? ItemDisplay

    internal fun registerPacketListeners() {

        protocolManager(blocky.plugin) {
            // Handles left-clicks and right-clicks with Interaction Barrier hitbox
            // Moved to [com/mineinabyss/blocky/listeners/BlockyFurnitureListener.kt:102]

            // Handles left-clicks with Collision Barrier hitbox
            onReceive<ServerboundPlayerActionPacketWrap> { wrap ->
                val baseFurniture = getBaseFurnitureFromCollisionHitbox(wrap.pos) ?: return@onReceive
                when ((wrap.handle as ServerboundPlayerActionPacket).action) {
                    ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK ->
                        Bukkit.getScheduler().callSyncMethod(blocky.plugin) {
                            BlockyFurnitures.removeFurniture(baseFurniture, player)
                        }

                    else -> {}
                }
            }
            // Handles right-clicks with Collision Barrier hitbox
            // Cancelled so client doesn't remove the "Ghost Block"
            onReceive<ServerboundUseItemOnPacketWrap> { wrap ->
                val baseFurniture = getBaseFurnitureFromCollisionHitbox(wrap.blockHit.blockPos) ?: return@onReceive
                isCancelled = true
                Bukkit.getScheduler().callSyncMethod(blocky.plugin) {
                    BlockyFurnitureInteractEvent(
                        baseFurniture, player,
                        EquipmentSlot.HAND, player.inventory.itemInMainHand,
                        null, null
                    ).callEvent()
                }
            }
        }
    }

    /**
     * Sends a packet to show the interaction hitbox of the given furniture to all players in the world.
     * @param furniture The furniture to show the interaction hitbox of.
     */
    fun sendInteractionEntityPacket(furniture: ItemDisplay) {
        // Don't send interactionEntity packet if modelengine furniture with hitbox
        if (furniture.isModelEngineFurniture) {
            val modelId = furniture.toGeary().get<BlockyModelEngine>()?.modelId ?: return
            val blueprint = ModelEngineAPI.getBlueprint(modelId) ?: return
            if (blueprint.mainHitbox != null || blueprint.subHitboxes.isNotEmpty()) return
        }
        furniture.world.players.forEach {
            sendInteractionEntityPacket(furniture, it)
        }
    }

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

        val interactionPackets = interactionHitboxPacketMap.computeIfAbsent(furniture.uniqueId) {
            val entityId = interactionHitboxIdMap.computeIfAbsent(furniture.uniqueId) { Entity.nextEntityId() }
            val loc = furniture.location.toBlockCenterLocation()
            val addEntityPacket = ClientboundAddEntityPacket(
                entityId, UUID.randomUUID(),
                loc.x, loc.y, loc.z, loc.pitch, loc.yaw,
                EntityType.INTERACTION, 0, Vec3.ZERO, 0.0
            )

            val hitbox = furniture.toGeary().get<BlockyFurniture>()?.interactionHitbox ?: return@computeIfAbsent addEntityPacket to null
            if (hitbox.width == 0f || hitbox.height == 0f) return@computeIfAbsent addEntityPacket to null
            val metadataPacket = ClientboundSetEntityDataPacket(
                entityId, listOf(
                    SynchedEntityData.DataValue(INTERACTION_WIDTH_ID, EntityDataSerializers.FLOAT, hitbox.width),
                    SynchedEntityData.DataValue(INTERACTION_HEIGHT_ID, EntityDataSerializers.FLOAT, hitbox.height)
                )
            )
            return@computeIfAbsent addEntityPacket to metadataPacket
        }

        PacketContainer.fromPacket(interactionPackets.first).sendTo(player)
        interactionPackets.second?.let { PacketContainer.fromPacket(it).sendTo(player)}
    }

    /**
     * Sends a packet to remove the interaction hitbox of the given furniture to all players in the world.
     * @param furniture The furniture to remove the interaction hitbox of.
     */
    fun removeInteractionHitboxPacket(furniture: ItemDisplay) {
        blocky.plugin.server.onlinePlayers.forEach { player ->
            removeInteractionHitboxPacket(furniture, player)
        }
        interactionHitboxIdMap.remove(furniture.uniqueId)
        interactionHitboxPacketMap.remove(furniture.uniqueId)
    }

    /**
     * Sends a packet to remove the interaction hitbox of the given furniture to the given player.
     * @param furniture The furniture to remove the interaction hitbox of.
     */
    fun removeInteractionHitboxPacket(furniture: ItemDisplay, player: Player) {
        PacketContainer.fromPacket(ClientboundRemoveEntitiesPacket(interactionHitboxIdMap[furniture.uniqueId] ?: return)).sendTo(player)
    }

    fun sendHitboxOutlinePacket(furniture: ItemDisplay) {
        furniture.world.players.forEach {
            sendHitboxOutlinePacket(furniture, it)
        }
    }

    val outlinePlayerMap = mutableMapOf<UUID, UUID>()
    fun sendHitboxOutlinePacket(furniture: ItemDisplay, player: Player) {
        if (outlinePlayerMap[player.uniqueId] == furniture.uniqueId) return
        removeHitboxOutlinePacket(player)
        outlinePlayerMap[player.uniqueId] = furniture.uniqueId
        val entityId = hitboxOutlineIdMap.computeIfAbsent(furniture) { Entity.nextEntityId() }
        val hitbox = furniture.toGeary().get<BlockyFurniture>()?.interactionHitbox ?: return
        if (hitbox.width == 0f || hitbox.height == 0f) return

        val loc = furniture.location.toBlockCenterLocation().apply { y += hitbox.height / 2 }
        val displayEntityPacket = ClientboundAddEntityPacket(
            entityId, UUID.randomUUID(),
            loc.x, loc.y, loc.z, loc.pitch, loc.yaw,
            EntityType.ITEM_DISPLAY, 0, Vec3.ZERO, 0.0
        )
        PacketContainer.fromPacket(displayEntityPacket).sendTo(player)
        val metadataPacket = ClientboundSetEntityDataPacket(
            entityId, listOf(
                SynchedEntityData.DataValue(12, EntityDataSerializers.VECTOR3, Vector3f(hitbox.width, hitbox.height, hitbox.width)),
                SynchedEntityData.DataValue(23, EntityDataSerializers.ITEM_STACK, CraftItemStack.asNMSCopy(hitbox.outline.toItemStack())),
                SynchedEntityData.DataValue(24, EntityDataSerializers.INT, furniture.itemDisplayTransform.ordinal)
            )
        )
        PacketContainer.fromPacket(metadataPacket).sendTo(player)

    }

    fun removeHitboxOutlinePacket(furniture: ItemDisplay) {
        furniture.world.players.forEach {
            removeHitboxOutlinePacket(furniture, it)
        }
    }

    fun removeHitboxOutlinePacket(furniture: ItemDisplay, player: Player) {
        val displayEntityPacket = ClientboundRemoveEntitiesPacket(hitboxOutlineIdMap[furniture] ?: return)
        PacketContainer.fromPacket(displayEntityPacket).sendTo(player)
        hitboxOutlineIdMap.remove(furniture)
        outlinePlayerMap.remove(player.uniqueId)
    }

    fun removeHitboxOutlinePacket(player: Player) {
        val furniture = outlinePlayerMap[player.uniqueId]?.toEntity() ?: return
        val displayEntityPacket = ClientboundRemoveEntitiesPacket(hitboxOutlineIdMap[furniture] ?: return)
        PacketContainer.fromPacket(displayEntityPacket).sendTo(player)
        outlinePlayerMap.remove(player.uniqueId)
    }

    /**
     * Sends a packet to show the collision hitbox of the given furniture to all players in the world.
     * @param baseEntity The furniture to show the collision hitbox of.
     */
    fun sendCollisionHitboxPacket(baseEntity: ItemDisplay) {
        baseEntity.world.players.forEach {
            sendCollisionHitboxPacket(baseEntity, it)
        }
    }

    /**
     * Sends a packet to show the collision hitbox of the given furniture to the given player.
     * @param baseEntity The furniture to show the collision hitbox of.
     */
    fun sendCollisionHitboxPacket(baseEntity: ItemDisplay, player: Player) {
        val furniture = baseEntity.toGeary().get<BlockyFurniture>() ?: return
        val positions = getLocations(
            baseEntity.yaw,
            baseEntity.location,
            furniture.collisionHitbox
        ).values.flatten().map { Position.block(it) }.associateWith { Material.BARRIER.createBlockData() }
            .toMutableMap()
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
        blocky.plugin.server.onlinePlayers.forEach {
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
        val positions = getLocations(baseEntity.yaw, baseEntity.location, furniture.collisionHitbox)
            .values.flatten().map { Position.block(it) }.associateWith { Material.AIR.createBlockData() }.toMutableMap()
        player.sendMultiBlockChange(positions)
    }

    /**
     * Sends the light packets for this furniture to all players in the world
     * @param baseEntity The furniture to send the light packets for
     */
    fun sendLightPacket(baseEntity: ItemDisplay) {
        baseEntity.world.players.forEach {
            sendLightPacket(baseEntity, it)
        }
    }

    /**
     * Sends the light packets for this furniture to a specific player
     * @param baseEntity The furniture to send the light packets for
     */
    fun sendLightPacket(baseEntity: ItemDisplay, player: Player) {
        val furniture = baseEntity.toGeary().get<BlockyFurniture>() ?: return
        val light = baseEntity.toGeary().get<BlockyLight>() ?: return

        val collisionHitboxPositions = getLocations(
            baseEntity.yaw, baseEntity.location, furniture.collisionHitbox
        ).values.flatten().map { Position.block(it) }.associateWith {
            Material.LIGHT.createBlockData {
                (it as Light).level = light.lightLevel
            }
        }.toMutableMap()

        player.sendMultiBlockChange(collisionHitboxPositions)
    }

    /**
     * Removes the light packets for this furniture to all players in the world
     * @param baseEntity The furniture to remove the light packets for
     */
    fun removeLightPacket(baseEntity: ItemDisplay) {
        blocky.plugin.server.onlinePlayers.forEach {
            removeLightPacket(baseEntity, it)
        }
    }

    /**
     * Removes the light packets for this furniture to a specific player
     * @param baseEntity The furniture to remove the light packets for
     */
    fun removeLightPacket(baseEntity: ItemDisplay, player: Player) {
        val furniture = baseEntity.toGeary().get<BlockyFurniture>() ?: return
        val collisionHitboxPositions =
            getLocations(baseEntity.yaw, baseEntity.location, furniture.collisionHitbox)
                .values.flatten().map { Position.block(it) }.associateWith { Material.AIR.createBlockData() }
                .toMutableMap()

        player.sendMultiBlockChange(collisionHitboxPositions)
    }
}
