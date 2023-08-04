package com.mineinabyss.blocky.helpers

import com.destroystokyo.paper.MaterialTags
import com.jeff_media.customblockdata.CustomBlockData
import com.mineinabyss.blocky.api.BlockyBlocks.isBlockyBlock
import com.mineinabyss.blocky.api.BlockyFurnitures.isBlockyFurniture
import com.mineinabyss.blocky.api.events.block.BlockyBlockBreakEvent
import com.mineinabyss.blocky.api.events.block.BlockyBlockPlaceEvent
import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.VanillaNoteBlock
import com.mineinabyss.blocky.components.features.BlockyBreaking
import com.mineinabyss.blocky.components.features.BlockyDrops
import com.mineinabyss.blocky.components.features.BlockyLight
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
import com.mineinabyss.idofront.spawning.spawn
import com.mineinabyss.idofront.util.randomOrMin
import io.th0rgal.protectionlib.ProtectionLib
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Chest
import org.bukkit.block.data.type.Fence
import org.bukkit.block.data.type.Stairs
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.ExperienceOrb
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import java.util.*
import kotlin.random.Random
import kotlin.time.Duration

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

val BlockFace.isCardinal get() = this == BlockFace.NORTH || this == BlockFace.EAST || this == BlockFace.SOUTH || this == BlockFace.WEST
val Block.persistentDataContainer get() = customBlockData as PersistentDataContainer
val Block.customBlockData get() = CustomBlockData(this, blocky.plugin)

internal fun minOf(duration: Duration, duration2: Duration) = if (duration < duration2) duration else duration2
internal infix fun <A, B, C> Pair<A, B>.to(that: C): Triple<A, B, C> = Triple(this.first, this.second, that)
internal inline fun <reified T> ItemStack.decode(): T? = this.itemMeta.persistentDataContainer.decode()
internal val Player.gearyInventory get() = inventory.toGeary()

fun placeBlockyBlock(
    player: Player,
    hand: EquipmentSlot,
    item: ItemStack,
    against: Block,
    face: BlockFace,
    newData: BlockData
): Block? {
    val targetBlock = if (against.isReplaceable) against else against.getRelative(face)

    if (!targetBlock.type.isAir && !targetBlock.isLiquid && targetBlock.type != Material.LIGHT) return null
    if (!against.isBlockyBlock && player.gearyInventory?.get(hand)?.has<SetBlock>() != true) return null
    if (player.isInBlock(targetBlock)) return null
    if (against.isVanillaNoteBlock) return null

    if (!blocky.config.noteBlocks.restoreFunctionality && targetBlock.isVanillaNoteBlock)
        targetBlock.persistentDataContainer.encode(VanillaNoteBlock(0))

    val currentData = targetBlock.blockData
    val isFlowing = newData.material == Material.WATER || newData.material == Material.LAVA
    targetBlock.setBlockData(newData, isFlowing)

    val blockPlaceEvent = BlockPlaceEvent(targetBlock, targetBlock.state, against, item, player, true, hand)
    blockPlaceEvent.callEvent()

    when {
        targetBlock.toGearyOrNull()?.get<BlockyPlacableOn>()?.isPlacableOn(targetBlock, face) == true -> blockPlaceEvent.isCancelled = true

        !ProtectionLib.canBuild(player, targetBlock.location) || !blockPlaceEvent.canBuild() ->
            blockPlaceEvent.isCancelled = true

        !BlockStateCorrection.correctAllBlockStates(targetBlock, player, face, item) -> blockPlaceEvent.isCancelled = true
    }

    val blockyEvent = BlockyBlockPlaceEvent(targetBlock, player, hand, item)
    blockyEvent.callEvent()
    if (blockPlaceEvent.isCancelled || blockyEvent.isCancelled) {
        targetBlock.setBlockData(currentData, false) // false to cancel physic
        return null
    }

    val sound =
        if (isFlowing) {
            if (newData.material == Material.WATER) Sound.ITEM_BUCKET_EMPTY
            else Sound.valueOf("ITEM_BUCKET_EMPTY_" + newData.material)
        } else newData.soundGroup.placeSound

    if (player.gameMode != GameMode.CREATIVE) {
        if (MaterialTags.BUCKETS.isTagged(item)) item.type = Material.BUCKET
        else item.subtract()
    }

    targetBlock.toGearyOrNull()?.let { entity ->
        if (entity.has<BlockyLight>())
            BlockLight.createBlockLight(against.getRelative(face).location, entity.get<BlockyLight>()!!.lightLevel)
    }

    targetBlock.world.playSound(targetBlock.location, sound, 1.0f, 1.0f)
    player.swingMainHand()
    return targetBlock
}

internal fun attemptBreakBlockyBlock(block: Block, player: Player? = null): Boolean {
    player?.let {
        if (!BlockyBlockBreakEvent(block, player).callEvent()) return false
        if (!ProtectionLib.canBreak(it, block.location)) return false
        if (player.gameMode != GameMode.CREATIVE)
            player.inventory.itemInMainHand.damage(1, player)
    }

    val prefab = block.toGearyOrNull() ?: return false
    if (prefab.has<BlockyLight>()) BlockLight.removeBlockLight(block.location)
    handleBlockyDrops(block, player)


    block.customBlockData.clear()
    block.setType(Material.AIR, true)
    return true
}

fun handleBlockyDrops(block: Block, player: Player?) {
    val gearyBlock = block.toGearyOrNull() ?: return
    val drop = gearyBlock.get<BlockyDrops>() ?: return
    if (!gearyBlock.has<SetBlock>()) return

    if (drop.onlyDropWithCorrectTool && !GenericHelpers.isCorrectTool(player ?: return, block, EquipmentSlot.HAND)) return
    GenericHelpers.handleBlockDrop(drop, player, block.location)
}

object GenericHelpers {

    fun ItemStack.isSimilarNoDurab(other: ItemStack): Boolean {
        if (other === this) return true
        return type == other.type && hasItemMeta() == other.hasItemMeta() && if (hasItemMeta()) Bukkit.getItemFactory().equals(itemMeta, other.itemMeta) else true
    }

    fun Block.isInteractable(): Boolean {
        return when {
            isBlockyBlock || isBlockyFurniture || CaveVineHelpers.isBlockyCaveVine(this) -> false
            blockData is Stairs || blockData is Fence -> false
            !type.isInteractable || type in setOf(Material.PUMPKIN, Material.MOVING_PISTON, Material.REDSTONE_ORE, Material.REDSTONE_WIRE) -> false
            else -> true
        }
    }

    fun getDirectionalId(gearyEntity: GearyEntity, face: BlockFace, player: Player?): Int {
        return gearyEntity.get<BlockyDirectional>()?.let { directional ->
            if (directional.isLogType) {
                return when (face) {
                    BlockFace.UP, BlockFace.DOWN -> directional.yBlock?.toEntityOrNull() ?: gearyEntity
                    BlockFace.NORTH, BlockFace.SOUTH -> directional.xBlock?.toEntityOrNull() ?: gearyEntity
                    BlockFace.WEST, BlockFace.EAST -> directional.zBlock?.toEntityOrNull() ?: gearyEntity
                    else -> gearyEntity
                }.get<SetBlock>()?.blockId ?: 0
            } else {
                return when ((player?.getDirectionalRelative(directional) ?: face)) {
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

    private fun Player.getDirectionalRelative(directional: BlockyDirectional): BlockFace? {
        val yaw = location.yaw.toInt()
        val pitch = location.pitch.toInt()

        return when {
            directional.isLogType -> null
            directional.isDropperType && pitch >= 45 -> BlockFace.UP
            directional.isDropperType && pitch <= -45 -> BlockFace.DOWN
            else -> GenericHelpers.getRelativeBlockFace(yaw)
        }
    }

    fun getRelativeBlockFace(yaw: Int): BlockFace {
        return when (yaw) {
            in 45..135, in -315..-225 -> BlockFace.EAST
            in 135..225, in -225..-135 -> BlockFace.SOUTH
            in 225..315, in -135..-45 -> BlockFace.WEST
            else -> BlockFace.NORTH
        }
    }

    fun getLeftBlock(block: Block, player: Player): Block {
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

    fun getRightBlock(block: Block, player: Player): Block {
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

    fun Player.getRelativeFacing(): BlockFace {
        val yaw = location.yaw.toDouble()
        return when {
            (yaw >= 348.75 || yaw in 0.0..11.25 || yaw >= -11.25 && yaw <= 0.0 || yaw <= -348.75 && yaw <= 0.0) -> BlockFace.SOUTH
            (yaw in 11.25..33.75 || yaw in -348.75..-326.25) -> BlockFace.SOUTH_SOUTH_WEST
            (yaw in 33.75..56.25 || yaw in -326.25..-303.75) -> BlockFace.SOUTH_WEST
            (yaw in 56.25..78.75 || yaw in -303.75..-281.25) -> BlockFace.WEST_SOUTH_WEST
            (yaw in 78.75..101.25 || yaw in -281.25..-258.75) -> BlockFace.WEST
            (yaw in 101.25..123.75 || yaw in -258.75..-236.25) -> BlockFace.WEST_NORTH_WEST
            (yaw in 123.75..146.25 || yaw in -236.25..-213.75) -> BlockFace.NORTH_WEST
            (yaw in 146.25..168.75 || yaw in -213.75..-191.25) -> BlockFace.NORTH_NORTH_WEST
            (yaw in 168.75..191.25 || yaw in -191.25..-168.75) -> BlockFace.NORTH
            (yaw in 191.25..213.75 || yaw in -168.75..-146.25) -> BlockFace.NORTH_NORTH_EAST
            (yaw in 213.75..236.25 || yaw in -146.25..-123.75) -> BlockFace.NORTH_EAST
            (yaw in 236.25..258.75 || yaw in -123.75..-101.25) -> BlockFace.EAST_NORTH_EAST
            (yaw in 258.75..281.25 || yaw in -101.25..-78.75) -> BlockFace.EAST
            (yaw in 281.25..303.75 || yaw in -78.75..-56.25) -> BlockFace.EAST_SOUTH_EAST
            (yaw in 303.75..326.25 || yaw in -56.25..-33.75) -> BlockFace.SOUTH_EAST
            (yaw in 326.25..348.75 || yaw in -33.75..-11.25) -> BlockFace.SOUTH_SOUTH_EAST
            else -> facing
        }
    }

    fun getAnvilFacing(face: BlockFace): BlockFace {
        return when (face) {
            BlockFace.NORTH -> BlockFace.EAST
            BlockFace.EAST -> BlockFace.NORTH
            BlockFace.SOUTH -> BlockFace.WEST
            BlockFace.WEST -> BlockFace.SOUTH
            else -> BlockFace.NORTH
        }
    }

    /**
     * @return A new location at the bottom-center of a block
     */
    fun Location.toBlockCenterLocation(): Location {
        return clone().toCenterLocation().apply { y -= 0.5 }
    }

    fun isCorrectTool(player: Player, block: Block, hand: EquipmentSlot): Boolean {
        val acceptedToolTypes = block.toGearyOrNull()?.let { geary -> geary.get<BlockyDrops>()?.acceptedToolTypes ?: geary.get<BlockyBreaking>()?.modifiers?.heldTypes?.map { it.toolType } } ?: return false
        val heldToolTypes = player.gearyInventory?.get(hand)?.get<BlockyMining>()?.toolTypes
            ?: getVanillaToolTypes(player.inventory.getItem(hand))?.let { setOf(it) } ?: setOf()

        return ToolType.ANY in acceptedToolTypes || acceptedToolTypes.any { it in heldToolTypes }
    }

    fun getVanillaToolTypes(itemStack: ItemStack): ToolType? {
        return when {
            MaterialTags.AXES.isTagged(itemStack.type) -> ToolType.AXE
            MaterialTags.PICKAXES.isTagged(itemStack.type) -> ToolType.PICKAXE
            MaterialTags.SWORDS.isTagged(itemStack.type) -> ToolType.SWORD
            MaterialTags.SHOVELS.isTagged(itemStack.type) -> ToolType.SHOVEL
            MaterialTags.HOES.isTagged(itemStack.type) -> ToolType.HOE
            itemStack.type == Material.SHEARS -> ToolType.SHEARS
            else -> null
        }
    }

    fun handleBlockDrop(blockyDrop: BlockyDrops, player: Player?, location: Location) {
        blockyDrop.drops.forEach { drop ->
            val hand = player?.inventory?.itemInMainHand ?: ItemStack(Material.AIR)
            val item =
                if (Enchantment.SILK_TOUCH in hand.enchantments)
                    drop.silkTouchedDrop?.toItemStack()
                else drop.item?.toItemStack()
            val amount =
                if (drop.affectedByFortune && Enchantment.LOOT_BONUS_BLOCKS in hand.enchantments)
                    drop.amount.randomOrMin() * Random.nextInt(
                        1,
                        hand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) + 1
                    )
                else drop.amount.randomOrMin()

            if (player?.gameMode == GameMode.CREATIVE) return

            if (drop.exp > 0) location.spawn<ExperienceOrb> { experience = drop.exp }
            item?.let {
                item.amount = amount
                location.world.dropItemNaturally(location, item)
            }
        }
    }

    fun UUID.toEntity() = Bukkit.getEntity(this)
}
