package com.mineinabyss.blocky.systems

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.BlockPosition
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.blocky.components.PlayerIsMining
import com.mineinabyss.blocky.helpers.attemptBreakBlockyBlock
import com.mineinabyss.blocky.helpers.getGearyEntityFromBlock
import com.mineinabyss.blocky.protocolManager
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.idofront.events.call
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.idofront.time.inWholeTicks
import kotlinx.coroutines.delay
import org.bukkit.GameMode
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType.SLOW_DIGGING

class CustomBreakingSpeedSystem : PacketAdapter(
    blockyPlugin,
    PacketType.Play.Client.BLOCK_DIG
) {
    override fun onPacketReceiving(event: PacketEvent) {
        val player = event.player
        if (player.gameMode == GameMode.CREATIVE) return

        val type: PlayerDigType? = try {
            event.packet.getEnumModifier(PlayerDigType::class.java, 2).values[0]
        } catch (exception: IllegalArgumentException) {
            PlayerDigType.SWAP_HELD_ITEMS
        }

        val pos = event.packet.blockPositionModifier.values.firstOrNull() ?: return
        val block = player.world.getBlockAt(pos.x, pos.y, pos.z)
        val breakTime = block.getGearyEntityFromBlock()?.get<BlockyInfo>()?.blockBreakTime ?: return
        val mining = player.toGeary().getOrSetPersisting { PlayerIsMining() }

        if (type != PlayerDigType.START_DESTROY_BLOCK) return
        if (mining.miningTask != null) return
        event.isCancelled = true


        mining.miningTask = blockyPlugin.launch {
            var value = 0
            val effectTime = (breakTime.inWholeTicks * 1.1).toInt()
            player.addPotionEffect(PotionEffect(SLOW_DIGGING, effectTime, Int.MAX_VALUE, false, false, true))

            do { // Delay and repeat breaking progress until stage is 1.0
                block.location.getNearbyPlayers(16.0).forEach {
                    it.sendBlockDamage(block.location, value.toFloat() / 10)
                    //it.sendBlockBreak(block, value.broadcastVal())
                }
                delay(breakTime / 10)
            } while (value++ < 10 && player.toGeary().has<PlayerIsMining>())

            val breakEvent = BlockBreakEvent(block, player)
            breakEvent.call()
            broadcast(breakEvent.isCancelled)

            //TODO Add checks for worldguard and other plugins
            if (!breakEvent.isCancelled) attemptBreakBlockyBlock(block, player)
            player.removePotionEffect(SLOW_DIGGING)
            block.location.getNearbyPlayers(16.0).forEach {
                it.sendBlockDamage(block.location, 0f)
                //it.sendBlockBreak(block, 0)
            }
        }
    }
    private fun Player.sendBlockBreak(block: Block, stage: Int) {
        val breakAnimation = protocolManager.createPacket(PacketType.Play.Server.BLOCK_BREAK_ANIMATION)
        breakAnimation.integers.write(0, entityId)
        breakAnimation.blockPositionModifier.write(0, BlockPosition(block.location.toVector()))
        breakAnimation.integers.write(1, stage)

        protocolManager.sendServerPacket(player, breakAnimation)
    }
}
