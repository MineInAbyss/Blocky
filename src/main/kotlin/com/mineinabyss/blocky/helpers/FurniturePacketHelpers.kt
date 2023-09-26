@file:Suppress("UnstableApiUsage")

package com.mineinabyss.blocky.helpers

import com.comphenix.protocol.events.PacketContainer
import com.mineinabyss.blocky.api.BlockyFurnitures
import com.mineinabyss.blocky.api.events.furniture.BlockyFurnitureInteractEvent
import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.features.BlockyLight
import com.mineinabyss.blocky.helpers.FurnitureHelpers.getLocations
import com.mineinabyss.blocky.helpers.GenericHelpers.toBlockCenterLocation
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.protocolburrito.dsl.protocolManager
import com.mineinabyss.protocolburrito.dsl.sendTo
import com.mineinabyss.protocolburrito.packets.ServerboundPlayerActionPacketWrap
import com.mineinabyss.protocolburrito.packets.ServerboundUseItemOnPacketWrap
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
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import java.util.*

object FurniturePacketHelpers {

    val collisionHitboxPosMap = mutableMapOf<ItemDisplay, MutableSet<BlockPos>>()
    val interactionHitboxIdMap = mutableMapOf<ItemDisplay, Int>()

    fun getBaseFurnitureFromInteractionEntity(id: Int) =
        interactionHitboxIdMap.entries.firstOrNull { it.value == id }?.key

    fun getBaseFurnitureFromCollisionHitbox(pos: BlockPos) =
        collisionHitboxPosMap.entries.firstOrNull { pos in it.value }?.key

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
                BlockyFurnitureInteractEvent(
                    baseFurniture, player,
                    EquipmentSlot.HAND, player.inventory.itemInMainHand,
                    null, null
                ).callEvent()
            }
        }
    }

    /**
     * Sends a packet to show the interaction hitbox of the given furniture to all players in the world.
     * @param furniture The furniture to show the interaction hitbox of.
     */
    internal fun sendInteractionEntityPacket(furniture: ItemDisplay) {
        furniture.world.players.forEach {
            sendInteractionEntityPacket(furniture, it)
        }
    }

    /**
     * Sends a packet to show the interaction hitbox of the given furniture to the given player.
     * @param furniture The furniture to show the interaction hitbox of.
     */
    internal fun sendInteractionEntityPacket(furniture: ItemDisplay, player: Player) {
        val entityId = interactionHitboxIdMap.computeIfAbsent(furniture) { Entity.nextEntityId() }

        val loc = furniture.location.toBlockCenterLocation()
        val interactionPacket = ClientboundAddEntityPacket(
            entityId, UUID.randomUUID(),
            loc.x, loc.y, loc.z, loc.pitch, loc.yaw,
            EntityType.INTERACTION, 0, Vec3.ZERO, 0.0
        )
        PacketContainer.fromPacket(interactionPacket).sendTo(player)
        val hitbox = furniture.toGeary().get<BlockyFurniture>()?.interactionHitbox ?: return
        if (hitbox.width == 0f || hitbox.height == 0f) return
        val metadataPacket = ClientboundSetEntityDataPacket(
            entityId, listOf(
                SynchedEntityData.DataValue(8, EntityDataSerializers.FLOAT, hitbox.width),
                SynchedEntityData.DataValue(9, EntityDataSerializers.FLOAT, hitbox.height)
            )
        )
        PacketContainer.fromPacket(metadataPacket).sendTo(player)

    }

    /**
     * Sends a packet to remove the interaction hitbox of the given furniture to all players in the world.
     * @param furniture The furniture to remove the interaction hitbox of.
     */
    internal fun removeInteractionHitboxPacket(furniture: ItemDisplay) {
        furniture.world.players.forEach {
            removeInteractionHitboxPacket(furniture, it)
        }
    }

    /**
     * Sends a packet to remove the interaction hitbox of the given furniture to the given player.
     * @param furniture The furniture to remove the interaction hitbox of.
     */
    internal fun removeInteractionHitboxPacket(furniture: ItemDisplay, player: Player) {
        val interactionPacket = ClientboundRemoveEntitiesPacket(interactionHitboxIdMap[furniture] ?: return)

        PacketContainer.fromPacket(interactionPacket).sendTo(player)
        interactionHitboxIdMap.remove(furniture)
    }

    /**
     * Sends a packet to show the collision hitbox of the given furniture to all players in the world.
     * @param baseEntity The furniture to show the collision hitbox of.
     */
    internal fun sendCollisionHitboxPacket(baseEntity: ItemDisplay) {
        baseEntity.world.players.forEach {
            sendCollisionHitboxPacket(baseEntity, it)
        }
    }

    /**
     * Sends a packet to show the collision hitbox of the given furniture to the given player.
     * @param baseEntity The furniture to show the collision hitbox of.
     */
    internal fun sendCollisionHitboxPacket(baseEntity: ItemDisplay, player: Player) {
        val furniture = baseEntity.toGeary().get<BlockyFurniture>() ?: return
        val positions = getLocations(
            baseEntity.yaw,
            baseEntity.location,
            furniture.collisionHitbox
        ).values.flatten().map { Position.block(it) }.associateWith { Material.BARRIER.createBlockData() }.toMutableMap()
        player.sendMultiBlockChange(positions)
        positions.map { it.key.toBlock() }.forEach {
            collisionHitboxPosMap.compute(baseEntity) { _, blockPos ->
                blockPos?.plus(BlockPos(it.blockX(), it.blockY(), it.blockZ()))?.toMutableSet()
                    ?: mutableSetOf(BlockPos(it.blockX(), it.blockY(), it.blockZ()))
            }
        }
    }

    /**
     * Sends a packet to remove the collision hitbox of the given furniture to all players in the world.
     * @param baseEntity The furniture to remove the collision hitbox of.
     */
    internal fun removeCollisionHitboxPacket(baseEntity: ItemDisplay) {
        baseEntity.world.players.forEach {
            removeCollisionHitboxPacket(baseEntity, it)
        }
    }

    /**
     * Sends a packet to remove the collision hitbox of the given furniture to the given player.
     * @param baseEntity The furniture to remove the collision hitbox of.
     */
    private fun removeCollisionHitboxPacket(baseEntity: ItemDisplay, player: Player) {
        val furniture = baseEntity.toGeary().get<BlockyFurniture>() ?: return
        val positions = getLocations(baseEntity.yaw, baseEntity.location, furniture.collisionHitbox)
            .values.flatten().map { Position.block(it) }.associateWith { Material.AIR.createBlockData() }.toMutableMap()
        player.sendMultiBlockChange(positions)
        positions.forEach {
            collisionHitboxPosMap.remove(baseEntity)
        }
    }

    /**
     * Sends the light packets for this furniture to all players in the world
     * @param baseEntity The furniture to send the light packets for
     */
    internal fun sendLightPacket(baseEntity: ItemDisplay) {
        baseEntity.world.players.forEach {
            sendLightPacket(baseEntity, it)
        }
    }

    /**
     * Sends the light packets for this furniture to a specific player
     * @param baseEntity The furniture to send the light packets for
     */
    internal fun sendLightPacket(baseEntity: ItemDisplay, player: Player) {
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
    internal fun removeLightPacket(baseEntity: ItemDisplay) {
        baseEntity.world.players.forEach {
            removeLightPacket(baseEntity, it)
        }
    }

    /**
     * Removes the light packets for this furniture to a specific player
     * @param baseEntity The furniture to remove the light packets for
     */
    private fun removeLightPacket(baseEntity: ItemDisplay, player: Player) {
        val furniture = baseEntity.toGeary().get<BlockyFurniture>() ?: return
        val collisionHitboxPositions =
            getLocations(baseEntity.yaw, baseEntity.location, furniture.collisionHitbox)
                .values.flatten().map { Position.block(it) }.associateWith { Material.AIR.createBlockData() }.toMutableMap()

        player.sendMultiBlockChange(collisionHitboxPositions)
    }
}
