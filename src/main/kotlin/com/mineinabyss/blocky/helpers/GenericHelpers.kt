package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.BlockyTypeQuery
import com.mineinabyss.blocky.BlockyTypeQuery.key
import com.mineinabyss.blocky.blockMap
import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.blocky.components.BlockType
import com.mineinabyss.blocky.components.BlockyBlock
import com.mineinabyss.blocky.components.BlockyDirectional
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.geary.prefabs.PrefabKey
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.MultipleFacing
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.block.data.type.Tripwire
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

fun handleBlockyDrops(block: Block, player: Player) {
    val gearyBlock = block.getPrefabFromBlock()?.toEntity() ?: return
    gearyBlock.get<BlockyBlock>() ?: return
    val info = gearyBlock.get<BlockyInfo>() ?: return

    info.blockDrop.map {
        val hand = player.inventory.itemInMainHand
        val item =
            if (it.affectedBySilkTouch && hand.containsEnchantment(Enchantment.SILK_TOUCH))
                it.silkTouchedDrop.toItemStack()
            else it.item.toItemStack()

        if (player.gameMode == GameMode.CREATIVE) return


        val amount =
            if (it.affectedByFortune && hand.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS))
                Random.nextInt(it.minAmount, it.maxAmount) * Random.nextInt(
                    1,
                    hand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) + 1
                )
            else Random.nextInt(it.minAmount, it.maxAmount)

        for (j in 0..amount) block.location.world.dropItemNaturally(block.location, item)
        //expToDrop = it.exp
    }
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
        val directional = it.entity.get<BlockyDirectional>()
        val blocky = it.entity.get<BlockyBlock>()
        if (it.entity.has<BlockyDirectional>()) {
            (directional?.yBlockId == blockMap[blockData] ||
                    directional?.xBlockId == blockMap[blockData] ||
                    directional?.zBlockId == blockMap[blockData]) &&
                    blocky?.blockType == type
        } else {
            blocky?.blockId == blockMap[blockData] &&
                    blocky?.blockType == type
        }
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
        if (!targetBlock.type.isAir && targetBlock.type != Material.WATER && targetBlock.type != Material.LAVA) return null
    }

    if (isStandingInside(player, targetBlock)) return null

    val currentData = targetBlock.blockData
    targetBlock.setBlockData(newData, false)

    val currentBlockState = targetBlock.state

    val blockPlaceEvent = BlockPlaceEvent(targetBlock, currentBlockState, against, item, player, true, hand)
    blockPlaceEvent.callEvent()

    if (!blockPlaceEvent.canBuild()) {
        targetBlock.setBlockData(currentData, false) // false to cancel physic
        return null
    }

    if (player.gameMode != GameMode.CREATIVE) item.subtract(1)
    return targetBlock
}

fun createBlockMap(): Map<BlockData, Int> {
    blockyPlugin.logger.fine("Generating blockMap...")
    val blockMap = mutableMapOf<BlockData, Int>()

    // Calculates tripwire states
    for (i in 1..128) {
        val tripWireData = Bukkit.createBlockData(Material.TRIPWIRE) as Tripwire
        val inAttachedRange = i !in 1..32 && i !in 65..96
        val inPoweredRange = i !in 1..16 && i !in 33..48 && i !in 65..80 && i !in 97..113
        val inDisarmedRange = i !in 1..64
        val northRange = 2..128
        val southRange = 5..128
        val eastRange = 3..128
        val westRange = 9..128

        if (inDisarmedRange) tripWireData.isDisarmed = true
        if (inAttachedRange) tripWireData.isAttached = true
        if (inPoweredRange) tripWireData.isPowered = true
        if (i in northRange step 2) {
            tripWireData.setFace(BlockFace.NORTH, true)
        }

        for (n in westRange) {
            if (i !in n..n + 7) westRange step 8
            else tripWireData.setFace(BlockFace.WEST, true)
        }

        for (n in southRange) {
            if (i !in n..n + 4) southRange step 4
            else tripWireData.setFace(BlockFace.SOUTH, true)
        }

        for (n in eastRange) {
            if (i !in n..n + 1) eastRange step 2
            else tripWireData.setFace(BlockFace.EAST, true)
        }

        blockMap.putIfAbsent(tripWireData, i)
    }

    // Calculates noteblock states
    for (j in 0..799) {
        val noteBlockData = Bukkit.createBlockData(Material.NOTE_BLOCK) as NoteBlock
        if (j >= 400) noteBlockData.instrument = Instrument.getByType((j / 50 % 400).toByte()) ?: continue
        else noteBlockData.instrument = Instrument.getByType((j / 25 % 400).toByte()) ?: continue
        noteBlockData.note = Note((j % 25))
        noteBlockData.isPowered = j !in 0..399

        blockMap.putIfAbsent(noteBlockData, j)
    }

    // Calculates chorus plant states
    for (k in 1..64) {
        val chorusData = Bukkit.createBlockData(Material.CHORUS_PLANT) as MultipleFacing
        val inUpRange = k !in 1..32
        val inDownRange = k !in 1..16 && k !in 33..48
        val northRange = 2..64
        val southRange = 5..64
        val eastRange = 3..64
        val westRange = 9..64

        if (inUpRange) chorusData.setFace(BlockFace.UP, true)
        if (inDownRange) chorusData.setFace(BlockFace.DOWN, true)
        if (k in northRange step 2) {
            chorusData.setFace(BlockFace.NORTH, true)
        }

        for (n in westRange) {
            if (k !in n..n + 7) westRange step 8
            else chorusData.setFace(BlockFace.WEST, true)
        }

        for (n in southRange) {
            if (k !in n..n + 4) southRange step 4
            else chorusData.setFace(BlockFace.SOUTH, true)
        }

        for (n in eastRange) {
            if (k !in n..n + 1) eastRange step 2
            else chorusData.setFace(BlockFace.EAST, true)
        }

        blockMap.putIfAbsent(chorusData, k)
    }

    blockyPlugin.logger.fine("Finished generating blockMap!")
    return blockMap
}