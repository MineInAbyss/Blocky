val idofrontVersion: String by project
val gearyPlatformVersion: String by project
val deeperworldVersion: String by project
val guiyVersion: String by project
val modelengineVersion: String by project
val composeVersion: String by project

plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.copyjar")
    id("com.mineinabyss.conventions.publication")
    id("com.mineinabyss.conventions.papermc")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
    maven("https://repo.mineinabyss.com/releases")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://mvn.lumine.io/repository/maven-public/") // Model Engine
    maven("https://repo.dmulloy2.net/nexus/repository/public/") //ProtocolLib
    maven("https://mvn.intellectualsites.com/content/repositories/releases/") // FAWE
    maven("https://repo.codemc.io/repository/maven-snapshots/") // AnvilGUI
    maven("https://jitpack.io")
}

dependencies {
    // MineInAbyss platform
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.kotlinx.serialization.json)
    compileOnly(libs.kotlinx.serialization.kaml)
    compileOnly(libs.kotlinx.coroutines)
    compileOnly(libs.minecraft.skedule)

    // Geary platform
    compileOnly(platform("com.mineinabyss:geary-platform:$gearyPlatformVersion"))
    compileOnly("com.mineinabyss:geary-papermc-core")
    compileOnly("com.mineinabyss:geary-commons-papermc")
    compileOnly("com.mineinabyss:looty")

    // Other plugins
    compileOnly("com.mineinabyss:deeperworld:$deeperworldVersion")
    compileOnly("com.mineinabyss:guiy-compose:$guiyVersion")
    compileOnly("com.ticxo.modelengine:api:$modelengineVersion")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit:2.1.1") { isTransitive = false }
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core:2.1.1")
    compileOnly("com.github.BeYkeRYkt:LightAPI:5.2.0-Bukkit")
    compileOnly(libs.minecraft.headlib)
    compileOnly(libs.minecraft.anvilgui)

    // Shaded
    implementation("com.mineinabyss:idofront:$idofrontVersion")

}
