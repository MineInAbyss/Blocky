package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.helpers.*
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
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class BlockyGenericListener : Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.onPlaceGravityOnBlocky() {
        val block = clickedBlock ?: return
        val relative = block.getRelative(blockFace)
        val type = item?.clone()?.type ?: return

        if (action != Action.RIGHT_CLICK_BLOCK || block.type != Material.NOTE_BLOCK || hand != EquipmentSlot.HAND) return
        if (block.type.isInteractable && block.type != Material.NOTE_BLOCK) return

        if (type.hasGravity() && relative.getRelative(BlockFace.DOWN).type.isAir) {
            val data = Bukkit.createBlockData(type)
            if (type.toString().endsWith("ANVIL"))
                (data as Directional).facing = getAnvilFacing(blockFace)
            block.world.spawnFallingBlock(relative.location.toBlockCenterLocation(), data)
            isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.onMakingDoubleSlabOnBlockyBlock() {
        val block = clickedBlock ?: return
        val relative = block.getRelative(blockFace)
        val item = item ?: return
        val type = item.clone().type

        if (action != Action.RIGHT_CLICK_BLOCK || block.type != Material.NOTE_BLOCK || hand != EquipmentSlot.HAND) return
        if (block.type.isInteractable && block.type != Material.NOTE_BLOCK) return

        if (type.toString().endsWith("SLAB") && relative.type == type) {
            val sound = relative.blockSoundGroup
            val data = (relative.blockData as Slab)
            data.type = Slab.Type.DOUBLE
            relative.setBlockData(data, false)
            relative.world.playSound(relative.location, sound.placeSound, sound.volume, sound.pitch)
            if (player.gameMode != GameMode.CREATIVE) item.subtract()
            isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.onPlaceLiquidOnBlockyBlock() {
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
                type = Material.WATER
                player.world.spawnEntity(relative.location.add(0.5, 0.0, 0.5), bucketEntity)
            }
        }
        block.setType(type, false)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun PlayerInteractEvent.onInteractBlockyBlock() {
        val block = clickedBlock ?: return
        val item = item ?: return
        val type = item.clone().type

        if (action != Action.RIGHT_CLICK_BLOCK || block.type != Material.NOTE_BLOCK || hand != EquipmentSlot.HAND) return
        if (block.type.isInteractable && block.type != Material.NOTE_BLOCK) return
        if (type.isBlock) placeBlockyBlock(player, hand!!, item, block, blockFace, Bukkit.createBlockData(type))
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun BlockPlaceEvent.onPlacingDefaultBlock() {
        when {
            itemInHand.isBlockyBlock(player) -> return
            !blockPlaced.isBlockyBlock() -> return
            !noteConfig.isEnabled && itemInHand.type == Material.NOTE_BLOCK -> return
            !chorusConfig.isEnabled && itemInHand.type == Material.CHORUS_PLANT -> return
            !tripwireConfig.isEnabled && itemInHand.type == Material.STRING -> return
            //!leafConfig.isEnabled && !leafList.contains(itemInHand.type) -> return
        }

        block.setBlockData(Bukkit.createBlockData(itemInHand.type), false)
        player.swingMainHand()
    }
}
