package com.mineinabyss.blocky.helpers

import com.jeff_media.customblockdata.CustomBlockData
import com.jeff_media.morepersistentdatatypes.DataType
import com.mineinabyss.blocky.BlockyTypeQuery
import com.mineinabyss.blocky.BlockyTypeQuery.key
import com.mineinabyss.blocky.blockMap
import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.blocky.components.*
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.prefabs.PrefabKey
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.Skull
import org.bukkit.block.data.*
import org.bukkit.block.data.type.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

val REPLACEABLE_BLOCKS =
    listOf(
        Material.SNOW, Material.VINE, Material.GRASS, Material.TALL_GRASS, Material.SEAGRASS, Material.FERN,
        Material.LARGE_FERN
    )

fun breakBlockyBlock(block: Block, player: Player?) {
    val prefab = block.getGearyEntityFromBlock() ?: return

    if (prefab.has<BlockySound>()) block.world.playSound(block.location, prefab.get<BlockySound>()!!.breakSound, 1.0f, 1.0f)
    if (prefab.has<BlockyLight>()) removeBlockLight(block.location)
    if (prefab.has<BlockyInfo>()) handleBlockyDrops(block, player)
}

fun handleBlockyDrops(block: Block, player: Player?) {
    val gearyBlock = block.getGearyEntityFromBlock() ?: return
    if (!gearyBlock.has<BlockyBlock>()) return

    gearyBlock.get<BlockyInfo>()?.blockDrop?.map {
        val tempAmount = if (it.minAmount < it.maxAmount) Random.nextInt(it.minAmount, it.maxAmount) else 1
        val hand = player?.inventory?.itemInMainHand ?: ItemStack(Material.AIR)
        val item =
            if (it.affectedBySilkTouch && hand.containsEnchantment(Enchantment.SILK_TOUCH))
                it.silkTouchedDrop.toItemStack()
            else it.item.toItemStack()

        if (player?.gameMode == GameMode.CREATIVE) return
        val amount =
            if (it.affectedByFortune && hand.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS))
                tempAmount * Random.nextInt(1, hand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) + 1)
            else tempAmount

        for (j in 1..amount) block.location.world.dropItemNaturally(block.location, item)
    } ?: return
}

fun Block.getPrefabFromBlock(): PrefabKey? {
    val type =
        when (type) {
            Material.NOTE_BLOCK -> BlockType.CUBE
            Material.TRIPWIRE -> BlockType.GROUND
            Material.CHORUS_PLANT -> BlockType.TRANSPARENT
            else -> return null
        }

    return BlockyTypeQuery.firstOrNull {
        val blockyBlock = it.entity.get<BlockyBlock>()
        if (it.entity.has<Directional>()) {
            val directional = it.entity.get<BlockyDirectional>()
            (directional?.yBlockId == blockMap[blockData] ||
                    directional?.xBlockId == blockMap[blockData] ||
                    directional?.zBlockId == blockMap[blockData]) &&
                    blockyBlock?.blockType == type
        } else blockyBlock?.blockId == blockMap[blockData] && blockyBlock?.blockType == type
    }?.key ?: return null
}

fun Block.getGearyEntityFromBlock() : GearyEntity? {
    return getPrefabFromBlock()?.toEntity()
}

fun placeBlockyBlock(
    player: Player,
    hand: EquipmentSlot,
    item: ItemStack,
    against: Block,
    face: BlockFace,
    newData: BlockData
): Block? {
    val targetBlock: Block
    if (REPLACEABLE_BLOCKS.contains(against.type)) targetBlock = against
    else {
        targetBlock = against.getRelative(face)
        if (!targetBlock.type.isAir && !targetBlock.isLiquid && targetBlock.type != Material.LIGHT) return null
    }

    if (isStandingInside(player, targetBlock)) return null
    if (against.isVanillaNoteBlock()) return null
    if (targetBlock.isVanillaNoteBlock())
        CustomBlockData(targetBlock, blockyPlugin).set(
            NamespacedKey(blockyPlugin, Material.NOTE_BLOCK.toString().lowercase()),
            DataType.BLOCK_DATA,
            newData
        )
    updateBlockyNote(targetBlock)

    val currentData = targetBlock.blockData
    val isFlowing = newData.material == Material.WATER || newData.material == Material.LAVA
    targetBlock.setBlockData(newData, isFlowing)

    val state = targetBlock.state
    val blockPlaceEvent = BlockPlaceEvent(targetBlock, state, against, item, player, true, hand)
    blockPlaceEvent.callEvent()

    if (!targetBlock.correctAllBlockStates(player, face)) blockPlaceEvent.isCancelled = true

    if (!blockPlaceEvent.canBuild() || blockPlaceEvent.isCancelled) {
        targetBlock.setBlockData(currentData, false) // false to cancel physic
        return null
    }

    val sound =
        if (isFlowing) {
            if (newData.material == Material.WATER) Sound.ITEM_BUCKET_EMPTY
            else Sound.valueOf("ITEM_BUCKET_EMPTY_" + newData.material)
        } else newData.soundGroup.placeSound

    if (player.gameMode != GameMode.CREATIVE) {
        if (item.type.toString().contains("BUCKET")) item.type = Material.BUCKET
        else item.amount = item.amount - 1
    }
    player.playSound(targetBlock.location, sound, 1.0f, 1.0f)
    return targetBlock
}

private fun Block.correctAllBlockStates(player: Player, face: BlockFace): Boolean {
    val data = blockData
    if (blockData is Tripwire || type == Material.CHORUS_PLANT) return true
    if (blockData is Ladder && (face == BlockFace.UP || face == BlockFace.DOWN)) return false
    if (type == Material.HANGING_ROOTS && face != BlockFace.DOWN) return false
    if (type.toString().endsWith("TORCH") && face == BlockFace.DOWN) return false
    if (state is Sign && face == BlockFace.DOWN) return false
    if (data !is Door && (data is Bisected || data is Slab)) handleHalfBlocks(player)
    if (data is Rotatable) handleRotatableBlocks(player)
    if (type.toString().contains("CORAL") && !type.toString()
            .endsWith("CORAL_BLOCK") && face == BlockFace.DOWN) return false
    if (type.toString().endsWith("CORAL") && getRelative(BlockFace.DOWN).type == Material.AIR) return false
    if (type.toString().endsWith("_CORAL_FAN") && face != BlockFace.UP)
        type = Material.valueOf(type.toString().replace("_CORAL_FAN", "_CORAL_WALL_FAN"))
    if (data is Waterlogged) handleWaterlogged(face)
    if (data is Ageable) {
        return if ((type == Material.WEEPING_VINES || type == Material.WEEPING_VINES_PLANT) && face != BlockFace.DOWN) false
        else if ((type == Material.TWISTING_VINES || type == Material.TWISTING_VINES_PLANT) && face != BlockFace.UP) false
        else false
    }
    if ((data is Door || data is Bed || data is Chest || data is Bisected) && data !is Stairs && data !is TrapDoor)
        if (!handleDoubleBlocks(player)) return false
    if ((state is Skull || state is Sign || type.toString()
            .contains("TORCH")) && face != BlockFace.DOWN && face != BlockFace.UP
    )
        handleWallAttachable(player, face)

    if (data !is Stairs && (data is Directional || data is FaceAttachable || data is MultipleFacing || data is Attachable)) {
        if (data is MultipleFacing && face == BlockFace.UP) return false
        if (data is CoralWallFan && face == BlockFace.DOWN) return false
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
    return true
}

private fun Block.handleWaterlogged(face: BlockFace) {
    val data = blockData
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
        if (type.toString().endsWith("TORCH"))
            Material.valueOf(type.toString().replace("TORCH", "WALL_TORCH"))
        else if (type.toString().endsWith("SIGN"))
            Material.valueOf(type.toString().replace("_SIGN", "_WALL_SIGN"))
        else if (type.toString().endsWith("SKULL"))
            Material.valueOf(type.toString().replace("_SKULL", "_WALL_SKULL"))
        else Material.valueOf(type.toString().replace("_HEAD", "_WALL_HEAD"))

    val data = Bukkit.createBlockData(type) as Directional
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
            setBlockData(Bukkit.createBlockData(Material.AIR), false)
            return false
        }
    }
    return true
}

private fun Block.handleHalfBlocks(player: Player) {
    val eye = player.rayTraceBlocks(5.0, FluidCollisionMode.NEVER) ?: return
    val data = blockData
    //TODO Stair interactions are still abit unlike vanilla when it comes to connecting around corners etc
    //TODO Making double slabs doesnt work if second slab is attempted placed on CB
    when (data) {
        is TrapDoor -> {
            data.facing = player.facing.oppositeFace
            if (eye.hitPosition.y <= eye.hitBlock?.location?.toCenterLocation()?.y!!) data.half = Bisected.Half.BOTTOM
            else data.half = Bisected.Half.TOP
        }
        is Stairs -> {
            data.facing = player.facing
            if (eye.hitPosition.y < eye.hitBlock?.location?.clone()?.apply { y += .6 }?.y!!)
                data.half = Bisected.Half.BOTTOM
            else data.half = Bisected.Half.TOP
        }
        is Slab -> {
            if (eye.hitPosition.y <= eye.hitBlock?.location?.toCenterLocation()?.y!!) data.type = Slab.Type.BOTTOM
            else data.type = Slab.Type.TOP
        }
    }
    setBlockData(data, false)
}

private fun Block.handleRotatableBlocks(player: Player) {
    val data = blockData
    //TODO Support full facing spectrum not just N/S/W/E
    when (data) {
        is Rotatable -> {
            data.rotation = player.facing
            if (this.state is Sign) player.openSign(this.state as Sign)
        }

    }
    setBlockData(data, false)
}

private fun Block.handleDirectionalBlocks(face: BlockFace) {
    val data = blockData

    when (data) {
        is Directional -> {
            if (data is FaceAttachable) {
                when (face) {
                    BlockFace.UP -> data.attachedFace = FaceAttachable.AttachedFace.FLOOR
                    BlockFace.DOWN -> data.attachedFace = FaceAttachable.AttachedFace.CEILING
                    else -> { data.facing = face }
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

fun createBlockMap(): Map<BlockData, Int> {
    val blockMap = mutableMapOf<BlockData, Int>()

    // Calculates tripwire states
    for (i in 0..127) {
        val tripWireData = Bukkit.createBlockData(Material.TRIPWIRE) as Tripwire
        if (i and 1 == 1) tripWireData.setFace(BlockFace.NORTH, true)
        if (i shr 1 and 1 == 1) tripWireData.setFace(BlockFace.EAST, true)
        if (i shr 2 and 1 == 1) tripWireData.setFace(BlockFace.SOUTH, true)
        if (i shr 3 and 1 == 1) tripWireData.setFace(BlockFace.WEST, true)
        if (i shr 4 and 1 == 1) tripWireData.isPowered = true
        if (i shr 5 and 1 == 1) tripWireData.isDisarmed = true
        if (i shr 6 and 1 == 1) tripWireData.isAttached = true

        blockMap.putIfAbsent(tripWireData, i)
    }

    // Calculates noteblock states
    for (j in 0..799) {
        val noteBlockData = Bukkit.createBlockData(Material.NOTE_BLOCK) as NoteBlock
        if (j >= 399) noteBlockData.instrument = Instrument.getByType((j / 50 % 400).toByte()) ?: continue
        else noteBlockData.instrument = Instrument.getByType((j / 25 % 400).toByte()) ?: continue
        noteBlockData.note = Note((j % 25))
        noteBlockData.isPowered = j !in 0..399

        blockMap.putIfAbsent(noteBlockData, j)
    }

    // Calculates chorus plant states
    for (k in 0..63) {
        val chorusData = Bukkit.createBlockData(Material.CHORUS_PLANT) as MultipleFacing
        if (k and 1 == 1) chorusData.setFace(BlockFace.NORTH, true)
        if (k shr 1 and 1 == 1) chorusData.setFace(BlockFace.EAST, true)
        if (k shr 2 and 1 == 1) chorusData.setFace(BlockFace.SOUTH, true)
        if (k shr 3 and 1 == 1) chorusData.setFace(BlockFace.WEST, true)
        if (k shr 4 and 1 == 1) chorusData.setFace(BlockFace.UP, true)
        if (k shr 5 and 1 == 1) chorusData.setFace(BlockFace.DOWN, true)

        blockMap.putIfAbsent(chorusData, k)
    }

    return blockMap
}

fun GearyEntity.getDirectionalId(face: BlockFace): Int? = when {
    !has<BlockyDirectional>() -> get<BlockyBlock>()?.blockId
    get<BlockyDirectional>()?.hasYVariant() == true && (face == BlockFace.UP || face == BlockFace.DOWN) -> get<BlockyDirectional>()?.yBlockId
    get<BlockyDirectional>()?.hasXVariant() == true && (face == BlockFace.NORTH || face == BlockFace.SOUTH) -> get<BlockyDirectional>()?.xBlockId
    get<BlockyDirectional>()?.hasZVariant() == true && (face == BlockFace.WEST || face == BlockFace.EAST) -> get<BlockyDirectional>()?.zBlockId
    else -> null
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
