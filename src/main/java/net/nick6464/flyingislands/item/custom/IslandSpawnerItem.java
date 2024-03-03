package net.nick6464.flyingislands.item.custom;

import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.nick6464.flyingislands.FlyingIslands;

public class IslandSpawnerItem extends Item {

    static int ISLAND_SIZE = 30;
    static int ISLAND_RADIUS = ISLAND_SIZE / 2;
    static int ISLAND_CONTAINER_SIZE = (int) (ISLAND_SIZE * 1.5);
    static int ISLAND_CONTAINER_RADIUS = ISLAND_CONTAINER_SIZE / 2;
    static int ISLAND_GROUND_HEIGHT = 15;
    static int SEED = 12345;
    static float FREQUENCY = 0.1f;
    static float MAGNITUDE = 2f;
    static int DETAIL = ISLAND_RADIUS /  3;


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
        boolean[][][] blocks = new boolean[ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE];
        boolean[][] noisyCircle = new boolean[ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE];

        // Use the block count in the circle to determine the number of blocks in a perfect circle
        int blockCount = DETAIL * blocksInCircle();

        // Divide a perfect circle by the number of blocks in a circle, this is the number of
        // angles to use to generate the circle. A block will be placed at each angle, and 1D noise
        // will be added or subtracted to the distance from the center of the circle to the block.

        // Loop over every angle
        for (int angle = 0; angle < blockCount; angle++) {
            // Determine the distance from the center of the circle to the block
            float noise = OpenSimplex2S.noise2(SEED, 1, (double) angle / DETAIL * FREQUENCY);
            double distance = ISLAND_RADIUS + noise * MAGNITUDE;

            // Determine the angle in radians
            double angleRads = angle * 2 * Math.PI / blockCount;

            // Calculate the x and z coordinates of the block
            int x = (int) Math.round(distance * Math.cos(angleRads));
            int z = (int) Math.round(distance * Math.sin(angleRads));

            FlyingIslands.LOGGER.info("x: " + x + " z: " + z);

            // Adjust the coordinates to ensure they fall within the valid range of the array indices
            x += ISLAND_CONTAINER_RADIUS;
            z += ISLAND_CONTAINER_RADIUS;

            // Set the block at (x, z) to true if it's within the valid range
            if (x >= 0 && x < ISLAND_CONTAINER_SIZE && z >= 0 && z < ISLAND_CONTAINER_SIZE) {
                noisyCircle[x][z] = true;
            }
        }

        // Remove the har corners of the circle
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

        for (int smoothing = 0; smoothing < 2; smoothing++){
            for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
                for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                    if (noisyCircle[x][z]) {
                        // Check if the block is a hard corner and set it to false if it is
                        // Ensure both the positive and negative are done to avoid a corner being missed
                        if (x - 2 > 0 && noisyCircle[x - 2][z] && noisyCircle[x - 1][z] && noisyCircle[x][z - 2] && noisyCircle[x][z - 1]) {
                            noisyCircle[x][z] = false;
                        }
                        if (x + 2 < ISLAND_CONTAINER_SIZE && noisyCircle[x + 2][z] && noisyCircle[x + 1][z] && noisyCircle[x][z + 2] && noisyCircle[x][z + 1]) {
                            noisyCircle[x][z] = false;
                        }
                        if (x - 2 > 0 && noisyCircle[x - 2][z] && noisyCircle[x - 1][z] && noisyCircle[x][z + 2] && noisyCircle[x][z + 1]) {
                            noisyCircle[x][z] = false;
                        }
                        if (x + 2 < ISLAND_CONTAINER_SIZE && noisyCircle[x + 2][z] && noisyCircle[x + 1][z] && noisyCircle[x][z - 2] && noisyCircle[x][z - 1]) {
                            noisyCircle[x][z] = false;
                        }
                    }
                }
            }
        }

        // Add the noisy circle to the blocks array at the ground height
        for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
            for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                if (noisyCircle[x][z]) {
                    blocks[x][ISLAND_GROUND_HEIGHT][z] = true;
                }
            }
        }

        return blocks;
    }

    // This method should return the number of blocks in a perfect circle based on the ISLAND_SIZE
    public static int blocksInCircle() {
        // The blocks can be calculated by diameter - 1 * 4
        return (ISLAND_SIZE - 1) * 4;
    }
}
