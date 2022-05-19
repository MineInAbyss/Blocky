package com.mineinabyss.blocky.helpers.biome

import com.mineinabyss.blocky.biomeMap
import com.mineinabyss.blocky.blockyBiomeQuery
import com.mineinabyss.blocky.components.BlockyBiome
import com.mineinabyss.blocky.components.blockyBiome
import com.mineinabyss.blocky.components.hasBlockyBiome
import com.mojang.serialization.Lifecycle
import net.minecraft.core.*
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.dedicated.DedicatedServer
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.BiomeGenerationSettings
import net.minecraft.world.level.biome.BiomeSpecialEffects
import net.minecraft.world.level.biome.MobSpawnSettings
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_18_R2.CraftServer
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld
import java.lang.reflect.Field


fun addCustomBiome(biome: BlockyBiome) {
    val dedicatedServer = (Bukkit.getServer() as CraftServer).server
    val mcKey: ResourceKey<Biome> = ResourceKey.create(Registry.BIOME_REGISTRY, ResourceLocation("minecraft", biome.mcName))
    val customKey: ResourceKey<Biome> = ResourceKey.create(Registry.BIOME_REGISTRY, ResourceLocation("blocky", biome.customName))
    val regWrite: WritableRegistry<Biome> = dedicatedServer.registryAccess().ownedRegistry(Registry.BIOME_REGISTRY).get() as WritableRegistry<Biome>
    val mcBiome: Biome? = regWrite[mcKey]
    val biomeHolder: Holder<Biome> = mcBiome?.let { Holder.direct(it) } as Holder<Biome>
    val newBiome = Biome.BiomeBuilder()
    newBiome.biomeCategory(Biome.getBiomeCategory(biomeHolder))
    mcBiome.precipitation.let { newBiome.precipitation(it) }
    val biomeSettingMobs: MobSpawnSettings = mcBiome.mobSettings
    newBiome.mobSpawnSettings(biomeSettingMobs)
    val biomeSettingGen: BiomeGenerationSettings = mcBiome.generationSettings
    newBiome.generationSettings(biomeSettingGen)
    newBiome.temperature(biome.temperature)
    newBiome.downfall(biome.downfall)
    newBiome.temperatureAdjustment(if (biome.isFrozen) Biome.TemperatureModifier.NONE else Biome.TemperatureModifier.FROZEN)
    val newFog = BiomeSpecialEffects.Builder()
    newFog.grassColorModifier(BiomeSpecialEffects.GrassColorModifier.NONE)
    newFog.fogColor(biome.fogColour)
    newFog.waterColor(biome.waterColour)
    newFog.waterFogColor(biome.waterFogColour)
    newFog.skyColor(biome.skyColour)
    newFog.foliageColorOverride(biome.foliageColour)
    newFog.grassColorOverride(biome.grassColour)
    newBiome.specialEffects(newFog.build())
    val b: Biome = newBiome.build()
    biomeMap[biome.customName] = b
    changeRegistryLock(dedicatedServer, false)
    regWrite.register(customKey, b, Lifecycle.stable())
    changeRegistryLock(dedicatedServer, true)
}

fun setCustomBiome(newBiomeName: String, loc: Location): Boolean {
    var base: Biome?
    val regWrite = (Bukkit.getServer() as CraftServer).server.registryAccess().ownedRegistry(Registry.BIOME_REGISTRY).get() as WritableRegistry<Biome>
    val key = ResourceKey.create(Registry.BIOME_REGISTRY, ResourceLocation(newBiomeName.lowercase()))
    base = regWrite[key]
    if (base == null) {
        if (newBiomeName.contains(":")) {
            val newKey = ResourceKey.create(Registry.BIOME_REGISTRY, ResourceLocation(
                newBiomeName.split(":")[0].lowercase(),
                newBiomeName.split(":")[1].lowercase()))
            base = regWrite[newKey] ?: return false
        } else return false
    }
    setCustomBiomeByBlock(
        loc.blockX,
        loc.blockY,
        loc.blockZ,
        (loc.world as CraftWorld).handle,
        Holder.direct(base)
    )
    loc.world.refreshChunk(loc.chunk.x, loc.chunk.z)
    return true
}

private fun setCustomBiomeByBlock(x: Int, y: Int, z: Int, world: Level, bb: Holder<Biome>) {
    val pos = BlockPos(x, 0, z)
    if (world.isLoaded(pos)) world.getChunk(pos).setBiome(x shr 2, y shr 2, z shr 2, bb)
}

private fun changeRegistryLock(dedicatedServer: DedicatedServer, isLocked: Boolean) {
    val materials: MappedRegistry<Biome> =
        dedicatedServer.registryAccess().ownedRegistry(Registry.BIOME_REGISTRY).get() as MappedRegistry<Biome>
    try {
        val isFrozen: Field = materials.javaClass.getDeclaredField("bL")
        isFrozen.isAccessible = true
        isFrozen.set(materials, isLocked)
    } catch (ignored: IllegalAccessException) {
    } catch (ignored: NoSuchFieldException) {
    }
}

fun createBiomeMap() {
    blockyBiomeQuery.forEach { prefab ->
        prefab.toEntity() ?: return@forEach
        if (!prefab.toEntity()!!.hasBlockyBiome) return@forEach
        prefab.toEntity()!!.blockyBiome?.let { addCustomBiome(it) }
    }
}
