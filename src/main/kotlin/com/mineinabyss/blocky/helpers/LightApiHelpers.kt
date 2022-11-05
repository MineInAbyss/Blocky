package com.mineinabyss.blocky.helpers

import org.bukkit.Bukkit
import org.bukkit.Location
import ru.beykerykt.minecraft.lightapi.common.LightAPI

val handleLight = BlockLight()
class BlockLight {
    private val lightApiLoaded = Bukkit.getPluginManager().isPluginEnabled("LightAPI")


    fun createBlockLight(loc: Location, value: Int) {
        if (lightApiLoaded) LightAPI.get().setLightLevel(loc.world.name, loc.blockX, loc.blockY, loc.blockZ, value, 1)
    }

    fun removeBlockLight(loc: Location) {
        if (lightApiLoaded) LightAPI.get().setLightLevel(loc.world.name, loc.blockX, loc.blockY, loc.blockZ, 0, 1)
    }
}
