plugins {
    kotlin("jvm") version "1.4.31"
}

group = "cz.cuni.gamedev.nail123.mcworldgeneration"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":MCWorldGenerationPlugin"))
    implementation("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    implementation("org.bukkit:bukkit:1.15.2-R0.1-SNAPSHOT")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    jar {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        // This line of code recursively collects and copies all of a project's files
        // and adds them to the JAR itself. One can extend this task, to skip certain
        // files or particular types at will
        from(configurations.runtimeClasspath.get().map { file ->
            if (file.isDirectory) file else zipTree(file)
        })

        archiveFileName.set("MCWorldGenerationVisualizer.jar")
        manifest {
            attributes["Main-Class"] = "cz.cuni.gamedev.nail123.mcworldgeneration.visualizer.MainKt"
        }
    }
}
