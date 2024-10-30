package com.mineinabyss.blocky

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.blocky.menus.BlockyMainMenu
import com.mineinabyss.blocky.systems.allBlockyPrefabs
import com.mineinabyss.geary.papermc.features.items.resourcepacks.ResourcePackAddon
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.Prefabs
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.brigadier.commands
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.util.to
import com.mojang.brigadier.arguments.IntegerArgumentType
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import org.bukkit.entity.Player

object BlockyBrigadierCommands {

    fun registerCommands() {
        blocky.plugin.commands {
            "blocky" {
                "reload" {
                    executes {
                        val geary = gearyPaper.worldManager.global
                        val prefabs = geary.getAddon(Prefabs)
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
                        geary.getAddon(ResourcePackAddon).generateResourcePack()
                        sender.success("Blocky has been reloaded!")
                    }
                }
                "give" {
                    val item by ArgumentTypes.key().suggests {
                        suggest(allBlockyPrefabs.distinctBy { it.prefabKey.full }.map { it.prefabKey.full })
                    }
                    val amount by IntegerArgumentType.integer(1)
                    playerExecutes {
                        val gearyItems = location.world.toGeary().getAddon(ItemTracking)
                        val (player, item, amount) = (executor as? Player
                            ?: return@playerExecutes) to item()!! to amount()!!
                        if (player.inventory.firstEmpty() == -1) return@playerExecutes player.error("No empty slots in inventory")

                        val itemstack = gearyItems.createItem(PrefabKey.of(item.asString()))?.asQuantity(amount)
                            ?: return@playerExecutes player.error("$item exists but is not a block.")
                        player.inventory.addItem(itemstack)
                    }
                }
                "menu" {
                    playerExecutes {
                        guiy { BlockyMainMenu(player) }
                    }
                }
            }
        }
    }
}
