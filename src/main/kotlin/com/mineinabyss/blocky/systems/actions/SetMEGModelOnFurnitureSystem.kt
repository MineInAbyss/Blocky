package com.mineinabyss.blocky.systems.actions

import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.features.furniture.BlockyModelEngine
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.systems.builders.observeWithData
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.plugin.Plugins
import com.mineinabyss.idofront.typealiases.BukkitEntity
import com.ticxo.modelengine.api.ModelEngineAPI
import org.bukkit.Bukkit
import org.bukkit.entity.ItemDisplay

fun GearyModule.createFurnitureMEGModelSetter() = observeWithData<OnSet>()
    .involving(query<BukkitEntity, BlockyFurniture, BlockyModelEngine>())
    .exec { (entity, _, modelengine) ->
        // Save for scheduled task
        if (!Plugins.isEnabled("ModelEngine")) return@exec
        val activeModel = ModelEngineAPI.createActiveModel(modelengine.modelId)
        Bukkit.getScheduler().scheduleSyncDelayedTask(blocky.plugin, {
            ModelEngineAPI.getOrCreateModeledEntity(entity).apply {
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
