package com.mineinabyss.blocky.systems

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.BlockPosition
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType
import com.mineinabyss.blocky.blockyPlugin
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.scheduler.BukkitTask
import java.lang.reflect.InvocationTargetException

class BlockBreakingSystem {

    val MODIFIERS: MutableList<BlockHardnessModifiers> = mutableListOf()
    val breakerPerLocation = hashMapOf<Location, BukkitScheduler>()
    var protocolManager = ProtocolLibrary.getProtocolManager()
    private val listener: PacketAdapter = object : PacketAdapter(
        blockyPlugin, ListenerPriority.LOW, PacketType.Play.Client.BLOCK_DIG
    ) {
        override fun onPacketReceiving(e: PacketEvent?) {
            val packet = e?.packet ?: return
            val player = e.player
            val item = player.inventory.itemInMainHand

            if (player.gameMode == GameMode.CREATIVE) return

            val tempData = packet.blockPositionModifier
            val data = packet.getEnumModifier(PlayerDigType::class.java, 2)
            val type: PlayerDigType? = try {
                data.values[0]
            } catch (exception: IllegalArgumentException) {
                PlayerDigType.SWAP_HELD_ITEMS
            }

            val pos = tempData.values[0]
            val block = player.world.getBlockAt(pos.toLocation(player.world))
            val loc = block.location

            var modifiers: BlockHardnessModifiers? = null
            for (modifier in MODIFIERS) {
                if (modifier.isTriggered(player, block, item)) {
                    modifiers = modifier
                    break
                }
            }
            if (modifiers == null) return
            e.isCancelled = true


            if (type == PlayerDigType.START_DESTROY_BLOCK) {
                val period = modifiers.getBreakTime(player, block, item)
                Bukkit.getScheduler().runTask(blockyPlugin, Runnable {
                    player.addPotionEffect(
                        PotionEffect(
                            PotionEffectType.SLOW_DIGGING, period.toInt() * 11,
                            Integer.MAX_VALUE,
                            false,
                            false,
                            false
                        )
                    )
                })

                if (breakerPerLocation.containsKey(loc)) breakerPerLocation[loc]?.cancelTasks(blockyPlugin)
                val scheduler = Bukkit.getScheduler()
                breakerPerLocation[loc] = scheduler

                val moreModifiers: BlockHardnessModifiers = modifiers
                scheduler.runTaskTimer(blockyPlugin, Runnable {
                    var value = 0

                    @Override
                    fun accept(task: BukkitTask) {
                        if (!breakerPerLocation.containsKey(loc)) {
                            task.cancel()
                            return
                        }

                        for (p in loc.world.getNearbyPlayers(loc, 16.0))
                            sendBlockBreak(p, loc, value)
                        if (value++ < 10) return

                        val blockBreakEvent = BlockBreakEvent(block, player)
                        Bukkit.getPluginManager().callEvent(blockBreakEvent)

                        if (!blockBreakEvent.isCancelled) {
                            moreModifiers.breakBlocky(player, block, item)
                            val playerItemDamageEvent = PlayerItemDamageEvent(player, item, 1)
                            Bukkit.getPluginManager().callEvent(playerItemDamageEvent)
                        }
                        Bukkit.getScheduler().runTask(blockyPlugin, Runnable {
                            player.removePotionEffect(PotionEffectType.SLOW_DIGGING)
                        })
                        breakerPerLocation.remove(loc)
                        for (p in loc.world.getNearbyPlayers(loc, 16.0))
                            sendBlockBreak(p, loc, 10)
                        task.cancel()
                    }
                }, period, period)
            }
            else {
                Bukkit.getScheduler().runTask(blockyPlugin, Runnable {
                    player.removePotionEffect(PotionEffectType.SLOW_DIGGING)
                    for (p in loc.world.getNearbyPlayers(loc, 16.0)) sendBlockBreak(p, loc, 10)
                })
                breakerPerLocation.remove(loc)
            }
        }
    }

    fun BreakerSystem() {
        protocolManager = ProtocolLibrary.getProtocolManager()
    }

    fun sendBlockBreak(player: Player, location: Location, progress: Int) {
        val fakeAnimation = protocolManager.createPacket(PacketType.Play.Server.BLOCK_BREAK_ANIMATION)
        fakeAnimation.integers.write(0, location.hashCode()).write(1, progress)
        fakeAnimation.blockPositionModifier.write(0, BlockPosition(location.toVector()))
        try {
            protocolManager.sendServerPacket(player, fakeAnimation)
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }

    fun registerListener() {
        protocolManager.addPacketListener(listener)
    }
}
