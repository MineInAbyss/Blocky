rootProject.name = "blocky"

pluginManagement {
    repositories {
        gradlePluginPortal()

        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
        maven("https://repo.papermc.io/repository/maven-public/") //Paper
        mavenLocal()
    }
}

dependencyResolutionManagement {
    val idofrontVersion: String by settings

    repositories {
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
        mavenLocal()
    }

    versionCatalogs {
        create("libs"){
            from("com.mineinabyss:catalog:$idofrontVersion")
            version("modelengine", "R4.0.3")
        }
        create("blockyLibs").from(files("gradle/blockyLibs.versions.toml"))
    }
}

val pluginName: String by settings
