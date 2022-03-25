package com.mineinabyss.blocky.systems

import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface BlockHardnessModifiers {
    fun isTriggered(player: Player, block: Block, tool: ItemStack) : Boolean
    fun breakBlocky(player: Player, block: Block, tool: ItemStack)
    fun getBreakTime(player: Player, block: Block, tool: ItemStack) : Long
}