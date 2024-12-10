package com.mineinabyss.blocky

import com.mineinabyss.geary.addons.dsl.createAddon
import com.mineinabyss.geary.autoscan.autoscan

val BlockyAddon = createAddon("Blocky", configuration = {
    autoscan(BlockyPlugin::class.java.classLoader, "com.mineinabyss.blocky") {
        components()
    }
})
