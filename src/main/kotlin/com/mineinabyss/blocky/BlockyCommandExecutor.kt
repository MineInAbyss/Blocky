package com.mineinabyss.blocky

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.blocky.assets_generation.ResourcepackGeneration
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.features.blocks.BlockyDirectional
import com.mineinabyss.blocky.helpers.gearyInventory
import com.mineinabyss.blocky.menus.BlockyMainMenu
import com.mineinabyss.blocky.systems.blockPrefabs
import com.mineinabyss.blocky.systems.megFurniturePrefabs
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.items.asColorable
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.util.toColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot

class BlockyCommandExecutor : IdofrontCommandExecutor(), TabCompleter {
    override val commands = commands(blocky.plugin) {
        ("blocky")(desc = "Commands related to Blocky-plugin") {
            "reload" {
                action {
                    blocky.plugin.createBlockyContext()
                    blocky.plugin.launch {
                        val blockyPrefabs = blocky.prefabQuery.entities()
                        val inheritedPrefabs = blockyPrefabs.asSequence().flatMap { it.prefabs }
                            .filter { it !in blockyPrefabs }.toSet().sortedBy { it.prefabs.size }

                        // Reload all prefabs that arent blockyPrefabs
                        inheritedPrefabs.forEach { prefabs.loader.reload(it) }

                        // Reload all blockyPrefabs that aren't in inheritedPrefabs
                        blockyPrefabs.filter { it !in inheritedPrefabs }.sortedBy { it.prefabs.size }
                            .forEach { prefabs.loader.reload(it) }
                    }
                    ResourcepackGeneration().generateDefaultAssets()
                    sender.success("Blocky has been reloaded!")
                }
            }
            "give" {
                val type by optionArg(options = blockPrefabs
                    .map { it.prefabKey.toString() }) {
                    parseErrorMessage = { "No such block: $passed" }
                }
                playerAction {
                    if (player.inventory.firstEmpty() == -1) {
                        player.error("No empty slots in inventory")
                        return@playerAction
                    }
                    val item = gearyItems.createItem(PrefabKey.of(type))
                    if (item == null) {
                        player.error("$type exists but is not a block.")
                        return@playerAction
                    }

                    player.inventory.addItem(item)
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
        return if (command.name == "blocky") {
            when (args.size) {
                1 -> listOf("reload", "give", "menu").filter { it.startsWith(args[0]) }
                2 -> when (args[0]) {
                    "give" -> blocky.prefabQuery.mapWithEntity { prefabKey }.asSequence()
                        .filter { it.data.key.startsWith(args[1]) || it.data.full.startsWith(args[1]) }
                        .filter { it.entity.get<BlockyDirectional>()?.isParentBlock != false }
                        .map { it.data.full }.take(20).toList()

                    else -> emptyList()
                }.filter { it.startsWith(args[1]) }

                3 -> when (args[0]) {
                    "modelengine" -> megFurniturePrefabs
                        .map { it.prefabKey.toString() }
                        .filter { it.startsWith(args[2]) }

                    else -> emptyList()
                }.filter { it.startsWith(args[2]) }

                else -> emptyList()
            }
        } else emptyList()
    }
}
