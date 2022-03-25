package com.mineinabyss.blocky.helpers

import com.mineinabyss.blocky.components.BlockType
import com.mineinabyss.blocky.components.BlockyBlock
import com.mineinabyss.blocky.components.BlockyInfo
import org.bukkit.block.Block
import org.bukkit.block.data.type.GlowLichen
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import kotlin.random.Random

fun handleBlockyDrops(block: Block, player: Player) {
    val gearyBlock = block.getPrefabFromBlock() ?: return
    val blocky = gearyBlock.get<BlockyBlock>() ?: return
    val info = gearyBlock.get<BlockyInfo>() ?: return

    info.blockDrop.map {
        val hand = player.inventory.itemInMainHand
        val item =
            if (it.affectedBySilkTouch && hand.containsEnchantment(Enchantment.SILK_TOUCH))
                it.silkTouchedDrop.toItemStack()
            else it.item.toItemStack()


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
    if (blocky.blockType == BlockType.WALL) {
        (block.blockData as GlowLichen).isWaterlogged = false
    }
}