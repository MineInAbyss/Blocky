package com.mineinabyss.blocky.components.core

import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.features.blocks.BlockyInstrument
import com.mineinabyss.blocky.helpers.GenericHelpers.toBlockCenterLocation
import com.mineinabyss.blocky.helpers.isVanillaNoteBlock
import com.mineinabyss.blocky.helpers.persistentDataContainer
import com.mineinabyss.blocky.helpers.vanillaNoteBlock
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.toGearyOrNull
import com.mineinabyss.idofront.location.up
import com.mineinabyss.idofront.messaging.broadcast
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import org.bukkit.Color
import org.bukkit.GameEvent
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.block.CraftBlockState
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import kotlin.jvm.optionals.getOrNull
import kotlin.math.pow

@Serializable
@SerialName("blocky:vanilla_note_block")
data class VanillaNoteBlock(private var note: Int = 0, private var powered: Boolean = false) {

    fun interact(block: Block, source: Player? = null, action: Action) {
        playSoundNaturally(block, source)
        if (action == Action.RIGHT_CLICK_BLOCK) note(block, (note + 1) % 25)
    }

    // Use method and private var to avoid issues with class changing but not pdc entry
    fun powered(): Boolean = powered
    fun powered(block: Block, state: Boolean) {
        if (powered == state || !block.isVanillaNoteBlock) return
        powered = state
        block.persistentDataContainer.encode(this)
    }

    fun note(): Int = note
    fun note(block: Block, note: Int) {
        if (this.note == note || !block.isVanillaNoteBlock) return
        this.note = note
        block.persistentDataContainer.encode(this)
    }

    private fun playSoundNaturally(block: Block, source: Player? = null) {
        val particleColor = note.toDouble() / 24.0
        val sound = block.instrumentSound()
        val pitch = 2f.pow((note - 12f).div(12f))
        val world = block.world
        val isSkullAbove = isSkullAbove(block)
        val loc = block.location.toBlockCenterLocation()

        if (!block.getRelative(BlockFace.UP).isEmpty && !isSkullAbove) return

        if (!isSkullAbove) {
            world.playSound(loc, sound, 1f, pitch)
            Particle.NOTE.builder().count(0).offset(particleColor, 0.0, 0.0).location(loc.up(1.2)).receivers(32).spawn()
        } else world.playSound(loc, sound, 1f, 1f)

        world.sendGameEvent(source, GameEvent.NOTE_BLOCK_PLAY, loc.toVector())
    }

    private fun isSkullAbove(block: Block) = (block.getRelative(BlockFace.UP).state as CraftBlockState).handle.instrument().worksAboveNoteBlock()

    private fun Block.instrumentSound(): String {
        val (stateAbove, stateBelow) = (getRelative(BlockFace.UP).state as CraftBlockState) to (getRelative(BlockFace.DOWN).state as CraftBlockState)
        val instrumentAbove = stateAbove.handle.instrument().takeIf { it.worksAboveNoteBlock() }
            ?.soundEvent?.unwrapKey()?.getOrNull()?.location()?.path
        val instrumentBelow = stateBelow.handle.instrument().takeUnless { it.worksAboveNoteBlock() }.let {
            stateBelow.block.toGearyOrNull()?.get<BlockyInstrument>()?.instrument?.asString()
                ?: it?.soundEvent?.unwrapKey()?.getOrNull()?.location()?.path
        }

        // Check the above block for heads, if none check block below for vanilla-sound or custom-block sound, otherwise default
        return instrumentAbove ?: instrumentBelow ?: NoteBlockInstrument.BASS.serializedName
    }
}
