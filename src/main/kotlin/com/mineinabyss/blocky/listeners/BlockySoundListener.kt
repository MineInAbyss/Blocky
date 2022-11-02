package com.mineinabyss.blocky.listeners

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.blocky.components.core.BlockySound
import com.mineinabyss.blocky.components.mining.PlayerIsMining
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay
import org.bukkit.*
import org.bukkit.block.BlockFace
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageAbortEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerAttemptPickupItemEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.world.GenericGameEvent

class BlockySoundListener : Listener {

    @EventHandler(ignoreCancelled = true)
    fun BlockDamageEvent.onStartBreaking() {
        if (instaBreak || player.gameMode == GameMode.CREATIVE) return
        val hitGroup = block.blockSoundGroup.hitSound
        if (hitGroup != Sound.BLOCK_WOOD_HIT && hitGroup != Sound.BLOCK_STONE_HIT) return
        val sound = block.gearyEntity?.get<BlockySound>()?.hitSound
            ?: if (hitGroup == Sound.BLOCK_WOOD_HIT) VANILLA_WOOD_BREAK else VANILLA_STONE_BREAK

        player.toGeary().getOrSet { PlayerIsMining() }
        blockyPlugin.launch {
            do {
                block.world.playSound(block.location, sound, SoundCategory.BLOCKS, DEFAULT_HIT_VOLUME, DEFAULT_HIT_PITCH)
                delay(3.ticks) // Add small delay to mimic vanilla
            } while (player.toGeary().has<PlayerIsMining>())
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockDamageAbortEvent.onStopBreaking() {
        if (player.toGeary().has<PlayerIsMining>()) player.toGeary().remove<PlayerIsMining>()
        player.stopSound(block.gearyEntity?.get<BlockySound>()?.hitSound
                ?: if (block.blockSoundGroup.hitSound == Sound.BLOCK_WOOD_HIT) VANILLA_WOOD_HIT else VANILLA_STONE_HIT)
    }

    // Stop sound from custom block-breaking when player picks item into main hand
    @EventHandler(ignoreCancelled = true)
    fun PlayerAttemptPickupItemEvent.onPickupItem() {
        val block = player.getTargetBlock(null, 5)
        if (player.toGeary().has<PlayerIsMining>() && player.inventory.itemInMainHand.type == Material.AIR)
            player.toGeary().remove<PlayerIsMining>()
        player.stopSound(block.gearyEntity?.get<BlockySound>()?.hitSound
            ?: if (block.blockSoundGroup.hitSound == Sound.BLOCK_WOOD_HIT) VANILLA_WOOD_HIT else VANILLA_STONE_HIT)
    }

    @EventHandler
    fun PlayerSwapHandItemsEvent.onSwapHand() {
        val block = player.getTargetBlock(null, 5)
        if (player.toGeary().has<PlayerIsMining>() && player.inventory.itemInMainHand.type == Material.AIR)
            player.toGeary().remove<PlayerIsMining>()
        player.stopSound(block.gearyEntity?.get<BlockySound>()?.hitSound
            ?: if (block.blockSoundGroup.hitSound == Sound.BLOCK_WOOD_HIT) VANILLA_WOOD_HIT else VANILLA_STONE_HIT)
    }

    @EventHandler(ignoreCancelled = true)
    fun GenericGameEvent.onSound() {
        if (entity !is  LivingEntity || !location.isWorldLoaded || !location.world.isChunkLoaded(location.chunk)) return
        val block = entity?.location?.block?.getRelative(BlockFace.DOWN) ?: return
        val stepGroup = block.blockSoundGroup.stepSound
        if (stepGroup != Sound.BLOCK_WOOD_STEP && stepGroup != Sound.BLOCK_STONE_STEP) return
        if (event != GameEvent.STEP && event != GameEvent.HIT_GROUND) return
        if (event == GameEvent.HIT_GROUND && (entity as LivingEntity).lastDamageCause?.cause != EntityDamageEvent.DamageCause.FALL) return

        val blockySound = block.gearyEntity?.get<BlockySound>()
        val currentBlock = entity?.location?.block ?: return
        if (!currentBlock.isReplaceable || currentBlock.type == Material.TRIPWIRE) return

        val sound = when (event) {
            GameEvent.STEP -> blockySound?.stepSound
                ?: if (stepGroup == Sound.BLOCK_WOOD_STEP) VANILLA_WOOD_STEP else VANILLA_STONE_STEP

            GameEvent.HIT_GROUND -> blockySound?.fallSound
                ?: if (stepGroup == Sound.BLOCK_WOOD_STEP) VANILLA_WOOD_FALL else VANILLA_STONE_FALL

            else -> return
        }
        val volume = when (event) {
            GameEvent.STEP -> DEFAULT_STEP_VOLUME
            GameEvent.HIT_GROUND -> DEFAULT_FALL_VOLUME
            else -> return
        }
        val pitch = when (event) {
            GameEvent.STEP -> DEFAULT_STEP_PITCH
            GameEvent.HIT_GROUND -> DEFAULT_FALL_PITCH
            else -> return
        }

        block.world.playSound(block.location, sound, SoundCategory.PLAYERS, volume, pitch)
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockBreakEvent.onBreak() {
        val breakGroup = block.blockSoundGroup.breakSound
        if (breakGroup != Sound.BLOCK_WOOD_BREAK && breakGroup != Sound.BLOCK_STONE_BREAK) return
        if (player.toGeary().has<PlayerIsMining>()) player.toGeary().remove<PlayerIsMining>()

        val sound = block.gearyEntity?.get<BlockySound>()?.breakSound
            ?: if (breakGroup == Sound.BLOCK_WOOD_BREAK) VANILLA_WOOD_BREAK else VANILLA_STONE_BREAK

        block.world.playSound(block.location, sound, SoundCategory.BLOCKS, DEFAULT_BREAK_VOLUME, DEFAULT_BREAK_PITCH)
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockPlaceEvent.onPlace() {
        val placeGroup = block.blockSoundGroup.placeSound
        if (placeGroup != Sound.BLOCK_WOOD_PLACE && placeGroup != Sound.BLOCK_STONE_PLACE) return

        val sound = block.gearyEntity?.get<BlockySound>()?.placeSound
            ?: if (placeGroup == Sound.BLOCK_WOOD_PLACE) VANILLA_WOOD_PLACE else VANILLA_STONE_PLACE

        block.world.playSound(block.location, sound, SoundCategory.BLOCKS, DEFAULT_PLACE_VOLUME, DEFAULT_PLACE_PITCH)
    }
}
