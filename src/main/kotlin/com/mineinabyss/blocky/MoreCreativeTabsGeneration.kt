package com.mineinabyss.blocky

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mineinabyss.blocky.api.BlockyFurnitures.isBlockyFurniture
import com.mineinabyss.blocky.api.BlockyFurnitures.isModelEngineFurniture
import com.mineinabyss.blocky.systems.BlockyBlockQuery.prefabKey
import com.mineinabyss.blocky.systems.BlockyBlockQuery.type
import com.mineinabyss.blocky.systems.blockyBlockQuery
import com.mineinabyss.blocky.systems.blockyFurnitureQuery
import com.mineinabyss.blocky.systems.blockyPlantQuery
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.looty.LootyFactory
import okio.Path.Companion.toPath
import org.bukkit.Instrument
import org.bukkit.inventory.ItemStack
import java.io.File
import java.nio.charset.Charset

class MoreCreativeTabsGeneration {

    fun generateModAssets() {
        if (!blockyConfig.MoreCreativeTabsMod.generateJsonForMod) return
        val root = "${blockyPlugin.dataFolder.absolutePath}/MoreCreativeTabsMod/assets/minecraft/morecreativetabs".run {
            toPath().toFile().mkdirs(); this
        }
        val langFile = "${root.toPath().parent.toString()}/lang/en_us.json".toPath().toFile().run { this.parentFile.mkdirs(); this.createNewFile(); this }
        val blockyBlockTabFile = "${root}/blocky_block_tab.json".toPath().toFile()
        val blockyWireTabFile = "${root}/blocky_wire_tab.json".toPath().toFile()
        val blockyFurnitureTabFile = "${root}/blocky_furniture_tab.json".toPath().toFile()

        blockyBlockTabFile.writeJson(getBlockyTabFile("blocky_block_tab", blockyBlockQuery))
        blockyWireTabFile.writeJson(getBlockyTabFile("blocky_wire_tab", blockyPlantQuery))
        blockyFurnitureTabFile.writeJson(getBlockyTabFile("blocky_furniture_tab", blockyFurnitureQuery.filter { !it.entity.isModelEngineFurniture }, blockyFurnitureQuery))
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

    private fun getBlockyTabFile(tabName: String, query: List<TargetScope>, secondQuery: List<TargetScope>? = null): JsonObject {
        return JsonObject().apply {
            val tabStack = JsonObject().apply tabStack@{
                val firstBlock = query.minByOrNull { (if (it.entity.isBlockyFurniture) it.prefabKey.lootyItem?.customModelData else it.type.blockId) ?: 1 }?.prefabKey?.lootyItem ?: return@apply
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

    private val PrefabKey.lootyItem get() = LootyFactory.createFromPrefab(this)
    private val ItemStack.customModelData get() = if (this.itemMeta.hasCustomModelData()) this.itemMeta.customModelData else 0

    private fun getInstrument(id: Instrument): String {
        when (id) {
            Instrument.BASS_DRUM -> return "basedrum"
            Instrument.STICKS -> return "hat"
            Instrument.SNARE_DRUM -> return "snare"
            Instrument.PIANO -> return "harp"
            Instrument.BASS_GUITAR -> return "bass"
            Instrument.FLUTE -> return "flute"
            Instrument.BELL -> return "bell"
            Instrument.GUITAR -> return "guitar"
            Instrument.CHIME -> return "chime"
            Instrument.XYLOPHONE -> return "xylophone"
            Instrument.IRON_XYLOPHONE -> return "iron_xylophone"
            Instrument.COW_BELL -> return "cow_bell"
            Instrument.DIDGERIDOO -> return "didgeridoo"
            Instrument.BIT -> return "bit"
            Instrument.BANJO -> return "banjo"
            Instrument.PLING -> return "pling"
            else -> return "hat"
        }
    }
}