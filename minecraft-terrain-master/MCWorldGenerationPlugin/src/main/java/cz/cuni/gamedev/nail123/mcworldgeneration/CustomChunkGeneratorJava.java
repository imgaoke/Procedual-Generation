package cz.cuni.gamedev.nail123.mcworldgeneration;

import cz.cuni.gamedev.nail123.mcworldgeneration.chunking.IChunk;
import org.bukkit.Material;
import org.bukkit.util.noise.OctaveGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Random;


public class CustomChunkGeneratorJava implements IChunkGenerator {
    public long seed;
    OctaveGenerator temperatureGenerator;
    OctaveGenerator annualParticipationGenerator;
    OctaveGenerator heightGenerator;

    public CustomChunkGeneratorJava(long seed) {
        this.seed = seed;

        // temperatureGenerator and annualParticipationGenerator are used to create a fictinal Whitakker Diagram
        // the two generators' scale are set so that some several cycles could be presented on a 1024 * 768 map
        this.temperatureGenerator = new SimplexOctaveGenerator(new Random(this.seed).nextLong(), 8);
        this.temperatureGenerator.setScale(0.005);
        this.annualParticipationGenerator = new SimplexOctaveGenerator(new Random(new Random(this.seed).nextLong()).nextLong(), 8);
        this.annualParticipationGenerator.setScale(0.005);
        // the third generator is for height generation with larger scale to present rugged terrain in each biome
        this.heightGenerator = new SimplexOctaveGenerator(new Random(new Random(new Random(this.seed).nextLong()).nextLong()).nextLong(), 8);
        this.heightGenerator.setScale(0.01);

    }
    public CustomChunkGeneratorJava() {
        this((new Random()).nextLong());
    }

    // a fictional Whitakker generator
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

    // a height generator based on the biome
    public int heightGenerator(Material currentMaterial, int X, int Z){
        double heightNoise = heightGenerator.noise((double)X, (double)Z, 2.0, 1, true);

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

    // a height generator based on the coordinates
    public Material materialGenerator(int X, int Z){
        // adjust the range of temperatureNoise to be from -10 to 30
        double temperatureNoise = temperatureGenerator.noise((double)X, (double)Z, 1, 1, true);
        temperatureNoise *= 20;
        temperatureNoise += 10;
        // adjust the range of temperatureNoise to be from 0 to 400
        double annualParticipationNoise = annualParticipationGenerator.noise((double)X, (double)Z, 1, 1, true);
        annualParticipationNoise *= 200;
        annualParticipationNoise += 200;

        Material currentMaterial = fictionalWhitakkerIdentification(temperatureNoise, annualParticipationNoise);
        return currentMaterial;
    }

    public void generateChunk(int chunkX, int chunkZ, @NotNull IChunk chunk) {
        // a 2D array which is used to store height generated from heightGenerator(with a fixed seed) and is initialized to be Integer.MAX_VALUE
        int[][] heightCache = new int[1024][768];

        for (int i = 0; i < 1024; i ++){
            for (int j = 0; j < 768; j++){
                heightCache[i][j] = Integer.MAX_VALUE;
            }
        }
        // a 2D array which is used to store height generated from the fictional Whitakker generator(with a fixed seed) and is initialized to be Material.ACACIA_DOOR
        Material[][] materialCache = new Material[1024][768];
        for (int i = 0; i < 1024; i ++){
            for (int j = 0; j < 768; j++){
                materialCache[i][j] = Material.ACACIA_DOOR;
            }
        }


        for (int X = 0; X <= 15; ++X) {
            for (int Z = 0; Z <= 15; ++Z) {
                // indexes to the 1024 * 768 map
                int indexX = chunkX * 16 + X;
                int indexZ = chunkZ * 16 + Z;

                int currentSmoothedHeight = 0;
                // a 11x11 mean filter
                for (int i = -5; i <= 5; i ++){
                    for (int j = -5; j <= 5; j++){
                        // the indexes to be stay in the 1024 * 768 map
                        int inRangeX = indexX + i;
                        int inRangeZ = indexZ + j;

                        // if the indexes is outside the 1024 * 768, the indexes is clutched to the nearest border
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

                        // if the cache array do not have the value stored, use the generator to store the value in the cache array
                        if (materialCache[inRangeX][inRangeZ] == Material.ACACIA_DOOR){
                            materialCache[inRangeX][inRangeZ] = materialGenerator(inRangeX, inRangeZ);
                        }
                        // if the cache array do not have the value stored, use the generator to store the value in the cache array
                        if (heightCache[inRangeX][inRangeZ] == Integer.MAX_VALUE){
                            heightCache[inRangeX][inRangeZ] = heightGenerator(materialCache[inRangeX][inRangeZ], inRangeX, inRangeZ);
                        }
                        currentSmoothedHeight += heightCache[inRangeX][inRangeZ];
                    }
                }
                // use a larger tile size to divide the currentSmoothedHeight to generate contour effects
                currentSmoothedHeight = currentSmoothedHeight / 300;
                chunk.set(X, currentSmoothedHeight, Z, materialCache[chunkX * 16 + X][chunkZ * 16 + Z]);
            }
        }
    }
}
