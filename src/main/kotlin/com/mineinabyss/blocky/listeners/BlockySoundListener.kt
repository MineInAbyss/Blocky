package com.mineinabyss.blocky.listeners

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.blocky.components.BlockySound
import com.mineinabyss.blocky.components.PlayerIsMining
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay
import org.bukkit.*
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageAbortEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.world.GenericGameEvent

class BlockySoundListener : Listener {

    @EventHandler(ignoreCancelled = true)
    fun BlockDamageEvent.onStartBreaking() {
        if (instaBreak || player.gameMode == GameMode.CREATIVE) return
        val hitGroup = block.blockSoundGroup.hitSound
        if (hitGroup != Sound.BLOCK_WOOD_HIT && hitGroup != Sound.BLOCK_STONE_HIT) return
        val sound = block.getGearyEntityFromBlock()?.get<BlockySound>()?.hitSound
            ?: if (hitGroup == Sound.BLOCK_WOOD_HIT) woodBreakSound else stoneBreakSound

        player.toGeary().getOrSet { PlayerIsMining() }
        blockyPlugin.launch {
            do {
                block.world.playSound(block.location, sound, SoundCategory.BLOCKS, 1.0f, 1.0f)
                delay(3.ticks) // Add small delay to mimic vanilla
            } while (player.toGeary().has<PlayerIsMining>())
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockDamageAbortEvent.onStopBreaking() {
        if (player.toGeary().has<PlayerIsMining>()) player.toGeary().remove<PlayerIsMining>()
        player.stopSound(block.getGearyEntityFromBlock()?.get<BlockySound>()?.hitSound
                ?: if (block.blockSoundGroup.hitSound == Sound.BLOCK_WOOD_HIT) woodHitSound else stoneHitSound)
    }

    @EventHandler(ignoreCancelled = true)
    fun GenericGameEvent.onSound() {
        // Some builds of Paper apparently crash when this is fired for an unloaded chunk
        // Usually only when going to new dimensions before it is fully loaded.
        if (!location.isWorldLoaded || !location.world.isChunkLoaded(location.chunk)) return
        val block = entity?.location?.block?.getRelative(BlockFace.DOWN) ?: return
        val stepGroup = block.blockSoundGroup.stepSound
        if (stepGroup != Sound.BLOCK_WOOD_STEP && stepGroup != Sound.BLOCK_STONE_STEP) return
        if (event != GameEvent.STEP && event != GameEvent.HIT_GROUND) return

        val blockySound = block.getGearyEntityFromBlock()?.get<BlockySound>()
        val currentBlock = entity?.location?.block ?: return
        if (!currentBlock.isReplaceable || currentBlock.type == Material.TRIPWIRE) return

        val sound = when (event) {
            GameEvent.STEP -> blockySound?.stepSound
                ?: if (stepGroup == Sound.BLOCK_WOOD_STEP) woodStepSound else stoneStepSound

            GameEvent.HIT_GROUND -> blockySound?.fallSound
                ?: if (stepGroup == Sound.BLOCK_WOOD_STEP) woodFallSound else stoneFallSound

            else -> return
        }

        block.world.playSound(block.location, sound, SoundCategory.PLAYERS, 1.0f, 1.0f)
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockBreakEvent.onBreak() {
        val breakGroup = block.blockSoundGroup.breakSound
        if (breakGroup != Sound.BLOCK_WOOD_BREAK && breakGroup != Sound.BLOCK_STONE_BREAK) return
        if (player.toGeary().has<PlayerIsMining>()) player.toGeary().remove<PlayerIsMining>()

        val sound = block.getGearyEntityFromBlock()?.get<BlockySound>()?.breakSound
            ?: if (breakGroup == Sound.BLOCK_WOOD_BREAK) woodBreakSound else stoneBreakSound

        block.world.playSound(block.location, sound, SoundCategory.BLOCKS, 1.0f, 1.0f)
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockPlaceEvent.onPlace() {
        val placeGroup = block.blockSoundGroup.placeSound
        if (placeGroup != Sound.BLOCK_WOOD_PLACE && placeGroup != Sound.BLOCK_STONE_PLACE) return

        val sound = block.getGearyEntityFromBlock()?.get<BlockySound>()?.placeSound
            ?: if (placeGroup == Sound.BLOCK_WOOD_PLACE) woodPlaceSound else stonePlaceSound

        block.world.playSound(block.location, sound, SoundCategory.BLOCKS, 1.0f, 1.0f)
    }
}
