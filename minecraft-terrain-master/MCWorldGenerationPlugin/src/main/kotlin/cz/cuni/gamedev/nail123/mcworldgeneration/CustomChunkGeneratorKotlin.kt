package cz.cuni.gamedev.nail123.mcworldgeneration

import cz.cuni.gamedev.nail123.mcworldgeneration.chunking.IChunk
import org.bukkit.Material
import org.bukkit.util.noise.SimplexOctaveGenerator
import java.util.Random

class CustomChunkGeneratorKotlin(var seed: Long = Random().nextLong()): IChunkGenerator {
    val generator = SimplexOctaveGenerator(Random(seed), 8)

    init {
        generator.setScale(0.001)
    }

    override fun generateChunk(chunkX: Int, chunkZ: Int, chunk: IChunk) {
        for (X in 0..15) {
            for (Z in 0..15) {
                // X and Z are the geographic coordinates in Minecraft

                // Noise normalized to -1 .. 1
                val noise = generator.noise(chunkX * 16.0 + X, chunkZ * 16.0 + Z, 2.0, 0.5, true)

                val currentHeight = ((noise + 1) * 50.0 + 30.0).toInt()

                // If lower than sea level (62), add water
                for (height in currentHeight + 1 .. 62) {
                    chunk[X, height, Z] = Material.WATER
                }

                // Set top layer of material based on height
                chunk[X, currentHeight, Z] = when (currentHeight) {
                    in 0..70 -> Material.SAND
                    in 70..95 -> Material.GRASS_BLOCK
                    else -> Material.SNOW
                }

                // Place dirt one block underneath
                chunk[X, currentHeight - 1, Z] = Material.DIRT

                // Place stone all the way to the bottom, except for the last layer
                for (i in currentHeight - 2 downTo 1)
                    chunk[X, i, Z] = Material.STONE

                // ... which is bedrock
                chunk[X, 0, Z] = Material.BEDROCK
            }
        }
    }
}