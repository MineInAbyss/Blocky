package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.components.BlockType
import com.mineinabyss.blocky.components.BlockyDebug
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.blocky.components.BlockyType
import com.mineinabyss.geary.ecs.api.entities.with
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent

class PlayerListener : Listener {

    @EventHandler
    fun PlayerInteractAtEntityEvent.debugBlocky() {
        val item = player.inventory.itemInMainHand
        val debug = item.toGearyOrNull(player)?.get<BlockyDebug>() ?: return

        rightClicked.toGearyOrNull()?.with { blocky: BlockyType, info: BlockyInfo ->
            if (!info.canBeDebugged) return
            if (blocky.blockType == BlockType.NORMAL) {
                //TODO Consider replicating how stairs change their model depending on somehting?
            }
            else if (blocky.blockType == BlockType.MISC) {
                rightClicked.setRotation(rightClicked.location.yaw + 90F, rightClicked.location.pitch)
            }
            else return
        } ?: return
    }
}