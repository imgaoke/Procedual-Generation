group = "cz.cuni.gamedev.nail123.mcworldgeneration"
version = "1.0-SNAPSHOT"

subprojects {
    version = "1.0"

    repositories {
        mavenCentral()

        maven {
            url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
        }
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        }
    }
}

plugins {
    id("de.undercouch.download") version "4.1.1"
}

// ======================================
//  Use generator to output Map into PNG
// ======================================

tasks.register<JavaExec>("generateMap") {
    group = "map generation"
    dependsOn(":MCWorldGenerationVisualizer:jar")

    main = "-jar"
    args = listOf("MCWorldGenerationVisualizer/build/libs/MCWorldGenerationVisualizer.jar")
}

tasks.register<JavaExec>("generateMapGrayscale") {
    group = "map generation"
    dependsOn(":MCWorldGenerationVisualizer:jar")

    main = "-jar"
    args = listOf("MCWorldGenerationVisualizer/build/libs/MCWorldGenerationVisualizer.jar", "--grayscale")
}

// ================================================
//  Run a full Minecraft server with custom plugin
// ================================================

val spigotVersion = "1.16.5"
val serverRunnable = "spigot-$spigotVersion.jar"

tasks.register<de.undercouch.gradle.tasks.download.Download>("getBuildTools") {
    group = "server"
    src("https://hub.spigotmc.org/jenkins/job/BuildTools/lastStableBuild/artifact/target/BuildTools.jar")
    dest("server/BuildTools.jar")
    overwrite(false)

    outputs.file(dest)
}

tasks.register("buildServer") {
    dependsOn("getBuildTools")
    group = "server"
    outputs.file("server/$serverRunnable")
    doLast {
        // Check also dynamically to prevent building server whenever build.gradle changes
        val f = File("server/$serverRunnable")
        if (!f.exists()) {
            javaexec {
                workingDir = File("server")
                main = "-jar"
                args = listOf("BuildTools.jar", "--rev", spigotVersion)
            }
        }
    }
}

tasks.register<Delete>("cleanServer") {
    group = "server"
    (
        fileTree("server") { exclude("BuildTools.jar", "eula.txt", "plugins") } +
        fileTree("server/plugins") { exclude("StartCommands.jar", "StartCommands") }
    ).visit {
        delete(this.file)
    }
}

tasks.register<Delete>("deleteMap") {
    group = "server"
    delete("server/world")
}

tasks.register("configureServerForPlugin") {
    group = "server"
    dependsOn("buildServer")
    doLast {
        val f = File("server/bukkit.yml")
        if (!f.exists()) {
            // We run the server once (quickly) to generate bukkit.yml
            javaexec {
                workingDir = File("server")
                main = "-jar"
                args = listOf(serverRunnable)
                standardInput = "stop\n".byteInputStream()
            }
        }
        val lines = f.readLines()
        if (lines.none { "worlds:" in it }) {
            f.appendText("""
                worlds:
                  world:
                    generator: MCWorldGenerationPlugin
            """.trimIndent())
            delete("server/world")
        }
    }
}

tasks.register<Copy>("copyPluginToServer") {
    group = "server"
    dependsOn(":MCWorldGenerationPlugin:jar")
    // Find the correct file and copy it

    from("MCWorldGenerationPlugin/build/libs")
    into("server/plugins")
}

tasks.register<JavaExec>("runServer") {
    group = "server"
    dependsOn("configureServerForPlugin", "copyPluginToServer")

    workingDir = File("server")
    main = "-jar"
    args = listOf(serverRunnable)
    standardInput = System.`in`
}