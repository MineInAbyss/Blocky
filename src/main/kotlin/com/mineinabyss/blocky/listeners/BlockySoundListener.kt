package com.mineinabyss.blocky.listeners

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.blocky.components.BlockySound
import com.mineinabyss.blocky.components.PlayerIsMining
import com.mineinabyss.blocky.helpers.getGearyEntityFromBlock
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageAbortEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerMoveEvent

class BlockySoundListener : Listener {

    @EventHandler(ignoreCancelled = true)
    fun BlockDamageEvent.onStartBreaking() {
        if (block.blockSoundGroup.hitSound != Sound.BLOCK_WOOD_HIT) return
        if (instaBreak || player.gameMode == GameMode.CREATIVE) return

        val geary = block.getGearyEntityFromBlock()
        val sound = geary?.get<BlockySound>()?.hitSound ?: Sound.BLOCK_STONE_HIT
        player.toGeary().getOrSet { PlayerIsMining() }
        blockyPlugin.launch {
            do {
                block.world.playSound(block.location, sound, 1.0f, 0.8f)
                delay(3.ticks)
            } while (player.toGeary().has<PlayerIsMining>())
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockDamageAbortEvent.onStopBreaking() {
        if (player.toGeary().has<PlayerIsMining>()) player.toGeary().remove<PlayerIsMining>()
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerMoveEvent.onStep() {
        val block = player.location.block.getRelative(BlockFace.DOWN)
        if (block.blockSoundGroup.stepSound != Sound.BLOCK_WOOD_STEP) return
        if (to.blockX == from.blockX && to.blockZ == from.blockZ) return
        if (player.isSneaking || player.isJumping) return

        val geary = block.getGearyEntityFromBlock()
        val sound = geary?.get<BlockySound>()?.stepSound ?: Sound.BLOCK_STONE_STEP
        blockyPlugin.launch {
            if (!player.isSprinting) delay(3.ticks) // Walking seems to have some delay
            if (player.location.block.getRelative(BlockFace.DOWN).type == block.type)
                block.world.playSound(block.location, sound, 1.0f, 0.8f)
            delay(9.ticks)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockBreakEvent.onBreak() {
        if (block.blockSoundGroup.breakSound != Sound.BLOCK_WOOD_BREAK) return
        if (player.toGeary().has<PlayerIsMining>()) player.toGeary().remove<PlayerIsMining>()

        val geary = block.getGearyEntityFromBlock()
        val sound = geary?.get<BlockySound>()?.breakSound ?: Sound.BLOCK_STONE_BREAK
        block.world.playSound(block.location, sound, 1.0f, 0.8f)
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockPlaceEvent.onPlace() {
        if (block.blockSoundGroup.placeSound != Sound.BLOCK_WOOD_PLACE) return
        val geary = block.getGearyEntityFromBlock()
        val sound = geary?.get<BlockySound>()?.placeSound ?: Sound.BLOCK_STONE_PLACE
        block.world.playSound(block.location, sound, 1.0f, 0.8f)
    }
}
