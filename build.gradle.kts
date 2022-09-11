plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.copyjar")
    id("com.mineinabyss.conventions.publication")
    id("com.mineinabyss.conventions.papermc")
    id ("com.mineinabyss.conventions.nms")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
    maven("https://repo.mineinabyss.com/releases")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://mvn.lumine.io/repository/maven-public/") { metadataSources { artifact() } } // Model Engine
    maven("https://repo.dmulloy2.net/nexus/repository/public/") //ProtocolLib
    maven("https://mvn.intellectualsites.com/content/repositories/releases/") // FAWE
    maven("https://repo.codemc.io/repository/maven-snapshots/") // AnvilGUI
    maven("https://hub.jeff-media.com/nexus/repository/jeff-media-public/") //CustomBlockData
    maven("https://jitpack.io")
}

dependencies {
    // MineInAbyss platform
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.kotlinx.serialization.json)
    compileOnly(libs.kotlinx.serialization.kaml)
    compileOnly(libs.kotlinx.coroutines)
    compileOnly(libs.minecraft.mccoroutine)

    // Geary platform
    compileOnly(blockyLibs.geary.papermc.core)
    compileOnly(blockyLibs.geary.commons.papermc)
    compileOnly(blockyLibs.looty)
    compileOnly(blockyLibs.deeperworld)
    compileOnly(blockyLibs.guiy)

    // Other plugins
    compileOnly(libs.minecraft.plugin.modelengine)
    compileOnly(libs.minecraft.plugin.fawe.core)
    compileOnly(libs.minecraft.plugin.fawe.bukkit) { isTransitive = false }
    compileOnly(libs.minecraft.plugin.protocollib)
    compileOnly(libs.minecraft.headlib)
    compileOnly(libs.minecraft.anvilgui)
    compileOnly(blockyLibs.minecraft.plugin.lightapi)

    implementation(blockyLibs.minecraft.plugin.customblockdata)
    implementation(blockyLibs.minecraft.plugin.morepersistentdatatypes)

    implementation(libs.idofront.core)
    implementation(libs.idofront.nms)
    implementation(libs.idofront.autoscan) {
        exclude("org.reflections")
    }
}

tasks {
    shadowJar {
        relocate("com.jeff_media", "com.mineinabyss.shaded")
    }
}
