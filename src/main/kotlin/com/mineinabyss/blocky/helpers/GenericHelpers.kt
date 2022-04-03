package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.BlockyTypeQuery
import com.mineinabyss.blocky.BlockyTypeQuery.key
import com.mineinabyss.blocky.components.BlockType
import com.mineinabyss.blocky.components.BlockyBlock
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.blocky.systems.BlockHardnessModifiers
import com.mineinabyss.geary.prefabs.PrefabKey
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
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
    val blocky = gearyBlock.get<BlockyBlock>() ?: return
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

fun getBreakModifier(): BlockHardnessModifiers {
    return object : BlockHardnessModifiers {
        override fun isTriggered(player: Player, block: Block, tool: ItemStack): Boolean {
            if (block.type != Material.NOTE_BLOCK) return false
            return block.getPrefabFromBlock() != null
        }

        override fun breakBlocky(player: Player, block: Block, tool: ItemStack) {
            block.type = Material.AIR
        }

        override fun getBreakTime(player: Player, block: Block, tool: ItemStack): Long {
            val prefab = block.getPrefabFromBlock()?.toEntity() ?: return 0L
            val info = prefab.get<BlockyInfo>() ?: return 0L
            val period: Long = info.blockBreakTime.toLong()
            val modifier = 1.0
            return (period * modifier).toLong()
        }
    }
}

fun Block.getPrefabFromBlock(): PrefabKey? {

    val type =
        when (type) {
            Material.NOTE_BLOCK -> BlockType.CUBE
            Material.TRIPWIRE -> BlockType.GROUND
            else -> return null
        }

    val blockyBlock = BlockyTypeQuery.firstOrNull {
        it.entity.get<BlockyBlock>()?.blockId == blockMap[blockData] &&
                it.entity.get<BlockyBlock>()?.blockType == type
    }?.key ?: return null
    return blockyBlock
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

    if (!blockPlaceEvent.canBuild() || blockPlaceEvent.isCancelled) {
        targetBlock.setBlockData(currentData, false) // false to cancel physic
        return null
    }

    if (player.gameMode != GameMode.CREATIVE) item.subtract(1)
    return targetBlock
}