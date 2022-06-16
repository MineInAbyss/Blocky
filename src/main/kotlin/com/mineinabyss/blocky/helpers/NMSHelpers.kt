package com.mineinabyss.blocky.helpers

import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.ints.IntList
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagNetworkSerialization
import net.minecraft.world.item.Item
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

fun createTagRegistryMap() : Map<ResourceLocation, IntArrayList> {
    val map = Registry.BLOCK.tags.map { pair ->
        pair.first.location to IntArrayList(pair.second.size()).apply {
            // If the tag is MINEABLE_WITH_AXE, don't add noteblock and chorus plant
            if (pair.first.location == BlockTags.MINEABLE_WITH_AXE.location) {
                pair.second.filter {
                    val itemName = Item.BY_BLOCK[it.value()].toString()
                    itemName != "note_block" && itemName != "chorus_plant"
                }.forEach { add(Registry.BLOCK.getId(it.value())) }
            }
            else pair.second.forEach { add(Registry.BLOCK.getId(it.value())) }
        }
    }.toList().toMap()

    return map
}

// Gets a list of all the materials present in the provided tags
fun Block.isInProvidedTags(list: List<String>) : Boolean {
    list.forEach {  tag ->
        if (Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft(tag), Material::class.java)?.isTagged(type) != true) return@forEach
        else return true
    }
    return false
}
