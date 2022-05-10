package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.BlockyTypeQuery
import com.mineinabyss.blocky.BlockyTypeQuery.key
import com.mineinabyss.blocky.blockMap
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
    val blockMap = mutableMapOf<BlockData, Int>()

    // Calculates tripwire states
    for (i in 0..127) {
        val tripWireData = Bukkit.createBlockData(Material.TRIPWIRE) as Tripwire
        if (i and 1 == 1) tripWireData.setFace(BlockFace.NORTH, true)
        if (i shr 1 and 1 == 1) tripWireData.setFace(BlockFace.EAST, true)
        if (i shr 2 and 1 == 1) tripWireData.setFace(BlockFace.SOUTH, true)
        if (i shr 3 and 1 == 1) tripWireData.setFace(BlockFace.WEST, true)
        if (i shr 4 and 1 == 1) tripWireData.isPowered = true
        if (i shr 5 and 1 == 1) tripWireData.isAttached = true
        if (i shr 6 and 1 == 1) tripWireData.isDisarmed = true

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