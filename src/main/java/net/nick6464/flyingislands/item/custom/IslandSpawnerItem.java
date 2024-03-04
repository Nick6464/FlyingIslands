package net.nick6464.flyingislands.item.custom;

import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.nick6464.flyingislands.FlyingIslands;

public class IslandSpawnerItem extends Item {

    static int ISLAND_SIZE = 50;
    static int ISLAND_GROUND_HEIGHT = 50;
    static int ISLAND_RADIUS = ISLAND_SIZE / 2;
    static int ISLAND_CONTAINER_SIZE = (int) (ISLAND_SIZE * 1.5);
    static int ISLAND_CONTAINER_RADIUS = ISLAND_CONTAINER_SIZE / 2;
    static int SEED = 12345;
    static float FREQUENCY = 0.15f;
    static float GROUND_LAYER_MAGNITUDE = 2f;
    static float UNDERSIDE_MAGNITUDE = 2f;
    static int RADIAL_DETAIL_MULTIPLIER = Math.round((float) ISLAND_RADIUS /  3f);
    static int SMOOTHING_PASSES = 2;

    public IslandSpawnerItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if(context.getHand() == Hand.MAIN_HAND) {
        boolean[][][] blocks = generateIsland();

        placeBlocks(context, blocks);

        return ActionResult.SUCCESS;
        } else {
            return ActionResult.FAIL;
        }
    }

    public void placeBlocks(ItemUsageContext context, boolean[][][] blocks) {
        for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
            for (int y = 0; y < ISLAND_CONTAINER_SIZE; y++) {
                for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                    if (blocks[x][y][z]) {
                        context.getWorld().setBlockState(context.getBlockPos().add(x, y, z),
                                Blocks.STONE.getDefaultState(), 3);
                    }
                }
            }
        }
    }

    public static boolean[][][] generateIsland() {
        boolean[][] groundLayer = new boolean[ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE];
        boolean[][][] blocks = new boolean[ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE];

        // Generate the radial noise to create a circle
        groundLevelGenerator(groundLayer, blocks);

        undersideGenerator(groundLayer, blocks);

        return blocks;
    }

    public static void undersideGenerator(boolean[][] groundLayer, boolean[][][] blocks) {
        // Generate a 2d array that represents how far from an edge the block is
        float[][] underside = new float[ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE];

        // Loop over the groundLayer and set the underside array to the distance from the edge
        for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
            for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                // If there is a corresponding block in the groundLayer, determine its distance from the edge
                if (groundLayer[x][z]) {
                    underside[x][z] = distanceFromEdge(x, z, groundLayer);
                }
            }
        }

        // Loop over the underside array and place the value from the underside array in the 3D
        // array under the ground layer, a value of 4 means there are 4 blocks under the ground layer
        for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
            for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                // Ensure there is a block in the ground layer
                if (!groundLayer[x][z]) {
                    continue;
                }

                // Generate noise to add to the depth of the island
                float noise =
                        OpenSimplex2S.noise2(SEED, x * FREQUENCY, z * FREQUENCY) * UNDERSIDE_MAGNITUDE * (underside[x][z]/ 3);
                // Start at the ground level and go down to the underside depth then add a layer
                // of noise which can be positive or negative

                // Determine the depth of the underside and ensure it's within the valid range
                int depth = Math.round( ISLAND_GROUND_HEIGHT + 1 - (int) underside[x][z] + noise);

                if (depth < 0) {
                    depth = 0;
                }

                for (int y = ISLAND_GROUND_HEIGHT; y >= depth; y--) {
                    blocks[x][y][z] = true;
                }
            }
        }

    }

    public static float distanceFromEdge(int x, int z, boolean[][] groundLayer) {
        // Find the nearest value in the groundLayer that is false
        int distance = 0;
        while (true) {
            for (int i = -distance; i <= distance; i++) {
                if (x + i >= 0 && x + i < ISLAND_CONTAINER_SIZE && z - distance >= 0 && z - distance < ISLAND_CONTAINER_SIZE) {
                    if (!groundLayer[x + i][z - distance]) {
                        return distance;
                    }
                }
                if (x + i >= 0 && x + i < ISLAND_CONTAINER_SIZE && z + distance >= 0 && z + distance < ISLAND_CONTAINER_SIZE) {
                    if (!groundLayer[x + i][z + distance]) {
                        return distance;
                    }
                }
                if (z + i >= 0 && z + i < ISLAND_CONTAINER_SIZE && x - distance >= 0 && x - distance < ISLAND_CONTAINER_SIZE) {
                    if (!groundLayer[x - distance][z + i]) {
                        return distance;
                    }
                }
                if (z + i >= 0 && z + i < ISLAND_CONTAINER_SIZE && x + distance >= 0 && x + distance < ISLAND_CONTAINER_SIZE) {
                    if (!groundLayer[x + distance][z + i]) {
                        return distance;
                    }
                }
            }
            distance++;
        }
    }

    public static void groundLevelGenerator(boolean[][] groundLayer, boolean[][][] blocks){
        // Generate the radial noise to create a circle
        radialNoiseGenerator(groundLayer);

        // Smooth the hard corners of the circle
        smoothHardCorners(groundLayer);

        // Flood fill the island to make it solid
        floodFill(groundLayer, ISLAND_CONTAINER_RADIUS, ISLAND_CONTAINER_RADIUS);

        // Place the blocks in the 3D array from the 2D array
        for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
            for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                if (groundLayer[x][z]) {
                    blocks[x][ISLAND_GROUND_HEIGHT][z] = true;
                }
            }
        }
    }

    //Radial noise function to create a circle
    public static void radialNoiseGenerator(boolean[][] noisyCircle){
        // Use the block count in the circle to determine the number of blocks in a perfect circle
        int blockCount = RADIAL_DETAIL_MULTIPLIER * blocksInCircle();

        // Divide a perfect circle by the number of blocks in a circle, this is the number of
        // angles to use to generate the circle. A block will be placed at each angle, and 1D noise
        // will be added or subtracted to the distance from the center of the circle to the block.

        // Loop over every angle
        for (int angle = 0; angle < blockCount; angle++) {
            // Determine the distance from the center of the circle to the block
            float noise = OpenSimplex2S.noise2(SEED, 1, (double) angle / RADIAL_DETAIL_MULTIPLIER * FREQUENCY);
            double distance = ISLAND_RADIUS + noise * GROUND_LAYER_MAGNITUDE;

            // Determine the angle in radians
            double angleRads = angle * 2 * Math.PI / blockCount;

            // Calculate the x and z coordinates of the block
            int x = (int) Math.round(distance * Math.cos(angleRads));
            int z = (int) Math.round(distance * Math.sin(angleRads));

            // Adjust the coordinates to ensure they fall within the valid range of the array indices
            x += ISLAND_CONTAINER_RADIUS;
            z += ISLAND_CONTAINER_RADIUS;

            // Set the block at (x, z) to true if it's within the valid range
            if (x >= 0 && x < ISLAND_CONTAINER_SIZE && z >= 0 && z < ISLAND_CONTAINER_SIZE) {
                noisyCircle[x][z] = true;
                FlyingIslands.LOGGER.info("x: " + x + " z: " + z);
            } else {
                FlyingIslands.LOGGER.warn("x: " + x + " z: " + z + " is out of range");
            }
        }
    }

    // Flood fill algorithm to make the island solid
    public static void floodFill(boolean[][] blocks, int x, int z) {
        if (x < 0 || x >= ISLAND_CONTAINER_SIZE || z < 0 || z >= ISLAND_CONTAINER_SIZE) {
            return;
        }
        if (blocks[x][z]) {
            return;
        }
        blocks[x][z] = true;
        floodFill(blocks, x + 1, z);
        floodFill(blocks, x - 1, z);
        floodFill(blocks, x, z + 1);
        floodFill(blocks, x, z - 1);
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
    public static void smoothHardCorners(boolean[][] noisyCircle){
        for (int smoothing = 0; smoothing < SMOOTHING_PASSES; smoothing++){
            for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
                for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                    if (noisyCircle[x][z]) {
                        // Check if the block is a hard corner and set it to false if it is
                        // Ensure both the positive and negative are done to avoid a corner being missed

                        // Check if the coordinates would be in range
                        if (x - 2 < 0 || x + 2 >= ISLAND_CONTAINER_SIZE || z - 2 < 0 || z + 2 >= ISLAND_CONTAINER_SIZE) {
                            continue;
                        }


                        if (x - 2 > 0 && noisyCircle[x - 2][z] && noisyCircle[x - 1][z] && noisyCircle[x][z - 2] && noisyCircle[x][z - 1]) {
                            noisyCircle[x][z] = false;
                        }
                        if (x - 2 > 0 && noisyCircle[x - 2][z] && noisyCircle[x - 1][z] && noisyCircle[x][z + 2] && noisyCircle[x][z + 1]) {
                            noisyCircle[x][z] = false;
                        }
                        if (x + 2 < ISLAND_CONTAINER_SIZE && noisyCircle[x + 2][z] && noisyCircle[x + 1][z] && noisyCircle[x][z + 2] && noisyCircle[x][z + 1]) {
                            noisyCircle[x][z] = false;
                        }
                        if (x + 2 < ISLAND_CONTAINER_SIZE && noisyCircle[x + 2][z] && noisyCircle[x + 1][z] && noisyCircle[x][z - 2] && noisyCircle[x][z - 1]) {
                            noisyCircle[x][z] = false;
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
}
