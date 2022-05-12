package com.mineinabyss.blocky.helpers

import com.jeff_media.customblockdata.CustomBlockData
import com.jeff_media.morepersistentdatatypes.DataType
import com.mineinabyss.blocky.BlockyTypeQuery
import com.mineinabyss.blocky.BlockyTypeQuery.key
import com.mineinabyss.blocky.blockMap
import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.blocky.components.*
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.looty.tracking.toGearyOrNull
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
    if (!block.isBlockyBlock) return

    block.blockyInfo?.blockDrop?.map {
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
    val gearyItem = item.toGearyOrNull(player)

    if (REPLACEABLE_BLOCKS.contains(against.type)) targetBlock = against
    else {
        targetBlock = against.getRelative(face)
        if (!targetBlock.type.isAir && !targetBlock.isLiquid && targetBlock.type != Material.LIGHT) return null
    }

    if (against.isVanillaNoteBlock) return null
    if (gearyItem?.isVanillaNoteBlock == true)
        CustomBlockData(targetBlock, blockyPlugin).set(
            gearyItem.getVanillaNoteBlock?.key!!,
            DataType.BLOCK_DATA,
            newData
        )
    updateBlockyNote(targetBlock)

    if (isStandingInside(player, targetBlock)) return null

    val currentData = targetBlock.blockData
    val isFlowing = newData.material == Material.WATER || newData.material == Material.LAVA
    targetBlock.setBlockData(newData, isFlowing)

    val blockPlaceEvent = BlockPlaceEvent(targetBlock, targetBlock.state, against, item, player, true, hand)
    blockPlaceEvent.callEvent()

    if (!blockPlaceEvent.canBuild()) {
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
