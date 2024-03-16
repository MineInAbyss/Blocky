package com.mineinabyss.blocky.systems.actions

import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.features.furniture.BlockyModelEngine
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import com.mineinabyss.idofront.plugin.Plugins
import com.ticxo.modelengine.api.ModelEngineAPI
import org.bukkit.Bukkit
import org.bukkit.entity.ItemDisplay

fun GearyModule.createFurnitureMEGModelSetter() = listener(
    object : ListenerQuery() {
        val display by get<ItemDisplay>()
        val furniture by get<BlockyFurniture>()
        val meg by source.get<BlockyModelEngine>()
    }
).exec {
    if (!Plugins.isEnabled("ModelEngine")) return@exec
    val activeModel = ModelEngineAPI.createActiveModel(meg.modelId)
    Bukkit.getScheduler().scheduleSyncDelayedTask(blocky.plugin, {
        ModelEngineAPI.getOrCreateModeledEntity(display).apply {
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
