package com.mineinabyss.blocky

import com.mineinabyss.blocky.BlockyTypeQuery.key
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.commands.CommandHolder
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.looty.LootyFactory
import com.mineinabyss.looty.ecs.components.itemcontexts.PlayerInventoryContext
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

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
                    LootyFactory.loadFromPlayerInventory(
                        context = PlayerInventoryContext(player, slot)
                    )
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
        return when (args.size) {
            1 -> listOf(
                "give"
            ).filter { it.startsWith(args[0]) }
            2 -> {
                when (args[0]) {
                    "give" -> BlockyTypeQuery.map { it.key.toString() }
                    else -> listOf()
                }
            }
            else -> listOf()
        }
    }
}
