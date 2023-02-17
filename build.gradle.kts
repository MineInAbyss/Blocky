plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.copyjar")
    id("com.mineinabyss.conventions.publication")
    id("com.mineinabyss.conventions.papermc")
    id ("com.mineinabyss.conventions.nms")
    id("com.mineinabyss.conventions.autoversion")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
    maven("https://repo.mineinabyss.com/releases")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://mvn.lumine.io/repository/maven-public/") { metadataSources { artifact() } } // Model Engine
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
    compileOnly(blockyLibs.looty)
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

    implementation(blockyLibs.minecraft.plugin.protectionlib)
    implementation(blockyLibs.minecraft.plugin.customblockdata)
    implementation(blockyLibs.minecraft.plugin.morepersistentdatatypes)

    implementation(libs.bundles.idofront.core)
    implementation(libs.idofront.nms)
    implementation(libs.idofront.autoscan) {
        exclude("org.reflections")
    }
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
