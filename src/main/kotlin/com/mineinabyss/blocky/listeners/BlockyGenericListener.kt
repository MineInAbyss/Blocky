package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.components.*
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.BlockFace
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class BlockyGenericListener : Listener {

    @EventHandler
    fun BlockPistonExtendEvent.cancelBlockyPiston() {
        if (blocks.stream().anyMatch
            {
                it.type == Material.NOTE_BLOCK ||
                        it.type == Material.CHORUS_PLANT ||
                        it.type == Material.CHORUS_FLOWER
            }
        ) isCancelled = true
    }

    @EventHandler
    fun BlockPistonRetractEvent.cancelBlockyPiston() {
        if (blocks.stream().anyMatch
            {
                it.type == Material.NOTE_BLOCK ||
                        it.type == Material.CHORUS_PLANT ||
                        it.type == Material.CHORUS_FLOWER
            }
        ) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.onInteractBlockyBlock() {
        val block = clickedBlock ?: return
        val blockAbove = block.getRelative(BlockFace.UP)
        val item = item ?: return
        var type = item.clone().type
        if (action != Action.RIGHT_CLICK_BLOCK) return
        if (hand != EquipmentSlot.HAND) return
        if (block.type != Material.NOTE_BLOCK) return
        if (block.type.isInteractable && block.getPrefabFromBlock()?.toEntity()
                ?.has<BlockyBlock>() != true && !player.isSneaking
        ) return
        if (item.type == Material.BUCKET && blockAbove.isLiquid) {
            val sound =
                if (blockAbove.type == Material.WATER) Sound.ITEM_BUCKET_FILL
                else Sound.valueOf("ITEM_BUCKET_FILL_" + blockAbove.type)

            if (player.gameMode != GameMode.CREATIVE)
                item.type = Material.getMaterial("${blockAbove.type}_BUCKET")!!

            player.playSound(blockAbove.location, sound, 1.0f, 1.0f)
            blockAbove.type = Material.AIR
            return
        }

        val bucketCheck = type.toString().endsWith("_BUCKET")
        val bucketBlock = type.toString().replace("_BUCKET", "")
        val bucketEntity = runCatching { EntityType.valueOf(bucketBlock) }.getOrNull()

        if (bucketCheck && type != Material.MILK_BUCKET) {
            if (bucketEntity == null)
                type = Material.getMaterial(bucketBlock) ?: return
            else {
                type = Material.WATER
                player.world.spawnEntity(blockAbove.location.add(0.5, 0.0, 0.5), bucketEntity)
            }
        }

        if (type.isBlock) placeBlockyBlock(
            player,
            hand!!,
            item,
            block,
            blockFace,
            Bukkit.createBlockData(type)
        )
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.onPrePlacingBlockyBlock() {
        if (action != Action.RIGHT_CLICK_BLOCK) return
        if (hand != EquipmentSlot.HAND) return

        val gearyItem = item?.toGearyOrNull(player) ?: return
        val blockyBlock = gearyItem.get<BlockyBlock>() ?: return
        val blockyType = blockyBlock.blockType
        val blockyLight = gearyItem.get<BlockyLight>()?.lightLevel
        val blockySound = gearyItem.get<BlockySound>()
        val against = clickedBlock ?: return
        if (against.type.isInteractable && against.getPrefabFromBlock()?.toEntity()
                ?.has<BlockyLight>() != true && !player.isSneaking
        ) return

        if (!gearyItem.has<BlockyInfo>()) return

        val newData =
            when (blockyType) {
                BlockType.CUBE -> gearyItem.getBlockyNoteBlock(blockFace)
                BlockType.TRANSPARENT -> gearyItem.getBlockyTransparent(blockFace)
                else -> return
            }

        val placed = placeBlockyBlock(player, hand!!, item!!, against, blockFace, newData) ?: return

        if (gearyItem.has<BlockySound>()) placed.world.playSound(placed.location, blockySound!!.placeSound, 1.0f, 0.8f)
        if (gearyItem.has<BlockyLight>()) createBlockLight(placed.location, blockyLight!!)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockPlaceEvent.onPlacingBlockyBlock() {
        if (itemInHand.toGearyOrNull(player)?.has<BlockyBlock>() != true) {
            if (itemInHand.type == Material.TRIPWIRE ||
                itemInHand.type == Material.NOTE_BLOCK ||
                itemInHand.type == Material.CHORUS_PLANT
            ) block.setBlockData(Bukkit.createBlockData(itemInHand.type), false)
        }


        val gearyItem = itemInHand.toGearyOrNull(player) ?: return
        val blockyBlock = gearyItem.get<BlockyBlock>() ?: return
        val type = blockyBlock.blockType
        val blockFace = blockAgainst.getFace(blockPlaced) ?: BlockFace.UP
        if (!gearyItem.has<BlockyInfo>()) return

        when (type) {
            BlockType.CUBE -> block.setBlockData(gearyItem.getBlockyNoteBlock(blockFace), false)
            BlockType.TRANSPARENT -> block.setBlockData(gearyItem.getBlockyTransparent(blockFace), false)
            BlockType.SLAB -> block.setType(Material.PETRIFIED_OAK_SLAB, false)
            else -> return
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockBreakEvent.onBreakingBlockyBlock() {
        val blockyInfo = block.getPrefabFromBlock()?.toEntity()?.get<BlockyInfo>() ?: return
        val blockType = block.getPrefabFromBlock()?.toEntity()?.get<BlockyBlock>()?.blockType ?: return

        if (isCancelled || !isDropItems) return
        if (blockType != BlockType.CUBE && blockType != BlockType.TRANSPARENT && blockType != BlockType.SLAB) return
        if (blockyInfo.isUnbreakable && player.gameMode != GameMode.CREATIVE) isCancelled = true
        breakBlockyBlock(block, player)
        isDropItems = false
    }
}
