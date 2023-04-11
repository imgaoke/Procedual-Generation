plugins {
    kotlin("jvm") version "1.4.31"
}

group = "cz.cuni.gamedev.nail123.mcworldgeneration"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("org.bukkit:bukkit:1.15.2-R0.1-SNAPSHOT")
}

repositories {
    mavenCentral()

    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
    }
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    jar {
        archiveFileName.set("MCWorldGenerationPlugin.jar")

        // This line of code recursively collects and copies all of a project's files
        // and adds them to the JAR itself. One can extend this task, to skip certain
        // files or particular types at will
        from(configurations.runtimeClasspath.get().map { file -> if (file.isDirectory) file else zipTree(file) })
    }
}