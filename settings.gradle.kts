rootProject.name = "blocky"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://repo.papermc.io/repository/maven-public/") //Paper
    }
    plugins {
        val kotlinVersion: String by settings
        val composeVersion: String by settings
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        id("org.jetbrains.compose") version composeVersion
    }

    val idofrontConventions: String by settings
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("com.mineinabyss.conventions"))
                useVersion(idofrontConventions)
        }
    }
}

dependencyResolutionManagement {
    val idofrontConventions: String by settings

    repositories {
        maven("https://repo.mineinabyss.com/releases")
    }

    versionCatalogs {
        create("libs"){
            from("com.mineinabyss:catalog:$idofrontConventions")
        }
        create("blockyLibs"){
            from(files("gradle/blockyLibs.versions.toml"))
        }
    }
}

val pluginName: String by settings
