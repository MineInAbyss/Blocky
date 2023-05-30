@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.mia.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.mia.papermc)
    alias(libs.plugins.mia.nms)
    alias(libs.plugins.mia.copyjar)
    alias(libs.plugins.mia.publication)
    alias(libs.plugins.mia.autoversion)
    alias(libs.plugins.compose)
}

repositories {
    mavenCentral()
    maven("https://repo.mineinabyss.com/releases")
    maven("https://repo.mineinabyss.com/snapshots")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://mvn.lumine.io/repository/maven-public/") { metadataSources { artifact() } } // Model Engine
    maven("https://mvn.intellectualsites.com/content/repositories/releases/") // FAWE
    maven("https://repo.codemc.io/repository/maven-snapshots/") // AnvilGUI
    maven("https://hub.jeff-media.com/nexus/repository/jeff-media-public/") //CustomBlockData
    maven("https://www.asangarin.eu/repo/releases") // Breaker
    maven("https://jitpack.io")
}

dependencies {
    // MineInAbyss platform
    compileOnly(libs.kotlinx.serialization.json)
    compileOnly(libs.kotlinx.serialization.kaml)
    compileOnly(libs.kotlinx.coroutines)
    compileOnly(libs.minecraft.mccoroutine)

    // Geary platform
    compileOnly(blockyLibs.geary.papermc)
    compileOnly(gearyLibs.autoscan)
    compileOnly(blockyLibs.deeperworld)
    compileOnly(blockyLibs.guiy)

    // Other plugins
    compileOnly(libs.minecraft.plugin.modelengine)
    compileOnly(libs.minecraft.plugin.protocollib)
    compileOnly(libs.minecraft.plugin.fawe.core)
    compileOnly(libs.minecraft.plugin.fawe.bukkit) { isTransitive = false }
    //compileOnly(libs.minecraft.headlib.api)
    compileOnly(libs.minecraft.anvilgui)
    compileOnly(blockyLibs.minecraft.plugin.lightapi)
    compileOnly(blockyLibs.minecraft.plugin.breaker)
    compileOnly(blockyLibs.minecraft.plugin.mythic)

    implementation(blockyLibs.minecraft.plugin.protectionlib)
    implementation(blockyLibs.minecraft.plugin.customblockdata)
    //implementation(blockyLibs.minecraft.plugin.morepersistentdatatypes)

    implementation(libs.bundles.idofront.core)
    implementation(libs.idofront.nms)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf(
            "-Xcontext-receivers",
        )
    }
}

tasks {
    shadowJar {
        relocate("com.jeff_media.customblockdata", "com.mineinabyss.shaded.customblockdata")
        relocate("com.jeff_media.morepersistentdatatypes", "com.mineinabyss.shaded.morepersistentdatatypes")
    }
}
