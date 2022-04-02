package com.mineinabyss.blocky.systems

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.BlockPosition
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType
import com.mineinabyss.blocky.blockyPlugin
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.blocky.helpers.getPrefabFromBlock
import com.mineinabyss.blocky.listeners.protocolManager
import com.okkero.skedule.schedule
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.scheduler.BukkitTask
import java.lang.reflect.InvocationTargetException
import java.util.function.Consumer


class BlockyBreakingPacketAdapter(
    private val player: Player,
    private val breakerLocations: MutableMap<Location, BukkitScheduler> = mutableMapOf()
) : PacketAdapter(
    blockyPlugin,
    ListenerPriority.LOW,
    PacketType.Play.Client.BLOCK_DIG
) {
    override fun onPacketReceiving(e: PacketEvent) {
        val item = player.inventory.itemInMainHand
        if (e.player != player) return
        if (player.gameMode == GameMode.CREATIVE) return

        val tempData = e.packet.blockPositionModifier
        val data = e.packet.getEnumModifier(PlayerDigType::class.java, 2)

        val pos = tempData.values[0]
        val block = player.world.getBlockAt(pos.toLocation(player.world))
        if (block.type != Material.NOTE_BLOCK) return
        val period = block.getPrefabFromBlock()?.get<BlockyInfo>()!!.blockBreakTime.toLong()

        e.isCancelled = true

        if (data.values[0] == PlayerDigType.START_DESTROY_BLOCK) {
            Bukkit.getScheduler().runTask(blockyPlugin, Runnable {
                player.addPotionEffect(
                    PotionEffect(
                        PotionEffectType.SLOW_DIGGING, (period*20).toInt(),
                        Int.MAX_VALUE, false, false, true
                    )
                ) //TODO implement stuff to send correct packet damage progress
            })
            if (breakerLocations.containsKey(block.location)) breakerLocations[block.location]?.cancelTasks(blockyPlugin)
            val scheduler = Bukkit.getScheduler()
            breakerLocations[block.location] = scheduler

            scheduler.runTaskTimer(blockyPlugin, Consumer {
                var value = 0

                //TODO Doesnt work
                fun accept(bukkitTask: BukkitTask) {
                    if (!breakerLocations.containsKey(block.location)) {
                        bukkitTask.cancel()//
                        return
                    }

                    player.world.getNearbyPlayers(block.location, 16.0).forEach {
                        sendBlockBreak(it, block.location, value)
                    }

                    if (value++ < 10) return

                    val blockBreakEvent = BlockBreakEvent(block, player)
                    blockBreakEvent.callEvent()

                    if (!blockBreakEvent.isCancelled) {
                        block.type = Material.AIR
                        PlayerItemDamageEvent(player, item, 1).callEvent()
                    }
                    Bukkit.getScheduler().runTask(blockyPlugin, Runnable {
                        player.removePotionEffect(PotionEffectType.SLOW_DIGGING)
                    })
                    breakerLocations.remove(block.location)
                    player.world.getNearbyPlayers(block.location, 16.0).forEach {
                        sendBlockBreak(it, block.location, 10)
                    }
                    bukkitTask.cancel()
                }
            }, period, period)
        } else {
            blockyPlugin.schedule {
                player.removePotionEffect(PotionEffectType.SLOW_DIGGING)
                player.world.getNearbyPlayers(block.location, 16.0).forEach {
                    sendBlockBreak(it, block.location, 10)
                }
            }
            breakerLocations.remove(block.location)
            return
        }
    }

    private fun sendBlockBreak(player: Player, location: Location, stage: Int) {
        val fakeAnimation = protocolManager.createPacket(PacketType.Play.Server.BLOCK_BREAK_ANIMATION)
        fakeAnimation.integers.write(0, location.hashCode()).write(1, stage)
        fakeAnimation.blockPositionModifier.write(0, BlockPosition(location.toVector()))
        try {
            protocolManager.sendServerPacket(player, fakeAnimation)
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }
}
