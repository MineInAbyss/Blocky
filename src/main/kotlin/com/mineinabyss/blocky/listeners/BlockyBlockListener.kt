package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.components.BlockType
import com.mineinabyss.blocky.components.BlockyBlock
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.blocky.helpers.getBlockyBlockDataFromItem
import com.mineinabyss.blocky.helpers.getBlockyDecorationDataFromItem
import com.mineinabyss.blocky.helpers.getPrefabFromBlock
import com.mineinabyss.idofront.entities.rightClicked
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.NotePlayEvent
import org.bukkit.event.player.PlayerInteractEvent


class BlockyBlockListener : Listener {

    @EventHandler
    fun NotePlayEvent.cancelBlockyNotes() {
        isCancelled = true
    }

    @EventHandler
    fun PlayerInteractEvent.onChangingNote() {
        if (clickedBlock?.type == Material.NOTE_BLOCK && rightClicked) isCancelled = true
        if (action == Action.PHYSICAL && material == Material.TRIPWIRE) isCancelled = true
    }

    @EventHandler
    fun BlockPlaceEvent.onPlacingBlockyBlock() {
        itemInHand.toGearyOrNull(player)?.get<BlockyInfo>() ?: return
        val blockyBlock = itemInHand.toGearyOrNull(player)?.get<BlockyBlock>() ?: return

        if (blockyBlock.blockType == BlockType.CUBE) {
            block.blockData = block.getBlockyBlockDataFromItem(blockyBlock.blockId)
            return
        } else if (blockyBlock.blockType == BlockType.GROUND && blockAgainst.getFace(blockPlaced) == BlockFace.UP)
            block.setType(Material.TRIPWIRE, false)
        else if (blockyBlock.blockType == BlockType.WALL && blockAgainst.getFace(blockPlaced) != BlockFace.UP)
            block.setType(Material.GLOW_LICHEN, false)
        else isCancelled = true

        block.blockData = block.getBlockyDecorationDataFromItem(blockyBlock.blockId)
    }

    //TODO Try and somehow do custom break-times depending on item in hand etc
    @EventHandler
    fun BlockDamageEvent.onMiningBlockyBlock() {
        val gearyBlock = block.getPrefabFromBlock() ?: return
        val blocky = gearyBlock.get<BlockyInfo>() ?: return
        if (blocky.isUnbreakable && player.gameMode != GameMode.CREATIVE) isCancelled = true


        //isCancelled = false

        /*
        val type = gearyBlock.get<BlockyType>() ?: return
        val info = gearyBlock.get<BlockyInfo>() ?: return
        var progress = 0F
        var timePassed = 0F
        val lastTime = System.currentTimeMillis()

        if (type.blockModelType == BlockModelType.MODELENGINE) return
        if (!info.canBeBroken && player.gameMode != GameMode.CREATIVE) return

        blockyPlugin.schedule {
            do {
                player.addPotionEffect(PotionEffect(PotionEffectType.SLOW_DIGGING, -1, 1, false, false))
                timePassed = lastTime - System.currentTimeMillis().toFloat()
                progress = info.blockBreakTime.toFloat() - timePassed
                player.sendBlockDamage(block.location, progress)
                waitFor(1)
            } while (progress < 1F)
            player.removePotionEffect(PotionEffectType.SLOW_DIGGING)
        }*/
    }

    //TODO Make this into its own component
    /*@EventHandler
    fun BlockBreakEvent.onBreakingBlockyBlock() {
        val gearyBlock = block.getPrefabFromBlock() ?: return
        val blocky = gearyBlock.get<BlockyBlock>() ?: return
        val info = gearyBlock.get<BlockyInfo>() ?: return

        isDropItems = false

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
            expToDrop = it.exp
        }
        if (blocky.blockType == BlockType.WALL) {
            (block.blockData as GlowLichen).isWaterlogged = false
        }
    }*/
}