import Com_mineinabyss_conventions_platform_gradle.Deps

val idofrontVersion: String by project
val gearyPlatformVersion: String by project
val deeperworldVersion: String by project
val guiyVersion: String by project
val modelengineVersion: String by project

plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.papermc")
    id("com.mineinabyss.conventions.copyjar")
    id("com.mineinabyss.conventions.publication")
    id("org.jetbrains.compose") version "1.0.1"
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
    maven("https://repo.mineinabyss.com/releases")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://mvn.lumine.io/repository/maven-public/") // Model Engine
    maven("https://repo.dmulloy2.net/nexus/repository/public/") //ProtocolLib
    maven("https://mvn.intellectualsites.com/content/repositories/releases/") // FAWE
    maven("https://jitpack.io")
}

dependencies {
    // MineInAbyss platform
    compileOnly(Deps.kotlin.stdlib)
    compileOnly(Deps.kotlinx.serialization.json)
    compileOnly(Deps.kotlinx.serialization.kaml)
    compileOnly(Deps.kotlinx.coroutines)
    compileOnly(Deps.minecraft.skedule)

    compileOnly(Deps.`sqlite-jdbc`) { isTransitive = false }
    compileOnly(Deps.exposed.core) { isTransitive = false }
    compileOnly(Deps.exposed.dao) { isTransitive = false }
    compileOnly(Deps.exposed.jdbc) { isTransitive = false }
    compileOnly(Deps.exposed.`java-time`) { isTransitive = false }

    // Geary platform
    compileOnly(platform("com.mineinabyss:geary-platform:$gearyPlatformVersion"))
    compileOnly("com.mineinabyss:geary-papermc-core")
    compileOnly("com.mineinabyss:geary-commons-papermc")
    compileOnly("com.mineinabyss:looty")

    // Other plugins
    compileOnly("com.mineinabyss:deeperworld:$deeperworldVersion")
    compileOnly("com.mineinabyss:guiy-compose:$guiyVersion")
    compileOnly("com.ticxo.modelengine:api:$modelengineVersion")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0")
    compileOnly("com.fastasyncworldedit:FAWE-Bukkit:1.17-47") { isTransitive = false }
    compileOnly("com.fastasyncworldedit:FAWE-Core:1.17-47")
    compileOnly("com.github.BeYkeRYkt:LightAPI:5.2.0-Bukkit")
    compileOnly(Deps.minecraft.headlib)
    compileOnly(Deps.minecraft.anvilgui)

    // Shaded
    implementation("com.mineinabyss:idofront:$idofrontVersion")

}
