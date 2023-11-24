package com.mineinabyss.blocky

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.features.blocks.BlockyDirectional
import com.mineinabyss.blocky.helpers.gearyInventory
import com.mineinabyss.blocky.menus.BlockyMainMenu
import com.mineinabyss.blocky.systems.migration.VanillaNoteblockMigrator
import com.mineinabyss.blocky.systems.BlockyBlockQuery.prefabKey
import com.mineinabyss.blocky.systems.BlockyQuery
import com.mineinabyss.blocky.systems.blockyModelEngineQuery
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.arguments.genericArg
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.extensions.actions.ensureSenderIsPlayer
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.actions
import com.mineinabyss.idofront.util.toColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.inventory.meta.PotionMeta

@OptIn(UnsafeAccessors::class)
class BlockyCommandExecutor : IdofrontCommandExecutor(), TabCompleter {
    override val commands = commands(blocky.plugin) {
        ("blocky")(desc = "Commands related to Blocky-plugin") {
            "migratechunk" {
                playerAction {
                    VanillaNoteblockMigrator().migrate(player.chunk)
                }
            }
            "reload" {
                actions {
                    blocky.plugin.createBlockyContext()
                    blocky.plugin.runStartupFunctions()
                    blocky.plugin.launch {
                        BlockyQuery.forEach {
                            prefabs.loader.reread(it.entity)
                        }
                    }
                    sender.success("Blocky has been reloaded!")
                }
            }
            "give" {
                val prefabKey by genericArg {
                    BlockyQuery.toList { it }.filter { it.entity.get<BlockyDirectional>()?.isParentBlock != false }
                        .map { it.prefabKey }.first { prefab -> prefab.full == it }
                }
                playerAction {
                    if (player.inventory.firstEmpty() == -1) return@playerAction player.error("No empty slots in inventory")
                    val item = gearyItems.createItem(prefabKey) ?: return@playerAction player.error("$prefabKey exists but is not a block.")

                    player.inventory.addItem(item)
                }
            }
            "dye" {
                val color by genericArg { it.toColor() }
                ensureSenderIsPlayer()
                action {
                    val player = sender as Player
                    val item = player.inventory.itemInMainHand
                    val furniture = player.gearyInventory?.get(EquipmentSlot.HAND)?.get<BlockyFurniture>()

                    if (furniture == null) {
                        player.error("This command only supports furniture.")
                        return@action
                    }

                    item.editItemMeta {
                        when (this) {
                            is LeatherArmorMeta -> setColor(color)
                            is PotionMeta -> setColor(color)
                            is MapMeta -> setColor(color)
                            else -> return@action
                        }
                    }
                    player.success("Dyed item to <$color>$color")
                }
            }
            "menu" {
                playerAction {
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
                1 -> listOf("reload", "give", "dye", "menu", "migratechunk").filter { it.startsWith(args[0]) }
                2 -> {
                    when (args[0]) {
                        "give" -> BlockyQuery.toList { it }.filter {
                            val arg = args[1].lowercase()
                            (it.prefabKey.key.startsWith(arg) || it.prefabKey.full.startsWith(arg)) &&
                                    it.entity.get<BlockyDirectional>()?.isParentBlock != false
                        }.map { it.prefabKey.toString() }

                        "menu" -> emptyList()
                        else -> emptyList()
                    }.filter { it.startsWith(args[1]) }
                }

                3 -> {
                    when (args[0]) {
                        "modelengine" -> blockyModelEngineQuery
                        else -> emptyList()
                    }.filter { it.startsWith(args[2]) }
                }

                else -> emptyList()
            }
        } else emptyList()

    }
}
