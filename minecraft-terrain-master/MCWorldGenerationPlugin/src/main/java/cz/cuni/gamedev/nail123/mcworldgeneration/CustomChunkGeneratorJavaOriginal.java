package cz.cuni.gamedev.nail123.mcworldgeneration;

import cz.cuni.gamedev.nail123.mcworldgeneration.chunking.IChunk;
import org.bukkit.Material;
import org.bukkit.util.noise.OctaveGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;


public class CustomChunkGeneratorJavaOriginal implements IChunkGenerator {
    public long seed;
    OctaveGenerator generator;
    OctaveGenerator temperatureGenerator;
    OctaveGenerator annualParticipationGenerator;
    OctaveGenerator heightGenerator;

    public CustomChunkGeneratorJavaOriginal(long seed) {
        this.seed = seed;
        this.generator = new SimplexOctaveGenerator(new Random(this.seed), 8);
        this.generator.setScale(0.003);

        this.temperatureGenerator = new SimplexOctaveGenerator(new Random(this.seed).nextLong(), 8);
        this.temperatureGenerator.setScale(0.005);
        this.annualParticipationGenerator = new SimplexOctaveGenerator(new Random(new Random(this.seed).nextLong()).nextLong(), 8);
        this.annualParticipationGenerator.setScale(0.005);
        this.heightGenerator = new SimplexOctaveGenerator(new Random(new Random(new Random(this.seed).nextLong()).nextLong()).nextLong(), 4);
        this.heightGenerator.setScale(0.01);

    }
    public CustomChunkGeneratorJavaOriginal() {
        this((new Random()).nextLong());
    }

    public Material fictionalWhitakkerIdentification(double temperatureNoise, double annualParticipationNoise){
        if (temperatureNoise >= -10 && temperatureNoise < 10 && annualParticipationNoise >= 0 && annualParticipationNoise < 200){
            return Material.SNOW;
        }
        else if (temperatureNoise >= 10 && temperatureNoise <= 30 && annualParticipationNoise >= 0 && annualParticipationNoise < 200){
            return Material.GRASS_BLOCK;
        }
        else{
            return Material.WATER;
        }
    }

    public int heightGenerator(Material currentMaterial, int X, int Z){
        double heightNoise = heightGenerator.noise(X, Z, 2.0, 1, true);

        int currentHeight = 0;
        if (currentMaterial == Material.GRASS_BLOCK){
            currentHeight = (int) (Math.round((heightNoise + 1) / 2 * 60.0));
        }
        else if (currentMaterial == Material.SNOW){
            currentHeight = (int) (Math.round((heightNoise + 1) / 2 * 150.0));
        }
        else if (currentMaterial == Material.WATER){
            currentHeight = (int) (Math.round((heightNoise + 1) / 2 * 30.0));
        }
        return currentHeight;
    }

    public void generateChunk(int chunkX, int chunkZ, @NotNull IChunk chunk) {

        double max = Double.NEGATIVE_INFINITY;
        double min = Double.POSITIVE_INFINITY;

        int[][] heightCache = new int[1024][768];
        for (int i = 0; i < 1024; i ++){
            for (int j = 0; j < 768; j++){
                heightCache[i][j] = Integer.MAX_VALUE;
            }
        }
        //Arrays.fill(heightCache, Integer.MAX_VALUE);

        for (int X = 0; X <= 15; ++X) {
            for (int Z = 0; Z <= 15; ++Z) {
                double temperatureNoise = temperatureGenerator.noise(chunkX * 16.0 + X, chunkZ * 16.0 + Z, 1, 1, true);
                temperatureNoise *= 20;
                temperatureNoise += 10;
                double annualParticipationNoise = annualParticipationGenerator.noise(chunkX * 16.0 + X, chunkZ * 16.0 + Z, 1, 1, true);
                annualParticipationNoise *= 200;
                annualParticipationNoise += 200;
                Material currentMaterial = fictionalWhitakkerIdentification(temperatureNoise, annualParticipationNoise);
                //System.out.println(currentMaterial);
                /*
                if (temperatureNoise > max){
                    max = temperatureNoise;
                    System.out.println("max number so far:" + max);
                }

                if (temperatureNoise < min){
                    min = temperatureNoise;
                    System.out.println("min number so far:" + min);
                }
                */


                //for (int i = 80; i <= 80; ++i) {
                //    chunk.set(X, i, Z, temp);
                //}

                int indexX = chunkX * 16 + X;
                int indexZ = chunkZ * 16 + Z;
                int currentSmoothedHeight = 0;

                for (int i = -2; i <= 2; i ++){
                    for (int j = -2; j <= 2; j++){
                        int inRangeX = indexX + i;
                        int inRangeZ = indexZ + j;
                        if (inRangeX < 0){
                            inRangeX = 0;
                        }
                        else if (inRangeX >= 1024){
                            inRangeX = 1023;
                        }
                        if (inRangeZ < 0){
                            inRangeZ = 0;
                        }
                        else if (inRangeZ >= 768){
                            inRangeZ = 767;
                        }

                        if (heightCache[inRangeX][inRangeZ] == Integer.MAX_VALUE){
                            heightCache[inRangeX][inRangeZ] = heightGenerator(currentMaterial, inRangeX, inRangeZ);
                        }
                        currentSmoothedHeight += heightCache[inRangeX][inRangeZ];
                    }
                }

                currentSmoothedHeight /= (15 * 15);
                //System.out.println(currentSmoothedHeight);


                //int currentHeight = heightGenerator(currentMaterial, indexX, indexZ);
                chunk.set(X, currentSmoothedHeight, Z, currentMaterial);
            }
        }


        /*
        for (int X = 0; X <= 15; ++X) {
            for (int Z = 0; Z <= 15; ++Z) {
                // X and Z are the geographic coordinates in Minecraft

                // Noise normalized to -1 .. 1
                double noise = generator.noise(chunkX * 16.0 + X, chunkZ * 16.0 + Z, 2.0, 0.5, true);

                int currentHeight = (int) (Math.round((noise + 1) * 50.0 + 30.0));

                // If lower than sea level (62), add water
                for (int i = currentHeight + 1; i <= 62; ++i) {
                    chunk.set(X, i, Z, Material.WATER);
                }

                // Set top layer of material based on height
                if (currentHeight <= 70) {
                    chunk.set(X, currentHeight, Z, Material.SAND);
                } else if (currentHeight <= 95) {
                    chunk.set(X, currentHeight, Z, Material.GRASS_BLOCK);
                } else {
                    chunk.set(X, currentHeight, Z, Material.SNOW);
                }

                // Place dirt one block underneath
                chunk.set(X, currentHeight - 1, Z, Material.DIRT);

                // Place stone all the way to the bottom, except for the last layer
                for (int i = currentHeight - 2; i >= 1; --i) {
                    chunk.set(X, i, Z, Material.STONE);
                }

                // ... which is bedrock
                chunk.set(X, 0, Z, Material.BEDROCK);
            }
        }
        */
    }
}
