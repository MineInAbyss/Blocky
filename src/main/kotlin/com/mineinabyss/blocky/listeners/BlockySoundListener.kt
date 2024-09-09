package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.api.events.furniture.BlockyFurnitureBreakEvent
import com.mineinabyss.blocky.api.events.furniture.BlockyFurniturePlaceEvent
import com.mineinabyss.blocky.components.features.BlockySound
import com.mineinabyss.blocky.helpers.*
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import io.papermc.paper.event.block.BlockBreakProgressUpdateEvent
import org.bukkit.*
import org.bukkit.block.data.Openable
import org.bukkit.block.data.type.Door
import org.bukkit.block.data.type.TrapDoor
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.GenericGameEvent

class BlockySoundListener : Listener {

    @EventHandler
    fun BlockPlaceEvent.onPlace() {
        val soundGroup = Registry.SOUNDS.get(block.blockSoundGroup.placeSound.key())?.takeUnless { it.isNotCustomBlockGroup() } ?: return
        val sound = block.toGearyOrNull()?.get<BlockySound>()?.placeSound ?: ("blocky:$soundGroup")
        block.world.playSound(block.location, sound, SoundCategory.BLOCKS, DEFAULT_HIT_VOLUME, DEFAULT_HIT_PITCH)
    }

    @EventHandler
    fun BlockBreakProgressUpdateEvent.onBreakProgress() {
        if ((entity as? Player)?.gameMode == GameMode.CREATIVE) return
        val soundGroup = Registry.SOUNDS.get(block.blockSoundGroup.hitSound.key())?.takeUnless { it.isNotCustomBlockGroup() } ?: return
        val sound = block.toGearyOrNull()?.get<BlockySound>()?.hitSound ?: ("blocky:$soundGroup")
        block.world.playSound(block.location, sound, SoundCategory.BLOCKS, DEFAULT_HIT_VOLUME, DEFAULT_HIT_PITCH)
    }

    @EventHandler
    fun BlockBreakEvent.onBreak() {
        val soundGroup = Registry.SOUNDS.get(block.blockSoundGroup.breakSound.key())?.takeUnless { it.isNotCustomBlockGroup() } ?: return
        val sound = block.toGearyOrNull()?.get<BlockySound>()?.breakSound ?: ("blocky:$soundGroup")
        block.world.playSound(block.location, sound, SoundCategory.BLOCKS, DEFAULT_HIT_VOLUME, DEFAULT_HIT_PITCH)
    }

    @EventHandler(ignoreCancelled = true)
    fun GenericGameEvent.onSound() {
        if (!location.isWorldLoaded || !location.world.isChunkLoaded(location.chunk)) return

        val (entity, standingOn) = (entity as? LivingEntity ?: return) to GenericHelpers.blockStandingOn(entity as LivingEntity)
        val soundGroup = Registry.SOUNDS.get(standingOn.blockSoundGroup.stepSound.key())?.takeUnless { it.isNotCustomBlockGroup() } ?: return
        if (event != GameEvent.STEP && event != GameEvent.HIT_GROUND) return
        if (event == GameEvent.HIT_GROUND && entity.lastDamageCause?.cause != EntityDamageEvent.DamageCause.FALL) return

        val blockySound = standingOn.toGearyOrNull()?.get<BlockySound>()
        val currentBlock = entity.location.block
        if (!currentBlock.isReplaceable || currentBlock.type == Material.TRIPWIRE) return

        val sound = when (event) {
            GameEvent.STEP -> blockySound?.stepSound
            GameEvent.HIT_GROUND -> blockySound?.fallSound
            else -> return
        } ?: ("blocky:$soundGroup")

        val (volume, pitch) = when (event) {
            GameEvent.STEP -> DEFAULT_STEP_VOLUME to DEFAULT_STEP_PITCH
            GameEvent.HIT_GROUND -> DEFAULT_FALL_VOLUME to DEFAULT_FALL_PITCH
            else -> return
        }

        standingOn.world.playSound(standingOn.location, sound, SoundCategory.PLAYERS, volume, pitch)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockyFurniturePlaceEvent.onPlaceBlockyFurniture() {
        val sound = entity.toGeary().get<BlockySound>()?.placeSound ?: entity.location.block.blockData.soundGroup.placeSound.key().asString()
        entity.world.playSound(entity.location, sound, SoundCategory.BLOCKS, DEFAULT_PLACE_VOLUME, DEFAULT_PLACE_PITCH)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockyFurnitureBreakEvent.onBreakBlockyFurniture() {
        val sound = entity.toGeary().get<BlockySound>()?.breakSound ?: entity.location.block.blockData.soundGroup.breakSound.key().asString()
        entity.world.playSound(entity.location, sound, SoundCategory.BLOCKS, DEFAULT_BREAK_VOLUME, DEFAULT_BREAK_PITCH)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun PlayerInteractEvent.onOpenCloseDoor() {
        if (action != Action.RIGHT_CLICK_BLOCK || useInteractedBlock() == Event.Result.DENY) return
        val block = clickedBlock?.takeIf(CopperHelpers::isBlockyDoor)?.takeIf(CopperHelpers::isBlockyTrapDoor) ?: return
        val opening = !(block.blockData as Openable).isOpen
        val suffix = when (block.blockData) {
            is Door -> "_door_"
            is TrapDoor -> "_trapdoor_"
            else -> return
        }.plus(if (opening) "open" else "close")
        val sound = block.toGearyOrNull()?.get<BlockySound>()?.let { if (opening) it.openSound else it.closeSound } ?: ("blocky:copper_$suffix")
        block.world.playSound(block.location, sound, SoundCategory.BLOCKS, DEFAULT_HIT_VOLUME, DEFAULT_HIT_PITCH)
    }

    private fun Sound.isNotCustomBlockGroup() = when (this) {
        Sound.BLOCK_WOOD_PLACE, Sound.BLOCK_WOOD_BREAK, Sound.BLOCK_WOOD_HIT, Sound.BLOCK_WOOD_FALL, Sound.BLOCK_WOOD_STEP -> true
        Sound.BLOCK_STONE_PLACE, Sound.BLOCK_STONE_BREAK, Sound.BLOCK_STONE_HIT, Sound.BLOCK_STONE_FALL, Sound.BLOCK_STONE_STEP -> true
        Sound.BLOCK_COPPER_PLACE, Sound.BLOCK_COPPER_BREAK, Sound.BLOCK_COPPER_HIT, Sound.BLOCK_COPPER_FALL, Sound.BLOCK_COPPER_STEP -> true
        Sound.BLOCK_COPPER_GRATE_PLACE, Sound.BLOCK_COPPER_GRATE_BREAK, Sound.BLOCK_COPPER_GRATE_HIT, Sound.BLOCK_COPPER_GRATE_FALL, Sound.BLOCK_COPPER_GRATE_STEP -> true
        else -> false
    }
}
