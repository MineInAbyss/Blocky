package com.mineinabyss.blocky

import com.mineinabyss.idofront.nms.interceptClientbound
import com.mineinabyss.idofront.nms.networkPayload
import com.mineinabyss.idofront.nms.tags
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import org.bukkit.entity.Player

object BlockyTags {
    /**
     * Intercepts ClientboundUpdateTagsPacket sent to players during Configuration Phase, then removes NOTE_BLOCK's from MINEABLE/AXE tag
     */
    fun interceptConfigPhaseTagPacket() {
        blocky.plugin.interceptClientbound { packet: Packet<*>, player: Player? ->
            if (packet !is ClientboundUpdateTagsPacket || player?.isOnline == true) return@interceptClientbound packet

            packet.tags.entries.find { it.key == Registries.BLOCK }?.let { registryEntry ->
                val noteBlock = BuiltInRegistries.BLOCK.getId(BuiltInRegistries.BLOCK.get(ResourceKey.create(Registries.BLOCK, ResourceLocation.parse("minecraft:note_block"))))
                val tags = registryEntry.value.tags().map { tag ->
                    tag.key to tag.value.also { it.remove(noteBlock) }
                }.toMap()
                registryEntry.setValue(tags.networkPayload())
            }

            return@interceptClientbound packet
        }
    }
}