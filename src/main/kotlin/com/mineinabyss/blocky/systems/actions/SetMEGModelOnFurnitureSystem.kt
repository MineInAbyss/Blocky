package com.mineinabyss.blocky.systems.actions

import com.mineinabyss.blocky.blocky
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.blocky.components.features.furniture.BlockyModelEngine
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.idofront.plugin.Plugins
import com.ticxo.modelengine.api.ModelEngineAPI
import org.bukkit.Bukkit
import org.bukkit.entity.ItemDisplay

class SetMEGModelOnFurnitureSystem : GearyListener() {
    private val Pointers.display by get<ItemDisplay>().on(target)
    private val Pointers.furniture by get<BlockyFurniture>().on(target)
    private val Pointers.meg by get<BlockyModelEngine>().on(source)

    override fun Pointers.handle() {
        if (!Plugins.isEnabled("ModelEngine")) return
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
}
