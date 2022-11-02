package com.mineinabyss.blocky.listeners

import com.destroystokyo.paper.MaterialTags
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.blocky.api.events.block.BlockyBlockBreakEvent
import com.mineinabyss.blocky.api.events.block.BlockyBlockDamageEvent
import com.mineinabyss.blocky.api.events.block.BlockyBlockPlaceEvent
import com.mineinabyss.blocky.blockyConfig
import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.blocky.components.core.BlockyInfo
import com.mineinabyss.blocky.components.features.mining.BlockyMining
import com.mineinabyss.blocky.components.features.mining.PlayerIsMining
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.idofront.events.call
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.idofront.time.inWholeTicks
import com.mineinabyss.looty.tracking.toGearyOrNull
import kotlinx.coroutines.delay
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Directional
import org.bukkit.block.data.type.Slab
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType.SLOW_DIGGING

class BlockyGenericListener : Listener {

    private fun Player.resetCustomBreak(block: Block) {
        toGeary {
            get<PlayerIsMining>()?.miningTask?.cancel() ?: return
            get<PlayerIsMining>()?.miningTask = null
            remove<PlayerIsMining>()
        }
        removePotionEffect(SLOW_DIGGING)
        block.location.getNearbyPlayers(16.0).forEach {
            it.sendBlockChange(block.location, block.blockData)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockDamageEvent.onDamage() {
        val info = block.gearyEntity?.get<BlockyInfo>() ?: return
        val mining = player.toGeary().getOrSetPersisting { PlayerIsMining() }
        val itemInHand = player.inventory.itemInMainHand.toGearyOrNull(player)?.get<BlockyMining>()
        val breakTime = info.blockBreakTime /
                (if (itemInHand?.toolTypes?.any { it in info.acceptedToolTypes } == true) itemInHand.breakSpeedModifier else 1.0)

        if (player.gameMode == GameMode.CREATIVE || info.isUnbreakable) return
        if (mining.miningTask != null) return
        isCancelled = true

        val damageEvent = BlockyBlockDamageEvent(block, player).run { call(); this }
        if (damageEvent.isCancelled) return

        mining.miningTask = blockyPlugin.launch {
            val effectTime = (breakTime.inWholeTicks * 1.1).toInt()
            var stage = 0

            player.addPotionEffect(PotionEffect(SLOW_DIGGING, effectTime, Int.MAX_VALUE, false, false, false))
            do { //TODO Fix visual glitch for blockbreaker
                block.location.getNearbyPlayers(16.0).forEach { p ->
                    p.sendBlockDamage(block.location, stage.toFloat() / 10, player.entityId)
                }
                delay(breakTime / 10)
            } while (player.toGeary().has<PlayerIsMining>() && stage++ < 10)

            block.attemptBreakBlockyBlock(player)
        }
    }

    // Cancel the custom break task if player stops breaking
    @EventHandler(priority = EventPriority.LOWEST)
    fun BlockDamageAbortEvent.onCancelMine() {
        player.resetCustomBreak(block)
    }

    // If player swaps item, cancel breaking to prevent exploits
    @EventHandler(priority = EventPriority.LOWEST)
    fun PlayerSwapHandItemsEvent.onSwapHand() {
        player.resetCustomBreak(player.getTargetBlock(null, 5))
    }

    // If player drops an item, cancel breaking to prevent exploits
    @EventHandler(priority = EventPriority.LOWEST)
    fun PlayerDropItemEvent.onDropHand() {
        player.resetCustomBreak(player.getTargetBlock(null, 5))
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockBreakEvent.onBreakBlockyBlock() {
        if (player.gameMode == GameMode.CREATIVE && block.isBlockyBlock) {
            block.attemptBreakBlockyBlock(player)
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.onPlaceGravityOnBlocky() {
        val block = clickedBlock ?: return
        val relative = block.getRelative(blockFace)
        val type = item?.clone()?.type ?: return

        if (action != Action.RIGHT_CLICK_BLOCK || block.type != Material.NOTE_BLOCK || hand != EquipmentSlot.HAND) return
        if (block.type.isInteractable && block.type != Material.NOTE_BLOCK) return

        if (type.hasGravity() && relative.getRelative(BlockFace.DOWN).type.isAir) {
            val data = Bukkit.createBlockData(type)
            if (Tag.ANVIL.isTagged(type))
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

        if (Tag.SLABS.isTagged(type) && relative.type == type) {
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

        MaterialTags.BUCKETS.isTagged(type) || return
        val bucketBlock = type.toString().replace("_BUCKET", "")
        val bucketEntity = runCatching { EntityType.valueOf(bucketBlock) }.getOrNull()

        if (MaterialTags.BUCKETS.isTagged(type) && type != Material.MILK_BUCKET) {
            if (!MaterialTags.FISH_BUCKETS.isTagged(type))
                type = Material.getMaterial(bucketBlock) ?: return
            else {
                type = Material.WATER
                player.world.spawnEntity(relative.location.add(0.5, 0.0, 0.5), bucketEntity!!)
            }
        }
        block.setType(type, false)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun BlockPlaceEvent.onPlacingDefaultBlock() {
        when {
            itemInHand.isBlockyBlock(player) -> return
            blockPlaced.isBlockyBlock -> return
            !blockyConfig.noteBlocks.isEnabled && itemInHand.type == Material.NOTE_BLOCK -> return
            !blockyConfig.tripWires.isEnabled && itemInHand.type == Material.STRING -> return
            !blockyConfig.caveVineBlocks.isEnabled && itemInHand.type == Material.CAVE_VINES -> return
            //!leafConfig.isEnabled && !leafList.contains(itemInHand.type) -> return
        }
        val material = when (itemInHand.type) {
            Material.STRING -> Material.TRIPWIRE
            else -> itemInHand.type
        }
        block.blockData = Bukkit.createBlockData(material)
        player.swingMainHand()
    }

    @EventHandler
    fun BlockyBlockPlaceEvent.o() {
        broadcast("BlockyBlockPlaceEvent")
    }

    @EventHandler
    fun BlockyBlockBreakEvent.d() {
        broadcast("BlockyBlockBreakEvent")
    }

    @EventHandler
    fun BlockyBlockDamageEvent.d() {
        broadcast("BlockyBlockDamageEvent")
    }
}
