package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.components.BlockType
import com.mineinabyss.blocky.components.BlockyBlock
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.blocky.components.BlockyLight
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Directional
import org.bukkit.block.data.type.Slab
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class BlockyGenericListener : Listener {

    @EventHandler
    fun BlockPistonExtendEvent.cancelBlockyPiston() {
        isCancelled = blocks.any { it.isBlockyCubeBlock() }
    }

    @EventHandler
    fun BlockPistonRetractEvent.cancelBlockyPiston() {
        isCancelled = blocks.any { it.isBlockyCubeBlock() }
    }

    //TODO Split this up and handle priority better
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.onInteractBlockyBlock() {
        val block = clickedBlock ?: return
        val relative = block.getRelative(blockFace)
        val item = item ?: return
        var type = item.clone().type

        if (action != Action.RIGHT_CLICK_BLOCK || block.type != Material.NOTE_BLOCK || hand != EquipmentSlot.HAND) return
        if (block.type.isInteractable && block.type != Material.NOTE_BLOCK) return

        if (item.type == Material.BUCKET && relative.isLiquid) {
            val sound =
                if (relative.type == Material.WATER) Sound.ITEM_BUCKET_FILL
                else Sound.valueOf("ITEM_BUCKET_FILL_" + relative.type)

            if (player.gameMode != GameMode.CREATIVE)
                item.type = Material.getMaterial("${relative.type}_BUCKET")!!

            player.playSound(relative.location, sound, 1.0f, 1.0f)
            relative.type = Material.AIR
            return
        }

        val bucketCheck = type.toString().endsWith("_BUCKET")
        val bucketBlock = type.toString().replace("_BUCKET", "")
        val bucketEntity = runCatching { EntityType.valueOf(bucketBlock) }.getOrNull()

        if (bucketCheck && type != Material.MILK_BUCKET) {
            if (bucketEntity == null)
                type = Material.getMaterial(bucketBlock) ?: return
            else {
                type = Material.WATER//
                player.world.spawnEntity(relative.location.add(0.5, 0.0, 0.5), bucketEntity)
            }
        }

        if (type.hasGravity() && relative.getRelative(BlockFace.DOWN).type.isAir) {
            val data = Bukkit.createBlockData(type)
            if (type.toString().endsWith("ANVIL"))
                (data as Directional).facing = getAnvilFacing(blockFace)
            block.world.spawnFallingBlock(relative.location.toBlockCenterLocation(), data)
            return
        }

        if (type.toString().endsWith("SLAB") && relative.type == type) {
            val sound = relative.blockSoundGroup
            val data = (relative.blockData as Slab)
            data.type = Slab.Type.DOUBLE
            relative.setBlockData(data, false)
            relative.world.playSound(relative.location, sound.placeSound, sound.volume, sound.pitch)
            if (player.gameMode != GameMode.CREATIVE) item.subtract()
            return
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
        val blockyLight = gearyItem.get<BlockyLight>()?.lightLevel
        val against = clickedBlock ?: return
        if ((against.type.isInteractable && against.getPrefabFromBlock()
                ?.toEntity() == null) && !player.isSneaking
        ) return

        if (!gearyItem.has<BlockyInfo>()) return
        if (blockyBlock.blockType != BlockType.CUBE &&
            blockyBlock.blockType != BlockType.TRANSPARENT
        ) return

        val newData = if (blockyBlock.blockType == BlockType.TRANSPARENT)
            gearyItem.getBlockyTransparent(blockFace)
        else
            gearyItem.getBlockyNoteBlock(blockFace)
        val placed = placeBlockyBlock(player, hand!!, item!!, against, blockFace, newData) ?: return

        if (gearyItem.has<BlockyLight>()) createBlockLight(placed.location, blockyLight!!)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockPlaceEvent.onPlacingBlockyBlock() {
        if (itemInHand.toGearyOrNull(player)?.has<BlockyBlock>() != true &&
            itemInHand.type == Material.TRIPWIRE ||
            itemInHand.type == Material.NOTE_BLOCK ||
            itemInHand.type == Material.CHORUS_PLANT
        )
            block.setBlockData(Bukkit.createBlockData(itemInHand.type), false)

        val gearyItem = itemInHand.toGearyOrNull(player) ?: return
        val blockyBlock = gearyItem.get<BlockyBlock>() ?: return
        val blockFace = blockAgainst.getFace(blockPlaced) ?: BlockFace.UP
        if (!gearyItem.has<BlockyInfo>()) return

        if (blockyBlock.blockType != BlockType.CUBE &&
            blockyBlock.blockType != BlockType.TRANSPARENT
        ) return

        if (blockyBlock.blockType == BlockType.TRANSPARENT)
            block.setBlockData(gearyItem.getBlockyTransparent(blockFace), false)
        else block.setBlockData(gearyItem.getBlockyNoteBlock(blockFace), false)
        player.swingMainHand()
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockBreakEvent.onBreakingBlockyBlock() {
        val blockyInfo = block.getGearyEntityFromBlock()?.get<BlockyInfo>() ?: return

        if (!block.isBlockyCubeBlock()) return
        if (blockyInfo.isUnbreakable && player.gameMode != GameMode.CREATIVE) isCancelled = true
        breakBlockyBlock(block, player)
        isDropItems = false
    }

    @EventHandler
    fun EntityExplodeEvent.onExplodingBlocky() {
        blockList().forEach { block ->
            val prefab = block.getGearyEntityFromBlock() ?: return@forEach
            if (prefab.has<BlockyInfo>()) handleBlockyDrops(block, null)
            block.setType(Material.AIR, false)
        }
    }
}
