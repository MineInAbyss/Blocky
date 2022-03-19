package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.components.BlockyDebug
import com.mineinabyss.blocky.components.BlockyEntity
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.geary.ecs.api.entities.with
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.guiy.inventory.GuiyInventoryHolder
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractAtEntityEvent

class PlayerListener : Listener {

    @EventHandler
    fun PlayerInteractAtEntityEvent.debugBlocky() {
        val item = player.inventory.itemInMainHand
        item.toGearyOrNull(player)?.get<BlockyDebug>() ?: return

        rightClicked.toGearyOrNull()?.with { entity: BlockyEntity, info: BlockyInfo ->
            if (!info.canBeDebugged) return
            rightClicked.setRotation(rightClicked.location.yaw + 180, rightClicked.location.pitch)
        } ?: return
    }

    @EventHandler
    fun InventoryClickEvent.onBlockyMenuInteract() {
        if (inventory.holder !is GuiyInventoryHolder) return
        if (clickedInventory?.type != InventoryType.PLAYER) return

        if (click.isLeftClick) {
            if (currentItem == null) {
                currentItem = cursor
                view.cursor = null
            }
            else if (view.cursor?.type == Material.AIR) {
                view.cursor = currentItem
                currentItem = null
            }
            else if (currentItem?.itemMeta == cursor?.itemMeta) {

                if (currentItem!!.amount + cursor?.amount!! <= currentItem!!.maxStackSize) {
                    currentItem?.amount = currentItem?.amount?.plus(cursor?.amount!!)!!
                    view.cursor = null
                }
            }
        }

        if (click.isRightClick) {
            if (currentItem == null) {
                currentItem = view.cursor
                currentItem?.amount = 1
                view.cursor?.subtract(1)
            }
            else if (view.cursor?.type == Material.AIR) {

                if (currentItem!!.amount == 1) {
                    view.cursor = currentItem
                    currentItem = null
                }
                else {
                    val clone = currentItem?.clone()
                    currentItem?.apply { amount /= 2 } // Takes the bigger half on odd-numbers like vanilla
                    view.cursor = clone?.apply { amount -= currentItem?.amount!! }
                }
            }
            else if (currentItem?.itemMeta == cursor?.itemMeta) {
                if (currentItem!!.amount + cursor?.amount!! <= currentItem!!.maxStackSize) {
                    currentItem?.amount = currentItem?.amount?.plus(1)!!
                    view.cursor?.subtract(1)
                }
            }
        }
    }
}