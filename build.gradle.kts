import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import com.matthewprenger.cursegradle.Options

plugins {
    kotlin("jvm") version "1.8.10"
    kotlin("plugin.serialization") version "1.8.10"
    id("fabric-loom") version "1.1-SNAPSHOT"
    id("com.modrinth.minotaur") version "2.+"
    id("com.matthewprenger.cursegradle") version "1.4.0"
    id("java")
}

group = "me.obsilabor"
version = "1.6.3+1.19.4"

repositories {
    mavenCentral()
    maven("https://maven.terraformersmc.com")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.isxander.dev/snapshots")
    maven("https://api.modrinth.com/maven")
}

dependencies {
    // kotlin
    implementation(kotlin("stdlib"))
    // event system
    implementation("me.obsilabor:alert:1.0.6")
    include("me.obsilabor:alert:1.0.6")
    // paper
    /**
     * I'm not using userdev here because I think it would cause issues together with loom.
     * Additionally, I don't use any NMS specific functions or classes
     */
    compileOnly("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")
    // fabric
    minecraft("com.mojang:minecraft:1.19.4-pre3")
    mappings("net.fabricmc:yarn:1.19.4-pre3+build.1")
    modImplementation("net.fabricmc:fabric-loader:0.14.11")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.9.1+kotlin.1.8.10")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.75.2+1.19.4")
    // modmenu
    modApi("maven.modrinth:modmenu:6.1.0-beta.3")
    // yacl
    //modImplementation("dev.isxander:yet-another-config-lib:2.3.0+beta.3+update.1.19.4-20230222.214402-1") xd
    modImplementation("dev.isxander:yet-another-config-lib:2.3.0+beta.3+update.1.19.4-SNAPSHOT")
}

tasks {
    processResources {
        val properties = mapOf(
            "version" to project.version,
        )
        inputs.properties(properties)
        filesMatching("fabric.mod.json") {
            expand(properties)
        }
        filesMatching("plugin.yml") {
            expand(properties)
        }
    }
    named("curseforge") {
        onlyIf {
            System.getenv("CURSEFORGE_TOKEN") != null
        }
        dependsOn(remapJar)
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    compileJava {
        options.release.set(17)
        options.encoding = "UTF-8"
    }
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("tps-hud")
    versionNumber.set(project.version.toString())
    versionType.set("release")
    gameVersions.addAll(listOf("1.19.4-pre3"))
    loaders.add("fabric")
    loaders.add("quilt")
    loaders.add("purpur")
    loaders.add("paper")
    loaders.add("spigot")
    loaders.add("bukkit")
    dependencies {
        required.project("fabric-api")
        required.project("fabric-language-kotlin")
        optional.project("yacl")
        optional.project("modmenu")
    }
    uploadFile.set(tasks.remapJar.get())
}

curseforge {
    project(closureOf<CurseProject> {
        apiKey = System.getenv("CURSEFORGE_TOKEN")
        mainArtifact(tasks.remapJar.get())

        id = "610618"
        releaseType = "release"
        addGameVersion("1.19-Snapshot")
        addGameVersion("Fabric")
        addGameVersion("Quilt")

        relations(closureOf<CurseRelation> {
            requiredDependency("fabric-api")
            requiredDependency("fabric-language-kotlin")
            optionalDependency("yacl")
            optionalDependency("modmenu")
        })
    })
    options(closureOf<Options> {
        forgeGradleIntegration = false
    })
}
