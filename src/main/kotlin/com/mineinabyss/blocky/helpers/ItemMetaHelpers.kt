package com.mineinabyss.blocky.helpers

import org.bukkit.Color
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.inventory.meta.PotionMeta

interface RGBColorable {
    var color: Color?
}

/**
 * These different ItemMeta classes don't share a common color property so we use this :(
 */
fun ItemMeta.asRGBColorable(): RGBColorable? {
    return when (val meta = this) {
        is LeatherArmorMeta -> object : RGBColorable {
            override var color: Color?
                get() = meta.color
                set(value) {
                    meta.setColor(value)
                }
        }

        is PotionMeta -> object : RGBColorable {
            override var color: Color?
                get() = meta.color
                set(value) {
                    meta.color = value
                }
        }

        is MapMeta -> object : RGBColorable {
            override var color: Color?
                get() = meta.color
                set(value) {
                    meta.color = value
                }
        }

        else -> null
    }
}
