package com.mineinabyss.blocky.listeners

import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.ints.IntList
import net.minecraft.core.Registry
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagNetworkSerialization
import net.minecraft.world.item.Item
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class BlockyNMSListener : Listener {

    @EventHandler
    fun PlayerJoinEvent.removeDefaultTool() {
        val tags = Registry.BLOCK.tags.map { pair ->
            pair.first.location to IntArrayList(pair.second.size()).apply {
                // If the tag is MINEABLE_WITH_AXE, don't add noteblock and chorus plant
                if (pair.first.location == BlockTags.MINEABLE_WITH_AXE.location) {
                    pair.second.filter {
                        Item.BY_BLOCK[it.value()].toString() != "note_block" &&
                                Item.BY_BLOCK[it.value()].toString() != "chorus_plant"
                    }.forEach { add(Registry.BLOCK.getId(it.value())) }
                } else pair.second.forEach { add(Registry.BLOCK.getId(it.value())) }
            }
        }.toList()

        val payload = createPayload(tags.toMap())
        val packet = ClientboundUpdateTagsPacket(mapOf(Registry.BLOCK_REGISTRY to payload))
        (player as CraftPlayer).handle.connection.send(packet)
    }
}

fun createPayload(map: Map<ResourceLocation, IntList>): TagNetworkSerialization.NetworkPayload {
    return TagNetworkSerialization.NetworkPayload::class.java.declaredConstructors.first()
        .also { it.isAccessible = true }
        .newInstance(map) as TagNetworkSerialization.NetworkPayload
}
