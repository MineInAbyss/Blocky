package com.mineinabyss.blocky

import com.mineinabyss.blocky.components.BlockyModelEngine
import com.mineinabyss.blocky.menus.BlockyMainMenu
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.CommandHolder
import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.looty.LootyFactory
import com.mineinabyss.looty.ecs.components.itemcontexts.PlayerInventorySlotContext
import com.mineinabyss.looty.ecs.components.itemcontexts.useWithLooty
import com.mineinabyss.looty.loadItem
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class BlockyCommandExecutor : IdofrontCommandExecutor(), TabCompleter {
    override val commands: CommandHolder = commands(blockyPlugin) {
        ("blocky")(desc = "Commands related to Blocky-plugin") {
            "give" {
                val type by optionArg(options = blockyQuery) {
                    parseErrorMessage = { "No such block: $passed" }
                }
                playerAction {
                    val slot = player.inventory.firstEmpty()
                    if (slot == -1) {
                        player.error("No empty slots in inventory")
                        return@playerAction
                    }
                    val item = LootyFactory.createFromPrefab(PrefabKey.of(type))
                    if (item == null) {
                        player.error("$type exists but is not a block.")
                        return@playerAction
                    }

                    item.useWithLooty {
                        PlayerInventorySlotContext(player, slot).loadItem(this)
                    }
                    player.inventory.setItem(slot, item)
                }
            }
            "menu" {
                playerAction {
                    val player = sender as Player
                    guiy { BlockyMainMenu(player) }
                }
            }
            "modelengine" {
                "remove" {
                    val radius by intArg { default = 10 }
                    val type by optionArg(options = blockyModelEngineQuery) {
                        parseErrorMessage = { "No such block: $passed" }
                    }
                    playerAction {
                        val player = sender as Player
                        val entities =
                            player.location.getNearbyEntities(radius.toDouble(), radius.toDouble(), radius.toDouble())
                                .filter { it.toGeary().has<BlockyModelEngine>() }
                        if (entities.isNotEmpty()) for (entity in entities) {
                            if (!entity.toGeary().instanceOf(PrefabKey.of(type).toEntity() ?: continue)) continue
                            entity.remove()
                            player.success("Removed ${type}'s in a radius of $radius")
                        } else player.error("No ${type}'s in a radius of $radius")
                    }
                }
            }
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return if (command.name == "blocky") {
            when (args.size) {
                1 -> listOf("give", "menu", "modelengine")
                2 -> {
                    when (args[0]) {
                        "give" ->
                            blockyQuery.filter {
                                it.startsWith(args[1]) || it.replace("mineinabyss:", "").startsWith(args[1])
                            }
                        "modelengine" -> listOf("remove")
                        "menu" -> emptyList()
                        else -> emptyList()
                    }
                }
                3 -> {
                    when (args[0]) {
                        "modelengine" -> blockyModelEngineQuery
                        else -> emptyList()
                    }
                }
                else -> emptyList()
            }
        } else emptyList()
    }
}
