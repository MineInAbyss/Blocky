package com.mineinabyss.blocky.assets_generation

import com.mineinabyss.blocky.blocky
import net.kyori.adventure.key.Key
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.sound.SoundEntry
import team.unnamed.creative.sound.SoundEvent
import team.unnamed.creative.sound.SoundRegistry

object VanillaSoundEntries {

    fun registerRequiredSounds(resourcePack: ResourcePack) {
        if (blocky.config.disableCustomSounds) return

        val soundRegistry = resourcePack.soundRegistry("minecraft") ?: SoundRegistry.soundRegistry("minecraft", emptyList())

        SoundRegistry.soundRegistry(soundRegistry.namespace(), soundRegistry.sounds().plus(listOf(
            SoundEvent.soundEvent(Key.key("minecraft:block.stone.place"), true, null,  listOf()),
            SoundEvent.soundEvent(Key.key("minecraft:block.stone.break"), true, null,  listOf()),
            SoundEvent.soundEvent(Key.key("minecraft:block.stone.hit"), true, null,  listOf()),
            SoundEvent.soundEvent(Key.key("minecraft:block.stone.fall"), true, null,  listOf()),
            SoundEvent.soundEvent(Key.key("minecraft:block.stone.step"), true, null,  listOf()),

            SoundEvent.soundEvent(Key.key("minecraft:block.wood.place"), true, null,  listOf()),
            SoundEvent.soundEvent(Key.key("minecraft:block.wood.break"), true, null,  listOf()),
            SoundEvent.soundEvent(Key.key("minecraft:block.wood.hit"), true, null,  listOf()),
            SoundEvent.soundEvent(Key.key("minecraft:block.wood.fall"), true, null,  listOf()),
            SoundEvent.soundEvent(Key.key("minecraft:block.wood.step"), true, null,  listOf()),

            SoundEvent.soundEvent(Key.key("minecraft:block.copper.place"), true, null,  listOf()),
            SoundEvent.soundEvent(Key.key("minecraft:block.copper.break"), true, null,  listOf()),
            SoundEvent.soundEvent(Key.key("minecraft:block.copper.hit"), true, null,  listOf()),
            SoundEvent.soundEvent(Key.key("minecraft:block.copper.fall"), true, null,  listOf()),
            SoundEvent.soundEvent(Key.key("minecraft:block.copper.step"), true, null,  listOf()),

            SoundEvent.soundEvent(Key.key("minecraft:block.copper_grate.place"), true, null,  listOf()),
            SoundEvent.soundEvent(Key.key("minecraft:block.copper_grate.break"), true, null,  listOf()),
            SoundEvent.soundEvent(Key.key("minecraft:block.copper_grate.hit"), true, null,  listOf()),
            SoundEvent.soundEvent(Key.key("minecraft:block.copper_grate.fall"), true, null,  listOf()),
            SoundEvent.soundEvent(Key.key("minecraft:block.copper_grate.step"), true, null,  listOf()),

            SoundEvent.soundEvent(Key.key("minecraft:block.copper_door.open"), true, null,  listOf()),
            SoundEvent.soundEvent(Key.key("minecraft:block.copper_door.close"), true, null,  listOf()),
            SoundEvent.soundEvent(Key.key("minecraft:block.copper_trapdoor.open"), true, null,  listOf()),
            SoundEvent.soundEvent(Key.key("minecraft:block.copper_trapdoor.close"), true, null,  listOf()),
        ))).let(resourcePack::soundRegistry)

        val blockyRegistry = resourcePack.soundRegistry("blocky") ?: SoundRegistry.soundRegistry("blocky", emptyList())
        SoundRegistry.soundRegistry(blockyRegistry.namespace(), blockyRegistry.sounds().plus(listOf(
            SoundEvent.soundEvent(Key.key("blocky:block.stone.place"), false, "subtitles.block.generic.place", VanillaSoundEntries.stoneDig),
            SoundEvent.soundEvent(Key.key("blocky:block.stone.break"), false, "subtitles.block.generic.break", VanillaSoundEntries.stoneDig),
            SoundEvent.soundEvent(Key.key("blocky:block.stone.hit"), false, "subtitles.block.generic.hit", VanillaSoundEntries.stoneStep),
            SoundEvent.soundEvent(Key.key("blocky:block.stone.fall"), false, null, VanillaSoundEntries.stoneStep),
            SoundEvent.soundEvent(Key.key("blocky:block.stone.step"), false, "subtitles.block.generic.footsteps", VanillaSoundEntries.stoneStep),

            SoundEvent.soundEvent(Key.key("blocky:block.wood.place"), false, "subtitles.block.generic.place", VanillaSoundEntries.woodDig),
            SoundEvent.soundEvent(Key.key("blocky:block.wood.break"), false, "subtitles.block.generic.break", VanillaSoundEntries.woodDig),
            SoundEvent.soundEvent(Key.key("blocky:block.wood.hit"), false, "subtitles.block.generic.hit", VanillaSoundEntries.woodStep),
            SoundEvent.soundEvent(Key.key("blocky:block.wood.fall"), false, null, VanillaSoundEntries.woodStep),
            SoundEvent.soundEvent(Key.key("blocky:block.wood.step"), false, "subtitles.block.generic.footsteps", VanillaSoundEntries.woodStep),

            SoundEvent.soundEvent(Key.key("blocky:block.copper.place"), false, "subtitles.block.generic.place", VanillaSoundEntries.copperDig),
            SoundEvent.soundEvent(Key.key("blocky:block.copper.break"), false, "subtitles.block.generic.break", VanillaSoundEntries.copperDig),
            SoundEvent.soundEvent(Key.key("blocky:block.copper.hit"), false, "subtitles.block.generic.hit", VanillaSoundEntries.copperStep),
            SoundEvent.soundEvent(Key.key("blocky:block.copper.fall"), false, null, VanillaSoundEntries.copperStep),
            SoundEvent.soundEvent(Key.key("blocky:block.copper.step"), false, "subtitles.block.generic.footsteps", VanillaSoundEntries.copperStep),

            SoundEvent.soundEvent(Key.key("blocky:block.copper_grate.place"), false, "subtitles.block.generic.place", VanillaSoundEntries.copperGrateDig),
            SoundEvent.soundEvent(Key.key("blocky:block.copper_grate.break"), false, "subtitles.block.generic.break", VanillaSoundEntries.copperGrateDig),
            SoundEvent.soundEvent(Key.key("blocky:block.copper_grate.hit"), false, "subtitles.block.generic.hit", VanillaSoundEntries.copperGrateStep),
            SoundEvent.soundEvent(Key.key("blocky:block.copper_grate.fall"), false, null, VanillaSoundEntries.copperGrateStep),
            SoundEvent.soundEvent(Key.key("blocky:block.copper_grate.step"), false, "subtitles.block.generic.footsteps", VanillaSoundEntries.copperGrateStep),

            SoundEvent.soundEvent(Key.key("blocky:block.copper_door.open"), false, null, VanillaSoundEntries.copperDoor),
            SoundEvent.soundEvent(Key.key("blocky:block.copper_door.close"), false, "subtitles.block.generic.footsteps", VanillaSoundEntries.copperDoor),

            SoundEvent.soundEvent(Key.key("blocky:block.copper_trapdoor.open"), false, null, VanillaSoundEntries.copperTrapDoor),
            SoundEvent.soundEvent(Key.key("blocky:block.copper_trapdoor.close"), false, "subtitles.block.generic.footsteps", VanillaSoundEntries.copperTrapDoor),
        ))).let(resourcePack::soundRegistry)
    }

    val stoneDig = listOf(
        SoundEntry.soundEntry().key(Key.key("dig/stone1")).build(),
        SoundEntry.soundEntry().key(Key.key("dig/stone2")).build(),
        SoundEntry.soundEntry().key(Key.key("dig/stone3")).build(),
        SoundEntry.soundEntry().key(Key.key("dig/stone4")).build()
    )
    val stoneStep = listOf(
        SoundEntry.soundEntry().key(Key.key("step/stone1")).build(),
        SoundEntry.soundEntry().key(Key.key("step/stone2")).build(),
        SoundEntry.soundEntry().key(Key.key("step/stone3")).build(),
        SoundEntry.soundEntry().key(Key.key("step/stone4")).build(),
        SoundEntry.soundEntry().key(Key.key("step/stone5")).build(),
        SoundEntry.soundEntry().key(Key.key("step/stone6")).build(),
    )
    val woodDig = listOf(
        SoundEntry.soundEntry().key(Key.key("dig/wood1")).build(),
        SoundEntry.soundEntry().key(Key.key("dig/wood2")).build(),
        SoundEntry.soundEntry().key(Key.key("dig/wood3")).build(),
        SoundEntry.soundEntry().key(Key.key("dig/wood4")).build()
    )
    val woodStep = listOf(
        SoundEntry.soundEntry().key(Key.key("step/wood1")).build(),
        SoundEntry.soundEntry().key(Key.key("step/wood2")).build(),
        SoundEntry.soundEntry().key(Key.key("step/wood3")).build(),
        SoundEntry.soundEntry().key(Key.key("step/wood4")).build(),
        SoundEntry.soundEntry().key(Key.key("step/wood5")).build(),
        SoundEntry.soundEntry().key(Key.key("step/wood6")).build(),
    )
    val copperDig = listOf(
        SoundEntry.soundEntry().key(Key.key("block/copper/dig1")).build(),
        SoundEntry.soundEntry().key(Key.key("block/copper/dig2")).build(),
        SoundEntry.soundEntry().key(Key.key("block/copper/dig3")).build(),
        SoundEntry.soundEntry().key(Key.key("block/copper/dig4")).build()
    )
    val copperStep = listOf(
        SoundEntry.soundEntry().key(Key.key("block/copper/step1")).build(),
        SoundEntry.soundEntry().key(Key.key("block/copper/step2")).build(),
        SoundEntry.soundEntry().key(Key.key("block/copper/step3")).build(),
        SoundEntry.soundEntry().key(Key.key("block/copper/step4")).build(),
        SoundEntry.soundEntry().key(Key.key("block/copper/step5")).build(),
        SoundEntry.soundEntry().key(Key.key("block/copper/step6")).build(),
    )
    val copperGrateDig = listOf(
        SoundEntry.soundEntry().key(Key.key("block/copper_grate/dig1")).build(),
        SoundEntry.soundEntry().key(Key.key("block/copper_grate/dig2")).build(),
        SoundEntry.soundEntry().key(Key.key("block/copper_grate/dig3")).build(),
        SoundEntry.soundEntry().key(Key.key("block/copper_grate/dig4")).build()
    )
    val copperGrateStep = listOf(
        SoundEntry.soundEntry().key(Key.key("block/copper_grate/step1")).build(),
        SoundEntry.soundEntry().key(Key.key("block/copper_grate/step2")).build(),
        SoundEntry.soundEntry().key(Key.key("block/copper_grate/step3")).build(),
        SoundEntry.soundEntry().key(Key.key("block/copper_grate/step4")).build(),
        SoundEntry.soundEntry().key(Key.key("block/copper_grate/step5")).build(),
        SoundEntry.soundEntry().key(Key.key("block/copper_grate/step6")).build(),
    )
    val copperDoor = listOf(
        SoundEntry.soundEntry().key(Key.key("block/copper_door/toggle1")).build(),
        SoundEntry.soundEntry().key(Key.key("block/copper_door/toggle2")).build(),
        SoundEntry.soundEntry().key(Key.key("block/copper_door/toggle3")).build(),
    )
    val copperTrapDoor = listOf(
        SoundEntry.soundEntry().key(Key.key("block/copper_door/toggle1")).build(),
        SoundEntry.soundEntry().key(Key.key("block/copper_door/toggle2")).build(),
        SoundEntry.soundEntry().key(Key.key("block/copper_door/toggle3")).build(),
        SoundEntry.soundEntry().key(Key.key("block/copper_door/toggle4")).build(),
    )
}