package com.mineinabyss.blocky.helpers

import it.unimi.dsi.fastutil.ints.IntList
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagNetworkSerialization
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.block.Block

fun createPayload(map: Map<ResourceLocation, IntList>): TagNetworkSerialization.NetworkPayload {
    return TagNetworkSerialization.NetworkPayload::class.java.declaredConstructors.first()
        .also { it.isAccessible = true }
        .newInstance(map) as TagNetworkSerialization.NetworkPayload
}

// Gets a list of all the materials present in the provided tags
fun Block.isInProvidedTags(list: List<String>) : Boolean {
    list.forEach {  tag ->
        if (Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft(tag), Material::class.java)?.isTagged(type) != true) return@forEach
        else return true
    }
    return false
}
