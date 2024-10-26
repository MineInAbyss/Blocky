package com.mineinabyss.blocky.systems.actions

import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.features.furniture.BlockyModelEngine
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.plugin.Plugins
import com.ticxo.modelengine.api.ModelEngineAPI
import org.bukkit.Bukkit
import org.bukkit.entity.ItemDisplay

fun Geary.createFurnitureMEGModelSetter() = observe<OnSet>()
    .involving(query<ItemDisplay, BlockyFurniture, BlockyModelEngine>())
    .exec { (itemDisplay, _, modelengine) ->
        // Save for scheduled task
        if (!Plugins.isEnabled("ModelEngine")) return@exec
        val activeModel = ModelEngineAPI.createActiveModel(modelengine.modelId)
        Bukkit.getScheduler().scheduleSyncDelayedTask(blocky.plugin, {
            ModelEngineAPI.getOrCreateModeledEntity(itemDisplay).apply {
                models.forEach {
                    removeModel(it.key)
                    it.value.destroy()
                }
//                setSaved(false)
                addModel(activeModel, false)
                isBaseEntityVisible = false
                isModelRotationLocked = false
            }
        }, 2L)
    }
