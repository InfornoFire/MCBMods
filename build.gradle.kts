/*
 * MCBMods
 * Copyright (C) 2018-2024 Inforno
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    java
    idea
}

version = "1.5.0"
group = "inforno.mcbmods"

val accessTransformerName = "mcbmods_at.cfg"

loom {
    launchConfigs {
        "client" {
            property("elementa.dev", "false")
            property("elementa.debug", "false")
            property("elementa.invalid_usage", "warn")
            property("mixin.debug", "true")
            property("asmhelper.verbose", "true")
            arg("--tweakClass", "gg.essential.loader.stage0.EssentialSetupTweaker")
            arg("--mixin", "mixins.mcbmods.json")
            arg("--username", System.getenv("USERNAME"))
        }
    }
    runConfigs {
        "server" {
        }
    }
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
        accessTransformer(rootProject.file("src/main/resources/META-INF/$accessTransformerName"))
        mixinConfig("mixins.mcbmods.json")
    }
    mixin {
        defaultRefmapName.set("mixins.mcbmods.refmap.json")
    }
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.sk1er.club/repository/maven-public/")
    maven("https://repo.sk1er.club/repository/maven-releases/")
    maven("https://repo.spongepowered.org/maven-public/")
    maven("https://jitpack.io")
}

val shadowImplementation: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    // shadowImplementation("org.spongepowered:mixin:0.8.5") {
    //     isTransitive = false // Dependencies of mixin are already bundled by minecraft
    // }

    shadowImplementation("gg.essential:loader-launchwrapper:1.2.1")
    implementation("gg.essential:essential-1.8.9-forge:2581") {
        exclude(module = "asm")
        exclude(module = "asm-commons")
        exclude(module = "asm-tree")
        exclude(module = "gson")
    }

    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    compileOnly("org.spongepowered:mixin:0.8.5")

    shadowImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    modImplementation(fileTree("libs"))
}

tasks {
    processResources {
        inputs.property("version", project.version)
        inputs.property("mcversion", "1.8.9")

        filesMatching("mcmod.info") {
            expand(mapOf("version" to project.version, "mcversion" to "1.8.9"))
        }
        dependsOn(compileJava)
    }
    named<Jar>("jar") {
        manifest.attributes(
            mapOf(
                "FMLCorePluginContainsFMLMod" to true,
                "ModSide" to "CLIENT",
                "FMLAT" to accessTransformerName,
                "TweakClass" to "gg.essential.loader.stage0.EssentialSetupTweaker",
                "TweakOrder" to 0,
                "MixinConfigs" to "mixins.mcbmods.json"
            )
        )
        dependsOn(shadowJar)
        enabled = false
    }
    named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
        archiveBaseName.set("MCBMods")
        input.set(shadowJar.get().archiveFile)
        doLast {
            println("Jar name: ${archiveFile.get().asFile}")
        }
    }
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("MCBMods")
        archiveClassifier.set("dev")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        configurations = listOf(shadowImplementation)

        relocate("kotlinx.serialization", "inforno.mcbmods.ktx-serialization")

        exclude(
            "**/LICENSE.txt",
            "dummyThing",
            "META-INF/maven/**",
            "META-INF/versions/**"
        )

        into("META-INF") {
            from("LICENSE.md")
        }

        into("META-INF/licenses") {
            from("licenses")
        }

        mergeServiceFiles()
    }
}

java {
    withSourcesJar()
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

kotlin {
    jvmToolchain {
        check(this is JavaToolchainSpec)
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}