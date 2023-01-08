rootProject.name = "blocky"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.papermc.io/repository/maven-public/") //Paper
    }
}

dependencyResolutionManagement {
    val idofrontConventions: String by settings

    repositories {
        maven("https://repo.mineinabyss.com/releases")
    }

    versionCatalogs {
        create("libs").from("com.mineinabyss:catalog:$idofrontConventions")
        create("blockyLibs").from(files("gradle/blockyLibs.versions.toml"))
    }
}

val pluginName: String by settings
