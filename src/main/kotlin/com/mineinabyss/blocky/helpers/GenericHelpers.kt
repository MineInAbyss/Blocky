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
    val prefab = block.getPrefabFromBlock()?.toEntity() ?: return

    if (prefab.hasBlockySound) block.world.playSound(block.location, prefab.blockySound!!.breakSound, 1.0f, 1.0f)
    if (prefab.hasBlockyLight) removeBlockLight(block.location)
    if (prefab.hasBlockyDrops) handleBlockyDrops(block, player)
}

fun handleBlockyDrops(block: Block, player: Player?) {
    if (!block.isBlockyBlock) return

    block.blockyInfo?.blockDrop?.map {
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
        if (it.entity.isDirectional) {
            (it.entity.directional?.yBlockId == blockMap[blockData] ||
                    it.entity.directional?.xBlockId == blockMap[blockData] ||
                    it.entity.directional?.zBlockId == blockMap[blockData]) &&
                    it.entity.blockyBlock?.blockType == type
        } else it.entity.blockyBlock?.blockId == blockMap[blockData] && it.entity.blockyBlock?.blockType == type
    }?.key ?: return null
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

    val data = targetBlock.blockData
    val state = targetBlock.state
    val blockPlaceEvent = BlockPlaceEvent(targetBlock, state, against, item, player, true, hand)
    blockPlaceEvent.callEvent()


    // Handle sign & double blocks before event due to option for cancelling everything
    if (data is Door || data is Bed || data is Chest || data is Bisected)
        if (!targetBlock.handleDoubleBlocks(player)) blockPlaceEvent.isCancelled = true

    if (state is Sign && face == BlockFace.DOWN) blockPlaceEvent.isCancelled = true
    if ((state is Skull || state is Sign) && face != BlockFace.DOWN && face != BlockFace.UP)
        targetBlock.handleSignAndSkull(player, face)

    if (!blockPlaceEvent.canBuild() || blockPlaceEvent.isCancelled) {
        targetBlock.setBlockData(currentData, false) // false to cancel physic
        return null
    }

    if (data !is Door && (data is Bisected || data is Slab))
        targetBlock.handleHalfBlocks(player)

    if (data is Rotatable)
        targetBlock.handleRotatableBlocks(player)

    if (data is Directional || data is FaceAttachable)
        targetBlock.handleDirectionalBlocks(player)

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

private fun Block.handleSignAndSkull(player: Player, face: BlockFace) {
    if (state is Sign) player.openSign(state as Sign)
    type =
        if (type.toString().endsWith("SIGN"))
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
            val top = getRelative(BlockFace.UP)

            if (getLeftBlock(player).blockData is Door)
                blockData.hinge = Door.Hinge.RIGHT
            else blockData.hinge = Door.Hinge.LEFT

            blockData.facing = player.facing
            blockData.half = Bisected.Half.TOP
            top.setBlockData(blockData, false)
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
            val top = getRelative(BlockFace.UP)

            blockData.half = Bisected.Half.TOP
            top.setBlockData(blockData, false)
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
            if (eye.hitPosition.y <= eye.hitBlock?.location?.clone()?.apply { y += 0.75 }?.y!!)
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

private fun Block.handleDirectionalBlocks(player: Player) {
    val data = blockData
    when (data) {
        is Directional -> {
            if (data is FaceAttachable) {
                data.attachedFace =
                    when (player.rayTraceBlocks(5.0, FluidCollisionMode.NEVER)?.hitBlockFace) {
                        BlockFace.UP -> FaceAttachable.AttachedFace.FLOOR
                        BlockFace.DOWN -> FaceAttachable.AttachedFace.CEILING
                        else -> FaceAttachable.AttachedFace.WALL
                    }
            }
            data.facing = player.facing.oppositeFace
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
    !isDirectional -> blockyBlock?.blockId
    directional?.hasYVariant() == true && (face == BlockFace.UP || face == BlockFace.DOWN) -> directional?.yBlockId
    directional?.hasXVariant() == true && (face == BlockFace.NORTH || face == BlockFace.SOUTH) -> directional?.xBlockId
    directional?.hasZVariant() == true && (face == BlockFace.WEST || face == BlockFace.EAST) -> directional?.zBlockId
    else -> null
}

fun Block.getLeftBlock(player: Player): Block {
    val leftBlock = when (player.facing) {
        BlockFace.NORTH -> player.world.getBlockAt(location.clone().subtract(1.0, 0.0, 0.0))
        BlockFace.SOUTH -> player.world.getBlockAt(location.clone().add(1.0, 0.0, 0.0))
        BlockFace.WEST -> player.world.getBlockAt(location.clone().add(0.0, 0.0, 1.0))
        BlockFace.EAST -> player.world.getBlockAt(location.clone().subtract(0.0, 0.0, 1.0))
        else -> this
    }
    return if (leftBlock.blockData is Chest && (leftBlock.blockData as Chest).facing != player.facing.oppositeFace) this
    else leftBlock
}

fun Block.getRightBlock(player: Player): Block {
    val rightBlock = when (player.facing) {
        BlockFace.NORTH -> player.world.getBlockAt(location.clone().add(1.0, 0.0, 0.0))
        BlockFace.SOUTH -> player.world.getBlockAt(location.clone().subtract(1.0, 0.0, 0.0))
        BlockFace.WEST -> player.world.getBlockAt(location.clone().subtract(0.0, 0.0, 1.0))
        BlockFace.EAST -> player.world.getBlockAt(location.clone().add(0.0, 0.0, 1.0))
        else -> this
    }
    return if (rightBlock.blockData is Chest && (rightBlock.blockData as Chest).facing != player.facing.oppositeFace) this
    else rightBlock
}
