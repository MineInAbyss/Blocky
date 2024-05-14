package com.mineinabyss.blocky.systems.actions

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.helpers.FurniturePacketHelpers
import com.mineinabyss.blocky.helpers.GenericHelpers
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.query
import kotlinx.coroutines.delay
import org.bukkit.entity.ItemDisplay

fun GearyModule.furnitureHitboxSetter() = observe<OnSet>()
    .involving(query<ItemDisplay, BlockyFurniture>())
    .exec { (itemDisplay, _) ->
        val itemDisplay = itemDisplay

        blocky.plugin.launch(blocky.plugin.minecraftDispatcher) {
            delay(1)
            blocky.plugin.server.onlinePlayers.filterNotNull().filter {
                it.world == itemDisplay.world && it.location.distanceSquared(itemDisplay.location) < GenericHelpers.simulationDistance
            }.forEach { player ->
                FurniturePacketHelpers.sendInteractionEntityPacket(itemDisplay, player)
                FurniturePacketHelpers.sendCollisionHitboxPacket(itemDisplay, player)
                FurniturePacketHelpers.sendLightPacket(itemDisplay, player)
            }
        }
    }