package com.mineinabyss.blocky

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.features.BlockyDirectional
import com.mineinabyss.blocky.helpers.gearyInventory
import com.mineinabyss.blocky.menus.BlockyMainMenu
import com.mineinabyss.blocky.systems.BlockyBlockQuery.prefabKey
import com.mineinabyss.blocky.systems.BlockyQuery
import com.mineinabyss.blocky.systems.blockyModelEngineQuery
import com.mineinabyss.geary.papermc.tracking.items.itemTracking
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.actions
import org.bukkit.Color
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.inventory.meta.PotionMeta

class BlockyCommandExecutor : IdofrontCommandExecutor(), TabCompleter {
    override val commands = commands(blocky.plugin) {
        ("blocky")(desc = "Commands related to Blocky-plugin") {
            "reload" {
                fun reloadConfig() {
                    blocky.plugin.createBlockyContext()
                    blocky.plugin.runStartupFunctions()
                    sender.success("Blocky configs has been reloaded!")
                }
                fun reloadItems() {
                    blocky.plugin.launch {
                        BlockyQuery.forEach {
                            prefabs.loader.reread(it.entity)
                        }
                        sender.success("Blocky items have been reloaded!")
                    }
                }
                "all" {
                    actions {
                        reloadItems()
                        reloadConfig()
                    }
                }

                "config" {
                    actions {
                        reloadConfig()
                    }
                }
                "items" {
                    actions {
                        reloadItems()
                    }
                }
            }
            "give" {
                val type by optionArg(options = BlockyQuery.filter { it.entity.get<BlockyDirectional>()?.isParentBlock != false }
                    .map { it.prefabKey.toString() }) {
                    parseErrorMessage = { "No such block: $passed" }
                }
                playerAction {
                    if (player.inventory.firstEmpty() == -1) {
                        player.error("No empty slots in inventory")
                        return@playerAction
                    }
                    val item = itemTracking.provider.serializePrefabToItemStack(PrefabKey.of(type))
                    if (item == null) {
                        player.error("$type exists but is not a block.")
                        return@playerAction
                    }

                    player.inventory.addItem(item)
                }
            }
            "dye" {
                val color by stringArg()
                playerAction {
                    val player = sender as? Player ?: return@playerAction
                    val item = player.inventory.itemInMainHand
                    val furniture = player.gearyInventory?.get(EquipmentSlot.HAND)?.get<BlockyFurniture>()

                    if (furniture == null) {
                        player.error("This command only supports furniture.")
                        return@playerAction
                    }

                    item.editItemMeta {
                        (this as? LeatherArmorMeta)?.setColor(color.toColor)
                            ?: (this as? PotionMeta)?.setColor(color.toColor)
                            ?: (this as? MapMeta)?.setColor(color.toColor) ?: return@playerAction
                    }
                    player.success("Dyed item to <$color>$color")
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
                1 -> listOf("reload", "give", "dye", "menu").filter { it.startsWith(args[0]) }
                2 -> {
                    when (args[0]) {
                        "reload" -> listOf("all", "config", "items").filter { it.startsWith(args[1]) }
                        "give" ->
                            BlockyQuery.filter {
                                val arg = args[1].lowercase()
                                (it.prefabKey.key.startsWith(arg) || it.prefabKey.full.startsWith(arg)) &&
                                        it.entity.get<BlockyDirectional>()?.isParentBlock != false
                            }.map { it.prefabKey.toString() }

                        "menu" -> emptyList()
                        else -> emptyList()
                    }
                }

                3 -> {
                    when (args[0]) {
                        "modelengine" -> blockyModelEngineQuery.filter { it.startsWith(args[2]) }
                        else -> emptyList()
                    }
                }

                else -> emptyList()
            }
        } else emptyList()
    }

    private val String.toColor: Color
        get() {
            return when {
                this.startsWith("#") -> return Color.fromRGB(this.substring(1).toInt(16))
                this.startsWith("0x") -> return Color.fromRGB(this.substring(2).toInt(16))
                "," in this -> {
                    val colorString = this.replace(" ", "").split(",")
                    if (colorString.any { it.toIntOrNull() == null }) return Color.WHITE
                    try {
                        Color.fromRGB(
                            minOf(colorString[0].toInt(), 255),
                            minOf(colorString[1].toInt(), 255),
                            minOf(colorString[2].toInt(), 255)
                        )
                    } catch (e: NumberFormatException) {
                        Color.WHITE
                    }
                }
                //TODO Make this support text, probably through minimessage
                else -> return Color.WHITE
            }
        }
}
