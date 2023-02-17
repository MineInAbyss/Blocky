package com.mineinabyss.blocky.helpers

import com.destroystokyo.paper.MaterialTags
import com.jeff_media.customblockdata.CustomBlockData
import com.jeff_media.morepersistentdatatypes.DataType
import com.mineinabyss.blocky.api.BlockyBlocks.gearyEntity
import com.mineinabyss.blocky.api.BlockyBlocks.isBlockyBlock
import com.mineinabyss.blocky.api.BlockyFurnitures.blockyFurnitureEntity
import com.mineinabyss.blocky.api.BlockyFurnitures.isFurnitureHitbox
import com.mineinabyss.blocky.api.events.block.BlockyBlockBreakEvent
import com.mineinabyss.blocky.api.events.block.BlockyBlockPlaceEvent
import com.mineinabyss.blocky.blockMap
import com.mineinabyss.blocky.blockyConfig
import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.blocky.components.core.BlockyBlock
import com.mineinabyss.blocky.components.core.BlockyBlock.BlockType
import com.mineinabyss.blocky.components.core.BlockyInfo
import com.mineinabyss.blocky.components.features.BlockyDirectional
import com.mineinabyss.blocky.components.features.BlockyDrops
import com.mineinabyss.blocky.components.features.BlockyLight
import com.mineinabyss.blocky.components.features.BlockyPlacableOn
import com.mineinabyss.blocky.components.features.mining.BlockyMining
import com.mineinabyss.blocky.components.features.mining.ToolType
import com.mineinabyss.blocky.systems.BlockyBlockQuery
import com.mineinabyss.blocky.systems.BlockyBlockQuery.prefabKey
import com.mineinabyss.blocky.systems.BlockyBlockQuery.type
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.events.call
import com.mineinabyss.idofront.util.randomOrMin
import com.mineinabyss.looty.tracking.toGearyOrNull
import io.th0rgal.protectionlib.ProtectionLib
import org.bukkit.*
import org.bukkit.block.*
import org.bukkit.block.Sign
import org.bukkit.block.data.*
import org.bukkit.block.data.type.*
import org.bukkit.block.data.type.Bed
import org.bukkit.block.data.type.Chest
import org.bukkit.block.data.type.Lectern
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.ExperienceOrb
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.BlockInventoryHolder
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataContainer
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

internal fun Block.attemptBreakBlockyBlock(player: Player?) {
    val prefab = this.gearyEntity ?: return
    val blockBreakEvent = player?.let { BlockBreakEvent(this, it) }
    val blockyBreakEvent = BlockyBlockBreakEvent(this, player).run { call(); this }

    if (!ProtectionLib.canBreak(player, this.location)) blockyBreakEvent.isCancelled = true
    if (blockBreakEvent?.isCancelled == true || blockyBreakEvent.isCancelled) return

    if (prefab.has<BlockyLight>()) handleLight.removeBlockLight(this.location)
    if (prefab.has<BlockyInfo>()) handleBlockyDrops(this, player)
    if (player != null && player.gameMode != GameMode.CREATIVE)
        player.inventory.itemInMainHand.damage(1, player)

    this.customBlockData.clear()
    this.setType(Material.AIR, false)
}

fun ItemStack.isBlockyBlock(player: Player): Boolean {
    return toGearyOrNull(player)?.has<BlockyBlock>() == true
}

fun handleBlockyDrops(block: Block, player: Player?) {
    val gearyBlock = block.gearyEntity ?: return
    val info = gearyBlock.get<BlockyInfo>() ?: return
    if (!gearyBlock.has<BlockyBlock>()) return

    if (info.onlyDropWithCorrectTool) {
        if (player == null) return
        val itemInHand = player.inventory.itemInMainHand
        if (!itemInHand.isCorrectTool(player, block)) return
    }

    gearyBlock.get<BlockyInfo>()?.blockDrop?.handleBlockDrop(player, block.location) ?: return
}

private fun ItemStack.isCorrectTool(player: Player, block: Block): Boolean {
    val gearyBlock = block.gearyEntity ?: return false
    val info = gearyBlock.get<BlockyInfo>() ?: return false
    val allowedToolTypes = toGearyOrNull(player)?.get<BlockyMining>()?.toolTypes ?: return false

    return ToolType.ANY in allowedToolTypes || info.acceptedToolTypes.any { it in allowedToolTypes }
}

fun List<BlockyDrops>.handleBlockDrop(player: Player?, location: Location) {
    this.forEach { drop ->
        val hand = player?.inventory?.itemInMainHand ?: ItemStack(Material.AIR)
        val item =
            if (drop.affectedBySilkTouch && Enchantment.SILK_TOUCH in hand.enchantments)
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

        if (drop.exp < 0)
            (location.world.spawnEntity(location, EntityType.EXPERIENCE_ORB) as ExperienceOrb).experience = drop.exp
        (1..amount).forEach { _ -> item?.let { item -> location.world.dropItemNaturally(location, item) } }
    }
}

val Block.prefabKey
    get(): PrefabKey? {
        val type =
            when {
                this.isFurnitureHitbox -> return this.blockyFurnitureEntity?.toGearyOrNull()?.get()
                type == Material.NOTE_BLOCK -> BlockType.NOTEBLOCK
                type == Material.TRIPWIRE -> BlockType.WIRE
                type == Material.CAVE_VINES -> BlockType.CAVEVINE
                Tag.LEAVES.isTagged(type) -> BlockType.LEAF
                type in BLOCKY_SLABS -> BlockType.SLAB
                type in BLOCKY_STAIRS -> BlockType.STAIR
                else -> null
            } ?: return null

        return BlockyBlockQuery.filter { it.type.blockType == type }.firstOrNull { scope ->
            val blockyBlock = scope.type
            if (scope.entity.has<BlockyDirectional>()) {
                val directional = scope.entity.get<BlockyDirectional>()
                ((directional?.yBlock?.toEntityOrNull()
                    ?: scope.entity).get<BlockyBlock>()?.blockId == blockMap[blockData] ||
                        (directional?.xBlock?.toEntityOrNull()
                            ?: scope.entity).get<BlockyBlock>()?.blockId == blockMap[blockData] ||
                        (directional?.zBlock?.toEntityOrNull()
                            ?: scope.entity).get<BlockyBlock>()?.blockId == blockMap[blockData]) &&
                        blockyBlock.blockType == type
            } else if (blockyBlock.blockType == BlockType.SLAB) {
                BLOCKY_SLABS.elementAt(blockyBlock.blockId - 1) == blockData.material
            } else if (blockyBlock.blockType == BlockType.STAIR) {
                BLOCKY_STAIRS.elementAt(blockyBlock.blockId - 1) == blockData.material
            } else blockyBlock.blockId == blockMap[blockData] && blockyBlock.blockType == type
        }?.prefabKey
    }

val BlockData.prefabKey get(): PrefabKey? {
    val type =
        when {
            //TODO This might also need sapling for future
            material == Material.BARRIER -> return null
            material == Material.NOTE_BLOCK -> BlockType.NOTEBLOCK
            material == Material.TRIPWIRE -> BlockType.WIRE
            material == Material.CAVE_VINES -> BlockType.CAVEVINE
            Tag.LEAVES.isTagged(material) -> BlockType.LEAF
            material in BLOCKY_SLABS -> BlockType.SLAB
            material in BLOCKY_STAIRS -> BlockType.STAIR
            else -> null
        } ?: return null

    return BlockyBlockQuery.filter { it.type.blockType == type }.firstOrNull { scope ->
        val blockyBlock = scope.type
        if (scope.entity.has<BlockyDirectional>()) {
            val directional = scope.entity.get<BlockyDirectional>()
            ((directional?.yBlock?.toEntityOrNull()
                ?: scope.entity).get<BlockyBlock>()?.blockId == blockMap[this] ||
                    (directional?.xBlock?.toEntityOrNull()
                        ?: scope.entity).get<BlockyBlock>()?.blockId == blockMap[this] ||
                    (directional?.zBlock?.toEntityOrNull()
                        ?: scope.entity).get<BlockyBlock>()?.blockId == blockMap[this]) &&
                    blockyBlock.blockType == type
        } else if (blockyBlock.blockType == BlockType.SLAB) {
            BLOCKY_SLABS.elementAt(blockyBlock.blockId - 1) == this.material
        } else if (blockyBlock.blockType == BlockType.STAIR) {
            BLOCKY_STAIRS.elementAt(blockyBlock.blockId - 1) == this.material
        } else blockyBlock.blockId == blockMap[this] && blockyBlock.blockType == type
    }?.prefabKey
}


//val Block.isBlockyBlock get() = gearyEntity?.has<BlockyBlock>() == true
val BlockFace.isCardinal get() = this == BlockFace.NORTH || this == BlockFace.EAST || this == BlockFace.SOUTH || this == BlockFace.WEST
val Block.persistentDataContainer get() = customBlockData as PersistentDataContainer
val Block.customBlockData get() = CustomBlockData(this, blockyPlugin)

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
    if (!against.isBlockyBlock && !item.isBlockyBlock(player)) return null
    if (player.isInBlock(targetBlock)) return null
    if (against.isVanillaNoteBlock) return null

    if (!blockyConfig.noteBlocks.restoreFunctionality && targetBlock.isVanillaNoteBlock)
        targetBlock.customBlockData.set(NOTE_KEY, DataType.INTEGER, 0)

    val currentData = targetBlock.blockData
    val isFlowing = newData.material == Material.WATER || newData.material == Material.LAVA
    targetBlock.setBlockData(newData, isFlowing)

    val blockPlaceEvent = BlockPlaceEvent(targetBlock, targetBlock.state, against, item, player, true, hand)
    val blockyEvent = BlockyBlockPlaceEvent(targetBlock, player)

    when {
        blockPlaceEvent.isCancelled -> blockyEvent.isCancelled = true
        targetBlock.gearyEntity?.get<BlockyPlacableOn>()
            ?.isPlacableOn(targetBlock, face) == true -> blockyEvent.isCancelled = true

        !ProtectionLib.canBuild(player, targetBlock.location) || !blockPlaceEvent.canBuild() ->
            blockyEvent.isCancelled = true

        !targetBlock.correctAllBlockStates(player, face, item) -> blockyEvent.isCancelled = true
    }

    blockyEvent.call()
    if (blockyEvent.isCancelled) {
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
        else item.amount = item.amount - 1
    }

    targetBlock.gearyEntity?.let { entity ->
        if (entity.has<BlockyLight>())
            handleLight.createBlockLight(against.getRelative(face).location, entity.get<BlockyLight>()!!.lightLevel)
    }

    targetBlock.world.playSound(targetBlock.location, sound, 1.0f, 1.0f)
    player.swingMainHand()
    return targetBlock
}

//TODO This might be better to call via an event or something in stead of this god awful method
private fun Block.correctAllBlockStates(player: Player, face: BlockFace, item: ItemStack): Boolean {
    val data = blockData.clone()
    val state = state
    val booleanChecks = booleanChecks(face, item)
    if (state is Skull && item.itemMeta is SkullMeta) {
        (item.itemMeta as SkullMeta).playerProfile?.let { state.setPlayerProfile(it) }
        state.update(true, false)
    }
    if (booleanChecks != null) return booleanChecks
    if (data !is Door && (data is Bisected || data is Slab)) handleHalfBlocks(player)
    if (data is Rotatable) handleRotatableBlocks(player)
    if (MaterialTags.CORAL_FANS.isTagged(this) && face != BlockFace.UP)
        type = Material.valueOf(type.toString().replace("_CORAL_FAN", "_CORAL_WALL_FAN"))
    if (data is Waterlogged) handleWaterlogged(face)
    if (data is Ageable) {
        return if ((type == Material.WEEPING_VINES || type == Material.WEEPING_VINES_PLANT) && face != BlockFace.DOWN) false
        else if ((type == Material.TWISTING_VINES || type == Material.TWISTING_VINES_PLANT) && face != BlockFace.UP) false
        else false
    }
    if ((data is Door || data is Bed || data is Chest || data is Bisected) && data !is Stairs && data !is TrapDoor)
        if (!handleDoubleBlocks(player)) return false
    if ((state is Skull || state is Sign || MaterialTags.TORCHES.isTagged(this)) && face != BlockFace.DOWN && face != BlockFace.UP) handleWallAttachable(
        player,
        face
    )

    if (data !is Stairs && (data is Directional || data is FaceAttachable || data is MultipleFacing || data is Attachable)) {
        handleDirectionalBlocks(face)
    }

    if (data is Orientable) {
        data.axis = when {
            (face == BlockFace.UP || face == BlockFace.DOWN) -> Axis.Y
            (face == BlockFace.NORTH || face == BlockFace.SOUTH) -> Axis.Z
            (face == BlockFace.WEST || face == BlockFace.EAST) -> Axis.X
            else -> Axis.Y
        }
        setBlockData(data, false)
    }

    if (data is Lantern) {
        if (face != BlockFace.DOWN) return false
        data.isHanging = true
        setBlockData(data, false)
    }

    if (data is Repeater) {
        data.facing = player.facing.oppositeFace
        setBlockData(data, false)
    }

    if (Tag.ANVIL.isTagged(type)) {
        (data as Directional).facing = getAnvilFacing(face)
        setBlockData(data, false)
    }

    if (data is Lectern) {
        data.facing = player.facing.oppositeFace
        setBlockData(data, false)
    }

    if (state is BlockInventoryHolder && ((item.itemMeta as BlockStateMeta).blockState is BlockInventoryHolder)) {
        ((item.itemMeta as BlockStateMeta).blockState as Container).inventory.forEach { i ->
            if (i != null) state.inventory.addItem(i)
        }
    }

    if (state is Sign) player.openSign(state)

    return true
}

private fun Block.booleanChecks(face: BlockFace, item: ItemStack): Boolean? {
    return when {
        blockData is CaveVines || blockData is Tripwire || type == Material.CHORUS_PLANT -> true
        blockData is Sapling && face != BlockFace.UP -> false
        blockData is Ladder && (face == BlockFace.UP || face == BlockFace.DOWN) -> false
        type == Material.HANGING_ROOTS && face != BlockFace.DOWN -> false
        MaterialTags.TORCHES.isTagged(item) && face == BlockFace.DOWN -> false
        state is Sign && face == BlockFace.DOWN -> false
        isCoralNotBlock() && face == BlockFace.DOWN -> false
        MaterialTags.CORAL.isTagged(this) && getRelative(BlockFace.DOWN).type == Material.AIR -> false
        blockData is MultipleFacing && blockData !is GlassPane && face == BlockFace.UP -> false
        blockData is CoralWallFan && face == BlockFace.DOWN -> false
        else -> null
    }
}

private fun Block.handleWaterlogged(face: BlockFace) {
    val data = blockData.clone()
    when (data) {
        is Waterlogged -> {
            if (data is Directional && data !is Stairs) data.facing = face
            data.isWaterlogged = false
        }
    }
    setBlockData(data, false)
}

private fun Block.handleWallAttachable(player: Player, face: BlockFace) {
    if (state is Sign) player.openSign(state as Sign)
    type =
        if (MaterialTags.TORCHES.isTagged(this))
            Material.valueOf(type.toString().replace("TORCH", "WALL_TORCH"))
        else if (Tag.STANDING_SIGNS.isTagged(type))
            Material.valueOf(type.toString().replace("_SIGN", "_WALL_SIGN"))
        else if (type.toString().endsWith("SKULL") && !type.toString().endsWith("_WALL_SKULL"))
            Material.valueOf(type.toString().replace("_SKULL", "_WALL_SKULL"))
        else Material.valueOf(type.toString().replace("_HEAD", "_WALL_HEAD"))

    val data = type.createBlockData() as Directional
    data.facing = face
    setBlockData(data, false)
}

private fun Block.handleDoubleBlocks(player: Player): Boolean {
    when (val blockData = blockData) {
        is Door -> {
            if (getRelative(BlockFace.UP).type.isSolid || !getRelative(BlockFace.UP).isReplaceable) return false
            if (getLeftBlock(player).blockData is Door)
                blockData.hinge = Door.Hinge.RIGHT
            else blockData.hinge = Door.Hinge.LEFT

            blockData.facing = player.facing
            blockData.half = Bisected.Half.TOP
            getRelative(BlockFace.UP).setBlockData(blockData, false)
            blockData.half = Bisected.Half.BOTTOM

            setBlockData(blockData, false)
        }

        is Bed -> {
            if (getRelative(player.facing).type.isSolid || !getRelative(player.facing).isReplaceable) return false
            getRelative(player.facing).setBlockData(blockData, false)
            val nextBlock = getRelative(player.facing)
            val nextData = nextBlock.blockData as Bed

            blockData.part = Bed.Part.FOOT
            nextData.part = Bed.Part.HEAD
            blockData.facing = player.facing
            nextData.facing = player.facing
            nextBlock.blockData = nextData
            setBlockData(blockData, false)
        }

        is Chest -> {
            if (getLeftBlock(player).blockData is Chest)
                blockData.type = Chest.Type.LEFT
            else if (getRightBlock(player).blockData is Chest)
                blockData.type = Chest.Type.RIGHT
            else blockData.type = Chest.Type.SINGLE

            blockData.facing = player.facing.oppositeFace
            setBlockData(blockData, true)
        }

        is Bisected -> {
            if (getRelative(BlockFace.UP).type.isSolid || !getRelative(BlockFace.UP).isReplaceable) return false

            blockData.half = Bisected.Half.TOP
            getRelative(BlockFace.UP).setBlockData(blockData, false)
            blockData.half = Bisected.Half.BOTTOM
        }

        else -> {
            setBlockData(Material.AIR.createBlockData(), false)
            return false
        }
    }
    return true
}

private fun Block.handleHalfBlocks(player: Player) {
    val eye = player.rayTraceBlocks(5.0, FluidCollisionMode.NEVER) ?: return
    val data = blockData.clone()
    when (data) {
        is TrapDoor -> {
            data.facing = player.facing.oppositeFace
            if (eye.hitPosition.y <= eye.hitBlock?.location?.toCenterLocation()?.y!!) data.half = Bisected.Half.BOTTOM
            else data.half = Bisected.Half.TOP
        }

        is Stairs -> {
            data.facing = player.facing
            if (eye.hitPosition.y < eye.hitBlock?.location?.clone()?.apply { y += 0.6 }?.y!!)
                data.half = Bisected.Half.BOTTOM
            else data.half = Bisected.Half.TOP
        }

        is Slab -> {
            if (eye.hitPosition.y <= eye.hitBlock?.location?.toCenterLocation()?.y!!) data.type = Slab.Type.BOTTOM
            else data.type = Slab.Type.TOP
        }
    }
    setBlockData(data, true)
}

private fun Block.handleRotatableBlocks(player: Player) {
    val data = blockData.clone() as Rotatable
    data.rotation =
        if (MaterialTags.SKULLS.isTagged(this)) player.getRelativeFacing()
        else player.getRelativeFacing().oppositeFace
    setBlockData(data, false)
}

private fun Block.handleDirectionalBlocks(face: BlockFace) {
    val data = blockData.clone()

    when (data) {
        is Directional -> {
            if (data is FaceAttachable) {
                when (face) {
                    BlockFace.UP -> data.attachedFace = FaceAttachable.AttachedFace.FLOOR
                    BlockFace.DOWN -> data.attachedFace = FaceAttachable.AttachedFace.CEILING
                    else -> {
                        data.facing = face
                    }
                }
            } else data.facing = face
        }

        is MultipleFacing -> {
            data.allowedFaces.forEach {
                if (getRelative(it).type.isSolid) data.setFace(it, true)
                else data.setFace(it, false)
            }
        }

        is Attachable -> {
            data.isAttached = true
        }
    }
    setBlockData(data, false)
}

fun GearyEntity.getDirectionalId(face: BlockFace, player: Player?): Int {
    return this.get<BlockyDirectional>()?.let { directional ->
        if (directional.isLogType) {
            return when (face) {
                BlockFace.UP, BlockFace.DOWN -> directional.yBlock?.toEntityOrNull() ?: this
                BlockFace.NORTH, BlockFace.SOUTH -> directional.xBlock?.toEntityOrNull() ?: this
                BlockFace.WEST, BlockFace.EAST -> directional.zBlock?.toEntityOrNull() ?: this
                else -> this
            }.get<BlockyBlock>()?.blockId ?: 0
        } else {
            return when ((player?.getDirectionalRelative(directional) ?: face)) {
                BlockFace.NORTH -> directional.northBlock?.toEntityOrNull() ?: this
                BlockFace.SOUTH -> directional.southBlock?.toEntityOrNull() ?: this
                BlockFace.WEST -> directional.westBlock?.toEntityOrNull() ?: this
                BlockFace.EAST -> directional.eastBlock?.toEntityOrNull() ?: this
                BlockFace.UP -> directional.upBlock?.toEntityOrNull() ?: this
                BlockFace.DOWN -> directional.downBlock?.toEntityOrNull() ?: this
                else -> this
            }.get<BlockyBlock>()?.blockId ?: 0
        }
    } ?: this.get<BlockyBlock>()?.blockId ?: 0
}

private fun Player.getDirectionalRelative(directional: BlockyDirectional): BlockFace? {
    val yaw = location.yaw.toInt()
    val pitch = location.pitch.toInt()

    return when {
        directional.isLogType -> null
        directional.isDropperType && pitch >= 45 -> BlockFace.UP
        directional.isDropperType && pitch <= -45 -> BlockFace.DOWN
        else -> getRelativeBlockFace(yaw)
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

fun Block.getLeftBlock(player: Player): Block {
    val leftBlock = when (player.facing) {
        BlockFace.NORTH -> getRelative(BlockFace.WEST)
        BlockFace.SOUTH -> getRelative(BlockFace.EAST)
        BlockFace.WEST -> getRelative(BlockFace.SOUTH)
        BlockFace.EAST -> getRelative(BlockFace.NORTH)
        else -> this
    }
    return if (leftBlock.blockData is Chest && (leftBlock.blockData as Chest).facing != player.facing.oppositeFace) this
    else leftBlock
}

fun Block.getRightBlock(player: Player): Block {
    val rightBlock = when (player.facing) {
        BlockFace.NORTH -> getRelative(BlockFace.EAST)
        BlockFace.SOUTH -> getRelative(BlockFace.WEST)
        BlockFace.WEST -> getRelative(BlockFace.NORTH)
        BlockFace.EAST -> getRelative(BlockFace.SOUTH)
        else -> this
    }
    return if (rightBlock.blockData is Chest && (rightBlock.blockData as Chest).facing != player.facing.oppositeFace) this
    else rightBlock
}

private fun Player.getRelativeFacing(): BlockFace {
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

private fun Block.isCoralNotBlock(): Boolean {
    return (MaterialTags.CORAL.isTagged(this) || MaterialTags.CORAL_FANS.isTagged(this))
}

/**
 * @return A new location at the bottom-center of a block
 */
internal fun Location.toBlockCenterLocation(): Location {
    return clone().toCenterLocation().apply { y -= 0.5 }
}
