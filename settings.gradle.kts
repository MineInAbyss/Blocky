import org.gradle.internal.impldep.junit.runner.Version.id

rootProject.name = "blocky"

pluginManagement {
    repositories {
        gradlePluginPortal()

        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.papermc.io/repository/maven-public/") //Paper
    }

    val composeVersion: String by settings
    plugins {
        id("org.jetbrains.compose") version composeVersion
    }

    val idofrontVersion: String by settings
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("com.mineinabyss.conventions"))
                useVersion(idofrontVersion)
        }
    }
}

dependencyResolutionManagement {
    val idofrontVersion: String by settings
    val gearyVersion: String by settings

    repositories {
        maven("https://repo.mineinabyss.com/releases")
    }

    versionCatalogs {
        create("libs").from("com.mineinabyss:catalog:$idofrontVersion")
        create("gearyLibs").from("com.mineinabyss:geary-catalog:$gearyVersion")
        create("blockyLibs").from(files("gradle/blockyLibs.versions.toml"))
    }
}

val pluginName: String by settings
