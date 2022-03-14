package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.components.*
import com.mineinabyss.blocky.helpers.getBlockyBlockDataFromItem
import com.mineinabyss.blocky.helpers.getBlockyBlockFromBlock
import com.mineinabyss.blocky.helpers.getBlockyDecorationDataFromItem
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
import org.bukkit.event.block.*
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
        if (action == Action.PHYSICAL && material == Material.TRIPWIRE) isCancelled = true
    }

    @EventHandler
    fun BlockPlaceEvent.onPlacingBlockyBlock() {
        val blockyType = itemInHand.toGearyOrNull(player)?.get<BlockyType>() ?: return
        val blockyItem = itemInHand.toGearyOrNull(player)?.get<BlockyInfo>() ?: return
        if (blockyType.blockModelType == BlockModelType.ENTITY) return

        if (blockyType.blockType == BlockType.CUBE) {
            block.blockData = block.getBlockyBlockDataFromItem(blockyItem.modelId.toInt())
            return
        }
        else if (blockyType.blockType == BlockType.GROUND/* && blockAgainst.getFace(blockPlaced) == BlockFace.UP*/)
            block.setType(Material.TRIPWIRE, false)
        else if (blockyType.blockType == BlockType.WALL/* && blockAgainst.getFace(blockPlaced) != BlockFace.UP*/)
            block.setType(Material.GLOW_LICHEN, false)
        else isCancelled = true

        block.blockData = block.getBlockyDecorationDataFromItem(blockyItem.modelId.toInt())
    }

    @EventHandler
    fun PlayerInteractEvent.onPlacingBlockyEntity() {
        val item = player.inventory.itemInMainHand
        val geary = item.toGearyOrNull(player) ?: return
        val blockyType = geary.get<BlockyType>() ?: return
        val blockyInfo = geary.get<BlockyInfo>() ?: return
        val blockEntity = geary.get<BlockyEntity>() ?: return
        val loc = clickedBlock?.location?.toCenterLocation() ?: return

        if (hand != EquipmentSlot.HAND) return
        if (action != Action.RIGHT_CLICK_BLOCK) return
        if (blockyType.blockModelType != BlockModelType.ENTITY) return

        val prefabKey = geary.getOrSetPersisting { BlockyEntity(blockEntity.prefab) }
        loc.spawnFromPrefab(prefabKey.prefab) ?: error("Prefab ${prefabKey.prefab} not found")

        if (player.gameMode != GameMode.CREATIVE) item.subtract()
        player.playSound(loc, blockyInfo.placeSound, 1f, 1f)
    }

    @EventHandler
    fun EntityDamageByEntityEvent.onBreakingBlockyEntity() {
        val blocky = entity.toGeary().get<BlockyInfo>() ?: return
        if (!blocky.canBeBroken && (damager as Player).gameMode != GameMode.CREATIVE) isCancelled = true
    }

    //TODO Try and somehow do custom break-times depending on item in hand etc
    @EventHandler
    fun BlockDamageEvent.onMiningBlockyBlock() {
        /*val gearyBlock = block.getBlockyBlockFromBlock() ?: return
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

    @EventHandler
    fun BlockBreakEvent.onBreakingBlockyBlock() {
        val gearyBlock = block.getBlockyBlockFromBlock() ?: return
        val type = gearyBlock.get<BlockyType>() ?: return
        val info = gearyBlock.get<BlockyInfo>() ?: return

        if (type.blockModelType == BlockModelType.ENTITY) return
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