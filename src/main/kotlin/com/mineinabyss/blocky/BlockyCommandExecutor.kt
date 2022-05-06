package com.mineinabyss.blocky

import com.mineinabyss.blocky.BlockyTypeQuery.key
import com.mineinabyss.blocky.menus.BlockyMainMenu
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.CommandHolder
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.looty.LootyFactory
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class BlockyCommandExecutor : IdofrontCommandExecutor(), TabCompleter {
    override val commands: CommandHolder = commands(blockyPlugin) {
        ("blocky")(desc = "Commands related to Blocky-plugin") {
            "give" {
                val type by optionArg(options = BlockyTypeQuery.map { it.key.toString() }) {
                    parseErrorMessage = { "No such item: $passed" }
                }
                playerAction {
                    val slot = player.inventory.firstEmpty()
                    if (slot == -1) {
                        player.error("No empty slots in inventory")
                        return@playerAction
                    }

                    player.inventory.setItem(slot, LootyFactory.createFromPrefab(PrefabKey.of(type)))
                }
            }
            "menu" {
                playerAction {
                    val player = sender as Player
                    guiy { BlockyMainMenu(player) }
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
        if (command.name == "blocky") {
            return when (args.size) {
                1 -> listOf("give", "menu")
                2 -> {
                    when (args[0]) {
                        "give" -> {
                            BlockyTypeQuery
                                .filter {
                                    val arg = args[1].lowercase()
                                    it.key.namespace.startsWith(arg) || it.key.key.startsWith(arg)
                                }
                                .map { it.key.toString() }
                        }
                        "menu" -> emptyList()
                        else -> emptyList()
                    }
                }
                else -> emptyList()
            }
        }
        else return emptyList()
    }
}
