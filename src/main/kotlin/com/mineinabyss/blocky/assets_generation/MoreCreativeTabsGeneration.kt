@file:OptIn(UnsafeAccessors::class)

package com.mineinabyss.blocky.assets_generation

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.systems.BlockyPrefabs
import com.mineinabyss.blocky.systems.blockPrefabs
import com.mineinabyss.blocky.systems.furniturePrefabs
import com.mineinabyss.blocky.systems.plantPrefabs
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.prefabs.PrefabKey
import okio.Path.Companion.toPath
import org.bukkit.inventory.ItemStack
import java.io.File
import java.nio.charset.Charset

class MoreCreativeTabsGeneration {

    fun generateModAssets() {
        if (!blocky.config.MoreCreativeTabsMod.generateJsonForMod) return
        val root = "${blocky.plugin.dataFolder.absolutePath}/MoreCreativeTabsMod/assets/minecraft/morecreativetabs"
            .run { toPath().toFile().mkdirs(); this }
        val langFile = "${root.toPath().parent.toString()}/lang/en_us.json".toPath().toFile()
            .run { this.parentFile.mkdirs(); this.createNewFile(); this }
        val blockyBlockTabFile = "${root}/blocky_block_tab.json".toPath().toFile()
        val blockyWireTabFile = "${root}/blocky_wire_tab.json".toPath().toFile()
        val blockyFurnitureTabFile = "${root}/blocky_furniture_tab.json".toPath().toFile()

        blockyBlockTabFile.writeJson(getBlockyTabFile("blocky_block_tab", blockPrefabs))
        blockyWireTabFile.writeJson(getBlockyTabFile("blocky_wire_tab", plantPrefabs))
        blockyFurnitureTabFile.writeJson(
            getBlockyTabFile("blocky_furniture_tab", furniturePrefabs.filter { !it.isModelEngine }, furniturePrefabs)
        )
        langFile.writeJson(JsonObject().apply {
            addProperty("itemGroup.morecreativetabs.blocky_block_tab", "Blocky Blocks")
            addProperty("itemGroup.morecreativetabs.blocky_wire_tab", "Blocky Plants")
            addProperty("itemGroup.morecreativetabs.blocky_furniture_tab", "Blocky Furniture")
        })
    }

    private fun File.writeJson(content: JsonObject) {
        if (!this.exists()) this.createNewFile()
        if (content.keySet().isEmpty()) this.delete()
        else this.writeText(content.toString(), Charset.defaultCharset())
    }

    private fun getBlockyTabFile(
        tabName: String,
        query: List<BlockyPrefabs>,
        secondQuery: List<BlockyPrefabs>? = null
    ): JsonObject {
        return JsonObject().apply {
            val tabStack = JsonObject().apply tabStack@{
                val firstBlock = query.minByOrNull {
                    when {
                        it is BlockyPrefabs.Furniture && it.isModelEngine -> it.prefabKey.lootyItem?.customModelData
                        it is BlockyPrefabs.Block -> it.block.blockId
                        it is BlockyPrefabs.Plant -> it.block.blockId
                        else -> 1
                    } ?: 1
                }?.prefabKey?.lootyItem ?: return@apply
                addProperty("name", firstBlock.type.name.lowercase())
                addProperty("nbt", firstBlock.itemMeta.asString)
            }

            val tabItems = JsonArray().apply {
                (secondQuery ?: query).forEach {
                    it.prefabKey.lootyItem?.let { item ->
                        add(JsonObject().apply {
                            addProperty("name", item.type.name.lowercase())
                            addProperty("nbt", item.itemMeta.asString)
                        })
                    }
                }
            }

            addProperty("tab_enabled", true)
            addProperty("tab_name", tabName)
            add("tab_stack", tabStack)
            add("tab_items", tabItems)
        }
    }

    private val PrefabKey.lootyItem get() = gearyItems.createItem(this)
    private val ItemStack.customModelData get() = if (this.itemMeta.hasCustomModelData()) this.itemMeta.customModelData else 0

}
