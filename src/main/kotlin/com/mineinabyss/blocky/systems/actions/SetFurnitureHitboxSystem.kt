package com.mineinabyss.blocky.systems.actions

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.helpers.FurniturePacketHelpers
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.systems.query.query
import kotlinx.coroutines.delay
import org.bukkit.entity.ItemDisplay

fun Geary.furnitureHitboxSetter() = observe<OnSet>()
    .involving(query<ItemDisplay, BlockyFurniture>())
    .exec { (itemDisplay, _) ->
        blocky.plugin.launch(blocky.plugin.minecraftDispatcher) {
            delay(1)
            FurniturePacketHelpers.sendInteractionHitboxPackets(itemDisplay)
            FurniturePacketHelpers.sendCollisionHitboxPacket(itemDisplay)
            FurniturePacketHelpers.sendLightPacket(itemDisplay)
        }
    }
