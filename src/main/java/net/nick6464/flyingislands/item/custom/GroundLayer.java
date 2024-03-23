package net.nick6464.flyingislands.item.custom;

import net.nick6464.flyingislands.FlyingIslands;

import java.util.Random;

public class GroundLayer {
    public static int RADIAL_DETAIL_MULTIPLIER = 0;
    public static boolean[][] groundLayer = new boolean[0][];
    public static int ISLAND_SIZE;
    public static int ISLAND_RADIUS;
    public static int ISLAND_CONTAINER_SIZE;
    public static int ISLAND_CONTAINER_RADIUS;
    public static int SEED;
    public static float GROUNDLAYER_FREQUENCY;
    static float GROUNDLAYER_MAGNITUDE;
    public static float LAKE_MAGNITUDE = 0.5f;
    static int SMOOTHING_PASSES = 2;
    public static Random random;

    public GroundLayer(int seed) {
        random = new Random(SEED);

        ISLAND_SIZE = generateRandomNumber(10, 64);
        ISLAND_RADIUS = ISLAND_SIZE / 2;
        ISLAND_CONTAINER_SIZE = ISLAND_SIZE * 2;
        ISLAND_CONTAINER_RADIUS = ISLAND_CONTAINER_SIZE / 2;
        RADIAL_DETAIL_MULTIPLIER = ISLAND_SIZE;
        GROUNDLAYER_FREQUENCY = generateRandomFloat(0.1f, 0.2f);
        GROUNDLAYER_MAGNITUDE = generateRandomFloat(1.5f, ISLAND_SIZE / 10f);

        groundLayer = new boolean[ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE];

        SEED = seed;
    }

    public static void generateGroundLayer() {
        radialNoiseGenerator();
        smoothHardCorners();
        floodFill(ISLAND_CONTAINER_SIZE / 2, ISLAND_CONTAINER_SIZE / 2);
    }

    public boolean getBlock(int x, int z) {
        return groundLayer[x][z];
    }

    public static void groundLayerCloser() {

    }

    public static void radialNoiseGenerator(){
        // Use the block count in the circle to determine the number of blocks in a perfect circle
        int blockCount = RADIAL_DETAIL_MULTIPLIER * blocksInCircle();

        // Divide a perfect circle by the number of blocks in a circle, this is the number of
        // angles to use to generate the circle. A block will be placed at each angle, and 1D noise
        // will be added or subtracted to the distance from the center of the circle to the block.

        // Loop over every angle
        for (int step = 0; step <= blockCount; step++) {
            // Determine the distance from the center of the circle to the block
            // Determine the angle in radians

            // Find the x and y coords of the block using the angle and distance
            double angleFromCenter = Math.toRadians(step * ((double) 360 / blockCount));
            // By using the angle, we can calculate the x and z coordinates of the block, which
            // are centered around the x: ISLAND_RADIUS, y: ISLAND_RADIUS point
            int xOff = (int) (ISLAND_RADIUS + (ISLAND_RADIUS * Math.cos(angleFromCenter)));
            int zOff = (int) (ISLAND_RADIUS + (ISLAND_RADIUS * Math.sin(angleFromCenter)));

            // Get the noise value at the x and z coordinates
            double noise =
                    GROUNDLAYER_MAGNITUDE * OpenSimplex2S.noise2(SEED, GROUNDLAYER_FREQUENCY * xOff,
                            GROUNDLAYER_FREQUENCY * zOff) * GROUNDLAYER_MAGNITUDE;

            // Then go out from the center again at the angle and distance, and set the block to true
            // from x: ISLAND_RADIUS, y: ISLAND_RADIUS point
            // The x and z coordinates are centered around the x: ISLAND_RADIUS, y: ISLAND_RADIUS point
            int x = (int) (ISLAND_CONTAINER_RADIUS + ISLAND_RADIUS * Math.cos(angleFromCenter));
            int z = (int) (ISLAND_CONTAINER_RADIUS + ISLAND_RADIUS * Math.sin(angleFromCenter));

            // Set the block at (x, z) to true if it's within the valid range
            if (x >= 0 && x < ISLAND_CONTAINER_SIZE && z >= 0 && z < ISLAND_CONTAINER_SIZE) {
                groundLayer[x][z] = true;
            }
        }
    }

    // Flood fill algorithm to make the island solid
    public static void floodFill(int x, int z) {
        if (x < 0 || x >= ISLAND_CONTAINER_SIZE || z < 0 || z >= ISLAND_CONTAINER_SIZE) {
            return;
        }
        if (groundLayer[x][z]) {
            return;
        }
        groundLayer[x][z] = true;
        floodFill(x + 1, z);
        floodFill(x - 1, z);
        floodFill(x, z + 1);
        floodFill(x, z - 1);
    }

    // Remove the hard corners of the circle
    // eg
    // 0 0 0 0 0
    // 0 1 1 1 0
    // 0 1 0 0 0
    // 0 1 0 0 0
    // would become
    // 0 0 0 0 0
    // 0 0 1 1 0
    // 0 1 0 0 0
    // 0 1 0 0 0
    // by removing the hard corners
    public static void smoothHardCorners(){
        for (int smoothing = 0; smoothing < SMOOTHING_PASSES; smoothing++){
            for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
                for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                    if (groundLayer[x][z]) {
                        // Check if the block is a hard corner and set it to false if it is
                        // Ensure both the positive and negative are done to avoid a corner being missed

                        // Check if the coordinates would be in range
                        if (x - 2 < 0 || x + 2 >= ISLAND_CONTAINER_SIZE || z - 2 < 0 || z + 2 >= ISLAND_CONTAINER_SIZE) {
                            continue;
                        }


                        if (x - 2 > 0 && groundLayer[x - 2][z] && groundLayer[x - 1][z] && groundLayer[x][z - 2] && groundLayer[x][z - 1]) {
                            groundLayer[x][z] = false;
                        }
                        if (x - 2 > 0 && groundLayer[x - 2][z] && groundLayer[x - 1][z] && groundLayer[x][z + 2] && groundLayer[x][z + 1]) {
                            groundLayer[x][z] = false;
                        }
                        if (x + 2 < ISLAND_CONTAINER_SIZE && groundLayer[x + 2][z] && groundLayer[x + 1][z] && groundLayer[x][z + 2] && groundLayer[x][z + 1]) {
                            groundLayer[x][z] = false;
                        }
                        if (x + 2 < ISLAND_CONTAINER_SIZE && groundLayer[x + 2][z] && groundLayer[x + 1][z] && groundLayer[x][z - 2] && groundLayer[x][z - 1]) {
                            groundLayer[x][z] = false;
                        }
                    }
                }
            }
        }
    }


    // This method should return the number of blocks in a perfect circle based on the ISLAND_SIZE
    public static int blocksInCircle() {
        // The blocks can be calculated by diameter - 1 * 4
        return (ISLAND_SIZE - 1) * 4;
    }

    // Gets the next random number based on the seed
    public static int generateRandomNumber(int lowerBound, int upperBound) {
        return random.nextInt(upperBound - lowerBound + 1) + lowerBound;
    }

    public float generateRandomFloat(float lowerBound, float upperBound) {
        return random.nextFloat() * (upperBound - lowerBound) + lowerBound;
    }

    public int getIslandContainerSize() {
        return ISLAND_CONTAINER_SIZE;
    }
}
