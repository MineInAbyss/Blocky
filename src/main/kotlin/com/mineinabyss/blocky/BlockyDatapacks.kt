package com.mineinabyss.blocky

import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.idofront.messaging.logVal
import io.papermc.paper.datapack.Datapack
import io.papermc.paper.datapack.DatapackManager
import io.papermc.paper.datapack.PaperDatapack
import kotlinx.serialization.json.*
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.NoteBlock
import org.bukkit.Bukkit
import org.bukkit.Tag

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