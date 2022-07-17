package com.mineinabyss.blocky.listeners

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.blocky.components.BlockySound
import com.mineinabyss.blocky.components.PlayerIsMining
import com.mineinabyss.blocky.helpers.getGearyEntityFromBlock
import com.mineinabyss.blocky.helpers.noteConfig
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
        if (block.blockSoundGroup.hitSound != Sound.BLOCK_WOOD_HIT) return
        if (instaBreak || player.gameMode == GameMode.CREATIVE) return

        val geary = block.getGearyEntityFromBlock()
        val sound = (geary?.get<BlockySound>()?.hitSound ?: noteConfig.woodHitSound)
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
        player.stopSound(block.getGearyEntityFromBlock()?.get<BlockySound>()?.hitSound ?: noteConfig.woodHitSound)
    }

    @EventHandler(ignoreCancelled = true)
    fun GenericGameEvent.onSound() {
        val block = entity?.location?.block?.getRelative(BlockFace.DOWN) ?: return
        val currentBlock = entity?.location?.block ?: return
        val blockySound = block.getGearyEntityFromBlock()?.get<BlockySound>()

        if (!currentBlock.isReplaceable || currentBlock.type == Material.TRIPWIRE) return
        if (block.blockSoundGroup.fallSound != Sound.BLOCK_WOOD_FALL) return

        val sound = when (event) {
            GameEvent.STEP -> blockySound?.stepSound ?: noteConfig.woodStepSound
            GameEvent.HIT_GROUND -> blockySound?.fallSound ?: noteConfig.woodFallSound
            else -> return
        }
        block.world.playSound(block.location, sound, 1.0f, 1.0f)
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockBreakEvent.onBreak() {
        if (block.blockSoundGroup.breakSound != Sound.BLOCK_WOOD_BREAK) return
        if (player.toGeary().has<PlayerIsMining>()) player.toGeary().remove<PlayerIsMining>()

        val geary = block.getGearyEntityFromBlock()
        val sound = geary?.get<BlockySound>()?.breakSound ?: noteConfig.woodBreakSound
        block.world.playSound(block.location, sound, SoundCategory.BLOCKS, 1.0f, 1.0f)
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockPlaceEvent.onPlace() {
        if (block.blockSoundGroup.placeSound != Sound.BLOCK_WOOD_PLACE) return
        val geary = block.getGearyEntityFromBlock()
        val sound = geary?.get<BlockySound>()?.placeSound ?: noteConfig.woodPlaceSound
        block.world.playSound(block.location, sound, SoundCategory.BLOCKS, 1.0f, 1.0f)
    }
}
