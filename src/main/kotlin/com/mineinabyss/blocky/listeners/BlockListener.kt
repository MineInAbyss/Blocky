package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.components.BlockModelType
import com.mineinabyss.blocky.components.BlockType
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.blocky.components.BlockyType
import com.mineinabyss.blocky.helpers.getBlockyBlockDataFromItem
import com.mineinabyss.blocky.helpers.getBlockyBlockFromBlock
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.spawnFromPrefab
import com.mineinabyss.idofront.entities.rightClicked
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.NotePlayEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import kotlin.random.Random


class BlockListener : Listener {

    @EventHandler
    fun NotePlayEvent.cancelBlockyNotes() {
        isCancelled = true
    }

    @EventHandler
    fun PlayerInteractEvent.onChangingNote() {
        if (clickedBlock?.type == Material.NOTE_BLOCK && rightClicked) isCancelled = true
    }

    @EventHandler
    fun BlockPlaceEvent.onPlacingBlockyBlock() {
        val blockyType = itemInHand.toGearyOrNull(player)?.get<BlockyType>() ?: return
        val blockyItem = itemInHand.toGearyOrNull(player)?.get<BlockyInfo>() ?: return

        //if (!itemInHand.itemMeta.hasCustomModelData()) return
        if (blockyType.blockType == BlockType.NORMAL) {
            block.setType(Material.NOTE_BLOCK, false)
            block.blockData = block.getBlockyBlockDataFromItem(blockyItem.modelId.toInt())
        } else if (blockyType.blockType == BlockType.PASSTHROUGH) {
            block.setType(Material.TRIPWIRE, false)
        }
    }

    @EventHandler
    fun PlayerInteractEvent.onPlacingBlockyMisc() {
        val item = player.inventory.itemInMainHand
        val blockyType = item.toGearyOrNull(player)?.get<BlockyType>() ?: return
        val blockyInfo = item.toGearyOrNull(player)?.get<BlockyInfo>() ?: return
        val loc = clickedBlock?.location?.toCenterLocation() ?: return

        if (hand != EquipmentSlot.HAND) return
        if (action != Action.RIGHT_CLICK_BLOCK) return
        if (blockyType.blockType == BlockType.NORMAL || blockyType.blockType == BlockType.PASSTHROUGH) return

        //TODO This will probably not work like it does in Mobzy
        if (blockyType.blockModelType == BlockModelType.MODELENGINE) {
            loc.spawnFromPrefab(item.toGearyOrNull(player)!!)

            if (player.gameMode != GameMode.CREATIVE) player.inventory.itemInMainHand.subtract()
            player.playSound(loc, blockyInfo.placeSound, 1f, 1f)
        }
    }

    @EventHandler
    fun EntityDamageByEntityEvent.onBreakingBlockyMisc() {
        val blocky = entity.toGeary().get<BlockyInfo>() ?: return
        if (!blocky.canBeBroken && (damager as Player).gameMode != GameMode.CREATIVE) isCancelled = true
    }

    @EventHandler
    fun BlockBreakEvent.onBreakingBlockyBlock() {
        val gearyBlock = block.getBlockyBlockFromBlock() ?: return
        val type = gearyBlock.get<BlockyType>() ?: return
        val info = gearyBlock.get<BlockyInfo>() ?: return

        if (type.blockModelType == BlockModelType.MODELENGINE) return
        isDropItems = false

        info.blockDrop.map {
            val hand = player.inventory.itemInMainHand
            val item =
                if (it.affectedBySilkTouch && hand.containsEnchantment(Enchantment.SILK_TOUCH))
                    it.silkTouchedDrop.toItemStack()
                else it.item.toItemStack()

            //TODO Make fortune check here not ass
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
    }


}