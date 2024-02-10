package com.mineinabyss.blocky.helpers

import com.destroystokyo.paper.MaterialTags
import com.jeff_media.customblockdata.CustomBlockData
import com.mineinabyss.blocky.api.BlockyBlocks
import com.mineinabyss.blocky.api.BlockyBlocks.isBlockyBlock
import com.mineinabyss.blocky.api.BlockyFurnitures.isBlockyFurniture
import com.mineinabyss.blocky.api.events.block.BlockyBlockBreakEvent
import com.mineinabyss.blocky.api.events.block.BlockyBlockPlaceEvent
import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.VanillaNoteBlock
import com.mineinabyss.blocky.components.features.BlockyBreaking
import com.mineinabyss.blocky.components.features.BlockyDrops
import com.mineinabyss.blocky.components.features.BlockyPlacableOn
import com.mineinabyss.blocky.components.features.blocks.BlockyDirectional
import com.mineinabyss.blocky.components.features.mining.BlockyMining
import com.mineinabyss.blocky.components.features.mining.ToolType
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.spawning.spawn
import com.mineinabyss.idofront.util.randomOrMin
import io.th0rgal.protectionlib.ProtectionLib
import net.minecraft.core.BlockPos
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Chest
import org.bukkit.block.data.type.Fence
import org.bukkit.block.data.type.Stairs
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.ExperienceOrb
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.util.BoundingBox
import java.util.*
import kotlin.random.Random

const val VANILLA_STONE_PLACE = "blocky.stone.place"
const val VANILLA_STONE_BREAK = "blocky.stone.break"
const val VANILLA_STONE_HIT = "blocky.stone.hit"
const val VANILLA_STONE_STEP = "blocky.stone.step"
const val VANILLA_STONE_FALL = "blocky.stone.fall"

const val VANILLA_WOOD_PLACE = "blocky.wood.place"
const val VANILLA_WOOD_BREAK = "blocky.wood.break"
const val VANILLA_WOOD_HIT = "blocky.wood.hit"
const val VANILLA_WOOD_STEP = "blocky.wood.step"
const val VANILLA_WOOD_FALL = "blocky.wood.fall"

const val DEFAULT_PLACE_VOLUME = 1.0f
const val DEFAULT_PLACE_PITCH = 0.8f
const val DEFAULT_BREAK_VOLUME = 1.0f
const val DEFAULT_BREAK_PITCH = 0.8f
const val DEFAULT_HIT_VOLUME = 0.25f
const val DEFAULT_HIT_PITCH = 0.5f
const val DEFAULT_STEP_VOLUME = 0.15f
const val DEFAULT_STEP_PITCH = 1.0f
const val DEFAULT_FALL_VOLUME = 0.5f
const val DEFAULT_FALL_PITCH = 0.75f

val Block.persistentDataContainer get() = customBlockData as PersistentDataContainer
val Block.customBlockData get() = CustomBlockData(this, blocky.plugin)
fun Block.toBlockPos() = BlockPos(this.x, this.y, this.z)

internal infix fun <A, B, C> Pair<A, B>.to(that: C): Triple<A, B, C> = Triple(this.first, this.second, that)
internal inline fun <reified T> ItemStack.decode(): T? = this.itemMeta?.persistentDataContainer?.decode()
internal val Player.gearyInventory get() = inventory.toGeary()

fun placeBlockyBlock(
    player: Player,
    hand: EquipmentSlot,
    item: ItemStack,
    against: Block,
    face: BlockFace,
    newData: BlockData
) {
    val targetBlock = if (against.isReplaceable) against else against.getRelative(face)
    if (!targetBlock.type.isAir && !targetBlock.isLiquid && targetBlock.type != Material.LIGHT) return
    if (!against.isBlockyBlock && !newData.isBlockyBlock) return
    if (GenericHelpers.entityStandingInside(targetBlock) || against.isVanillaNoteBlock) return

    if (targetBlock.isVanillaNoteBlock)
        targetBlock.persistentDataContainer.encode(VanillaNoteBlock(0))

    val blockPlaceEvent = BlockPlaceEvent(targetBlock, targetBlock.state, against, item, player, true, hand)

    when {
        newData.toGearyOrNull()?.get<BlockyPlacableOn>()?.isPlacableOn(targetBlock, face) == true ->
            blockPlaceEvent.isCancelled = true

        !ProtectionLib.canBuild(player, targetBlock.location) ->
            blockPlaceEvent.isCancelled = true
    }

    if (!blockPlaceEvent.callEvent() || !blockPlaceEvent.canBuild() ||
        !BlockyBlockPlaceEvent(targetBlock, player, hand, item).callEvent()
    ) return

    // if new block is a blocky block, place it via API
    // if not it is a vanilla block placed against a blocky block, and we place it via NMS methods
    if (newData.toGearyOrNull()?.has<PrefabKey>() == true) {
        BlockyBlocks.placeBlockyBlock(targetBlock.location, newData)
        if (player.gameMode != GameMode.CREATIVE) item.subtract(1)
    } else BlockStateCorrection.placeItemAsBlock(player, hand, item)

    targetBlock.world.sendGameEvent(null, GameEvent.BLOCK_PLACE, targetBlock.location.toVector())
}

internal fun attemptBreakBlockyBlock(block: Block, player: Player? = null): Boolean {
    player?.let {
        if (!BlockyBlockBreakEvent(block, player).callEvent()) return false
        if (!ProtectionLib.canBreak(it, block.location)) return false
        if (player.gameMode != GameMode.CREATIVE)
            player.inventory.itemInMainHand.damage(1, player)
        handleBlockyDrops(block, player)
    }

    block.toGearyOrNull() ?: return false

    block.customBlockData.clear()
    block.type = Material.AIR
    block.world.playEffect(block.location, Effect.STEP_SOUND, block.blockData)
    block.world.sendGameEvent(null, GameEvent.BLOCK_DESTROY, block.location.toVector())
    return true
}

fun handleBlockyDrops(block: Block, player: Player) {
    val gearyBlock = block.toGearyOrNull() ?: return
    val drop = gearyBlock.get<BlockyDrops>() ?: return
    if (!gearyBlock.has<SetBlock>()) return

    if (drop.onlyDropWithCorrectTool && !GenericHelpers.isCorrectTool(player, block, EquipmentSlot.HAND)) return
    GenericHelpers.handleBlockDrop(drop, player, block.location)
}

object GenericHelpers {

    fun blockStandingOn(entity: LivingEntity): Block {
        val block = entity.location.block
        val blockBelow = block.getRelative(BlockFace.DOWN)
        if (!block.type.isAir) return block
        if (!blockBelow.type.isAir) return blockBelow


        // Expand players hitbox by 0.3, which is the maximum size a player can be off a block
        // Whilst not falling off
        val entityBox = entity.boundingBox.expand(0.3)
        for (face in BlockFace.entries) {
            if (!face.isCartesian || face.modY != 0) continue
            val relative = blockBelow.getRelative(face)
            if (relative.type.isAir) continue
            if (relative.boundingBox.overlaps(entityBox)) return relative
        }

        return blockBelow
    }

    fun entityStandingInside(block: Block) =
        block.world.getNearbyEntities(BoundingBox.of(block.location.toCenterLocation(), 0.5, 0.5, 0.5))
            .any { it is LivingEntity && (it !is Player || it.gameMode != GameMode.SPECTATOR) }

    fun Block.isInteractable() = when {
        isBlockyBlock || isBlockyFurniture || CaveVineHelpers.isBlockyCaveVine(this) -> false
        blockData is Stairs || blockData is Fence -> false
        !type.isInteractable || type in setOf(
            Material.PUMPKIN,
            Material.MOVING_PISTON,
            Material.REDSTONE_ORE,
            Material.REDSTONE_WIRE
        ) -> false

        else -> true
    }

    fun directionalId(gearyEntity: GearyEntity, face: BlockFace, player: Player?): Int {
        return gearyEntity.get<BlockyDirectional>()?.let { directional ->
            if (directional.isLogType) {
                return when (face) {
                    BlockFace.UP, BlockFace.DOWN -> directional.yBlock?.toEntityOrNull() ?: gearyEntity
                    BlockFace.NORTH, BlockFace.SOUTH -> directional.xBlock?.toEntityOrNull() ?: gearyEntity
                    BlockFace.WEST, BlockFace.EAST -> directional.zBlock?.toEntityOrNull() ?: gearyEntity
                    else -> gearyEntity
                }.get<SetBlock>()?.blockId ?: 0
            } else {
                return when ((player?.directionalRelative(directional) ?: face)) {
                    BlockFace.NORTH -> directional.northBlock?.toEntityOrNull() ?: gearyEntity
                    BlockFace.SOUTH -> directional.southBlock?.toEntityOrNull() ?: gearyEntity
                    BlockFace.WEST -> directional.westBlock?.toEntityOrNull() ?: gearyEntity
                    BlockFace.EAST -> directional.eastBlock?.toEntityOrNull() ?: gearyEntity
                    BlockFace.UP -> directional.upBlock?.toEntityOrNull() ?: gearyEntity
                    BlockFace.DOWN -> directional.downBlock?.toEntityOrNull() ?: gearyEntity
                    else -> gearyEntity
                }.get<SetBlock>()?.blockId ?: 0
            }
        } ?: gearyEntity.get<SetBlock>()?.blockId ?: 0
    }

    private fun Player.directionalRelative(directional: BlockyDirectional) = when {
        directional.isLogType -> null
        directional.isDropperType && pitch >= 45 -> BlockFace.UP
        directional.isDropperType && pitch <= -45 -> BlockFace.DOWN
        else -> relativeBlockFace(yaw.toInt())
    }

    fun relativeBlockFace(yaw: Int) = when (yaw) {
        in 45..135, in -315..-225 -> BlockFace.EAST
        in 135..225, in -225..-135 -> BlockFace.SOUTH
        in 225..315, in -135..-45 -> BlockFace.WEST
        else -> BlockFace.NORTH
    }

    fun leftBlock(block: Block, player: Player): Block {
        val leftBlock = when (player.facing) {
            BlockFace.NORTH -> block.getRelative(BlockFace.WEST)
            BlockFace.SOUTH -> block.getRelative(BlockFace.EAST)
            BlockFace.WEST -> block.getRelative(BlockFace.SOUTH)
            BlockFace.EAST -> block.getRelative(BlockFace.NORTH)
            else -> block
        }
        return if (leftBlock.blockData is Chest && (leftBlock.blockData as Chest).facing != player.facing.oppositeFace) block
        else leftBlock
    }

    fun rightBlock(block: Block, player: Player): Block {
        val rightBlock = when (player.facing) {
            BlockFace.NORTH -> block.getRelative(BlockFace.EAST)
            BlockFace.SOUTH -> block.getRelative(BlockFace.WEST)
            BlockFace.WEST -> block.getRelative(BlockFace.NORTH)
            BlockFace.EAST -> block.getRelative(BlockFace.SOUTH)
            else -> block
        }
        return if (rightBlock.blockData is Chest && (rightBlock.blockData as Chest).facing != player.facing.oppositeFace) block
        else rightBlock
    }

    /**
     * @return A new location at the bottom-center of a block
     */
    fun Location.toBlockCenterLocation() = clone().toCenterLocation().apply { y -= 0.5 }

    fun isCorrectTool(player: Player, block: Block, hand: EquipmentSlot): Boolean {
        val acceptedToolTypes = block.toGearyOrNull()?.let { geary ->
            geary.get<BlockyDrops>()?.acceptedToolTypes
                ?: geary.get<BlockyBreaking>()?.modifiers?.heldTypes?.map { it.toolType }
        } ?: return false
        val heldToolTypes = player.gearyInventory?.get(hand)?.get<BlockyMining>()?.toolTypes
            ?: vanillaToolTypes(player.inventory.getItem(hand))?.let { setOf(it) } ?: setOf()

        return ToolType.ANY in acceptedToolTypes || acceptedToolTypes.any { it in heldToolTypes }
    }

    fun vanillaToolTypes(itemStack: ItemStack) = when {
        MaterialTags.AXES.isTagged(itemStack.type) -> ToolType.AXE
        MaterialTags.PICKAXES.isTagged(itemStack.type) -> ToolType.PICKAXE
        MaterialTags.SWORDS.isTagged(itemStack.type) -> ToolType.SWORD
        MaterialTags.SHOVELS.isTagged(itemStack.type) -> ToolType.SHOVEL
        MaterialTags.HOES.isTagged(itemStack.type) -> ToolType.HOE
        itemStack.type == Material.SHEARS -> ToolType.SHEARS
        else -> null
    }

    fun handleBlockDrop(blockyDrop: BlockyDrops, player: Player, location: Location) {
        if (player.gameMode == GameMode.CREATIVE) return

        blockyDrop.drops.forEach { drop ->
            val hand = player.inventory.itemInMainHand
            val item = when (Enchantment.SILK_TOUCH) {
                in hand.enchantments -> drop.silkTouchedDrop?.toItemStack()
                else -> drop.item?.toItemStack()
            }
            val amount = when {
                drop.affectedByFortune && Enchantment.LOOT_BONUS_BLOCKS in hand.enchantments ->
                    drop.amount.randomOrMin() * Random.nextInt(
                        1,
                        hand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) + 1
                    )

                else -> drop.amount.randomOrMin()
            }

            if (drop.exp > 0) location.spawn<ExperienceOrb> { experience = drop.exp }
            item?.let {
                item.amount = amount
                location.world.dropItemNaturally(location, item)
            }
        }
    }

    fun UUID.toEntity() = Bukkit.getEntity(this)
}
