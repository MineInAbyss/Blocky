import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    alias(idofrontLibs.plugins.mia.kotlin.jvm)
    alias(idofrontLibs.plugins.kotlinx.serialization)
    alias(idofrontLibs.plugins.mia.papermc)
    alias(idofrontLibs.plugins.mia.nms)
    alias(idofrontLibs.plugins.mia.copyjar)
    alias(idofrontLibs.plugins.mia.publication)
    alias(idofrontLibs.plugins.mia.autoversion)
    alias(idofrontLibs.plugins.compose.compiler)
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
}

repositories {
    mavenCentral()
    maven("https://repo.mineinabyss.com/releases")
    maven("https://repo.mineinabyss.com/snapshots")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://mvn.lumine.io/repository/maven-public/") // Model Engine
    maven("https://mvn.intellectualsites.com/content/repositories/releases/") // FAWE
    maven("https://hub.jeff-media.com/nexus/repository/jeff-media-public/") //CustomBlockData
    maven("https://repo.oraxen.com/releases") // ProtectionLib
    mavenLocal()
}

dependencies {
    // MineInAbyss platform
    compileOnly(idofrontLibs.bundles.idofront.core)
    compileOnly(idofrontLibs.idofront.nms)
    compileOnly(idofrontLibs.kotlinx.serialization.json)
    compileOnly(idofrontLibs.kotlinx.serialization.kaml)
    compileOnly(idofrontLibs.kotlinx.coroutines)
    compileOnly(idofrontLibs.minecraft.mccoroutine)

    // Geary platform
    compileOnly(libs.geary.papermc)
    compileOnly(libs.guiy)

    // Other plugins
    compileOnly(idofrontLibs.minecraft.plugin.modelengine)
    compileOnly(idofrontLibs.minecraft.plugin.fawe.core)
    compileOnly(idofrontLibs.minecraft.plugin.fawe.bukkit) { isTransitive = false }
    compileOnly(idofrontLibs.creative.api)
    compileOnly(idofrontLibs.creative.serializer.minecraft)

    implementation(libs.minecraft.plugin.protectionlib)
    implementation(libs.minecraft.plugin.customblockdata)
}

tasks {
    shadowJar {
        relocate("com.jeff_media.customblockdata", "com.mineinabyss.shaded.customblockdata")
        relocate("com.jeff_media.morepersistentdatatypes", "com.mineinabyss.shaded.morepersistentdatatypes")
    }
}

paper {
    main = "com.mineinabyss.blocky.BlockyPlugin"
    name = "Blocky"
    prefix = "Blocky"
    val version: String by project
    this.version = version
    authors = listOf("boy0000")
    apiVersion = "1.20"

    serverDependencies {
        register("Geary") {
            required = true
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            joinClasspath = true
        }
        register("Guiy") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            joinClasspath = true
        }
        register("FastAsyncWorldEdit") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            joinClasspath = true
        }
        register("ModelEngine") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            joinClasspath = true
        }
    }
}
