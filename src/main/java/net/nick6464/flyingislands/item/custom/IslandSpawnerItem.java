package net.nick6464.flyingislands.item.custom;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.nick6464.flyingislands.FlyingIslands;

public class IslandSpawnerItem extends Item {

    static int ISLAND_SIZE = 30;
    static int ISLAND_GROUND_HEIGHT = 15;
    static int SEED = 12345;


    public IslandSpawnerItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {

        boolean[][][] blocks = generateIsland();

        placeBlocks(context, blocks);

        return ActionResult.SUCCESS;
    }

    public void placeBlocks(ItemUsageContext context, boolean[][][] blocks) {
        for (int x = 0; x < ISLAND_SIZE; x++) {
            for (int y = 0; y < ISLAND_SIZE; y++) {
                for (int z = 0; z < ISLAND_SIZE; z++) {
                    if (blocks[x][y][z]) {
                        context.getWorld().setBlockState(context.getBlockPos().add(x, y, z),
                                Blocks.STONE.getDefaultState(), 3);
                    }
                }
            }
        }
    }

    public static boolean[][][] generateIsland(){
        boolean[][][] blocks = new boolean[ISLAND_SIZE][ISLAND_SIZE][ISLAND_SIZE];

        boolean[][] noisyCircle = new boolean[ISLAND_SIZE][ISLAND_SIZE];
        for (int x = 0; x < ISLAND_SIZE; x++) {
            for (int z = 0; z < ISLAND_SIZE; z++) {
                double distance = Math.sqrt(Math.pow(x - ISLAND_GROUND_HEIGHT, 2) + Math.pow(z - ISLAND_GROUND_HEIGHT, 2));
                double noise = OpenSimplex2S.noise2_ImproveX(SEED, x * 0.625, z * 0.625);

                noisyCircle[x][z] = distance + noise < ISLAND_GROUND_HEIGHT;
                blocks[x][ISLAND_GROUND_HEIGHT][z] = noisyCircle[x][z];

                int xFromCenter = Math.abs(ISLAND_SIZE / 2 - x);
                int zFromCenter = Math.abs(ISLAND_SIZE / 2 - z);

                float xEuclidean = (float) xFromCenter / ((float) ISLAND_SIZE / 2);
                float zEuclidean = (float) zFromCenter / ((float) ISLAND_SIZE / 2);

                // Calculate the Euclidean distance
                double perfectUnderside = euclidean(xEuclidean, zEuclidean);

                // Adjust perfectUnderside to ensure it's within islandGroundHeight bounds
                perfectUnderside *= ISLAND_GROUND_HEIGHT;

                perfectUnderside += noise;

                if (perfectUnderside > ISLAND_GROUND_HEIGHT) {
                    perfectUnderside = ISLAND_GROUND_HEIGHT;
                }

                if (noisyCircle[x][z]) {
                    FlyingIslands.LOGGER.info("Height noise at " + xEuclidean + ", " + zEuclidean + " is " + perfectUnderside);
                }

                // Add noise to the underside of the island
                if (perfectUnderside > 0) {
                    for (int y = ISLAND_GROUND_HEIGHT - (int) perfectUnderside; y < ISLAND_GROUND_HEIGHT; y++) {
                        blocks[x][y][z] = true;
                    }
                }

            }
        }

        return blocks;
    }

    // Euclidean function
    public static double euclidean(double x, double y) {
        return 1 - x * x - y * y;
    }
}
