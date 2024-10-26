package com.mineinabyss.blocky

import com.mineinabyss.blocky.systems.actions.createFurnitureItemSetter
import com.mineinabyss.blocky.systems.actions.createFurnitureMEGModelSetter
import com.mineinabyss.blocky.systems.actions.createFurnitureSeatSetter
import com.mineinabyss.blocky.systems.actions.furnitureHitboxSetter
import com.mineinabyss.blocky.systems.createFurnitureOutlineSystem
import com.mineinabyss.blocky.systems.createFurnitureSpawner
import com.mineinabyss.geary.addons.dsl.createAddon
import com.mineinabyss.geary.autoscan.autoscan

val BlockyAddon = createAddon("Blocky", configuration = {
    autoscan(BlockyPlugin::class.java.classLoader, "com.mineinabyss.blocky") {
        components()
    }
}) {
    systems {
        createFurnitureOutlineSystem()
        createFurnitureSpawner()
        createFurnitureItemSetter()
        createFurnitureSeatSetter()
        createFurnitureMEGModelSetter()
        furnitureHitboxSetter()
    }
}
