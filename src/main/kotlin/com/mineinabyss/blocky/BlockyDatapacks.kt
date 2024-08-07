package com.mineinabyss.blocky

import kotlinx.serialization.json.*
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.tags.BlockTags
import org.bukkit.Bukkit

object BlockyDatapacks {

    private val defaultWorld = Bukkit.getWorlds().first()
    private val blockyDatapack = defaultWorld.worldFolder.resolve("datapacks/blocky")

    fun generateDatapack() {
        blockyDatapack.resolve("data").mkdirs()
        writeMcMeta()
        generateMineableTag()

        Bukkit.getDatapackManager().packs.firstOrNull { it.name == "file/blocky" }?.isEnabled = true
    }

    fun writeMcMeta() {
        runCatching {
            val packMeta = blockyDatapack.resolve("pack.mcmeta")
            packMeta.writeText(buildJsonObject {
                putJsonObject("pack") {
                    put("description", "Datapack for Blocky")
                    put("pack_format", 26)
                }
            }.toString())
        }.onFailure { it.printStackTrace() }
    }

    private fun generateMineableTag() {
        runCatching {
            val tagFile = blockyDatapack.resolve("data/minecraft/tags/blocks/mineable/axe.json")
            tagFile.parentFile.mkdirs()
            tagFile.createNewFile()

            val tagObject = buildJsonObject {
                put("replace", true)
                putJsonArray("values") {
                    BuiltInRegistries.BLOCK.tags.toList().find { it.first == BlockTags.MINEABLE_WITH_AXE }?.second?.forEach {
                        if (it.registeredName != "minecraft:note_block") add(it.registeredName)
                    }
                }
            }

            tagFile.writeText(tagObject.toString())
        }.onFailure { it.printStackTrace() }
    }
}