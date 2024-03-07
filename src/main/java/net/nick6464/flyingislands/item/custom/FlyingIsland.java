package net.nick6464.flyingislands.item.custom;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.foliage.BlobFoliagePlacer;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.trunk.StraightTrunkPlacer;
import net.nick6464.flyingislands.FlyingIslands;

import java.util.Random;


public class FlyingIsland extends GroundLayer {
    private ItemUsageContext context;
    private int SEED;
    private Random random;
    private final boolean[][][] shape;
    private final boolean[][][] water;
    private final Block[][][] blocks;
    private boolean[][] trees;
    static int ISLAND_SIZE = 50;
    static int ISLAND_GROUND_HEIGHT = (int) (ISLAND_SIZE / 1.8);
    static int ISLAND_CONTAINER_SIZE = (int) (ISLAND_SIZE * 1.5);
    static double FREQUENCY = 0.12f;
    static float UNDERSIDE_MAGNITUDE = 2f;
    static float TOPSIDE_MAGNITUDE = 2f;
    private float[][] topside;
    private float[][] topsideNoise;
    static float DIRT_MAGNITUDE = 1.5f;
    static float DIRT_FREQUENCY = 0.5f;
    GroundLayer groundLayer = new GroundLayer(ISLAND_CONTAINER_SIZE, ISLAND_SIZE, SEED);

    public FlyingIsland(int seed, ItemUsageContext context) {
        super(ISLAND_CONTAINER_SIZE, ISLAND_SIZE, seed);
        this.context = context;
        this.SEED = seed;
        random = new Random(SEED);
        this.shape = new boolean[ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE];
        this.water = new boolean[ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE];
        this.blocks = new Block[ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE];
        this.trees = new boolean[ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE];
        this.topside = new float[ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE];
        this.topsideNoise = new float[ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE];
    }

    public void generateIsland() {
        // Generate the shape of the island
        generateGroundLayer();
        undersideGenerator();
        topsideGenerator();

        // Add water to the island and generate lakes and rivers
        lakeGenerator();

        // Generates the blocks according to desired biome along with foliage and trees
        blockPopulator();


        treeDecorator();


    }

    // ------------------------------- PLACER AND DELETER -------------------------------

    public void placeIsland(World world, BlockPos pos) {
        for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
            for (int y = 0; y < ISLAND_CONTAINER_SIZE; y++) {
                for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                    if (blocks[x][y][z] != null && blocks[x][y][z] != Blocks.AIR) {
                            world.setBlockState(pos.add(x, y, z), blocks[x][y][z].getDefaultState(), 3);
                    }
                }
            }
        }
    }

    public void deleteIsland(World world, BlockPos pos) {
        for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
            for (int y = 0; y < ISLAND_CONTAINER_SIZE; y++) {
                for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                    Block block = world.getBlockState(pos.add(x, y, z)).getBlock();
                    if (block != Blocks.AIR) {
                        world.removeBlock(pos.add(x, y, z), false);
                        if(block.getDefaultState() != null && block.getDefaultState().getBlock() != Blocks.AIR)
                            world.setBlockState(pos.add(x, y, z), Blocks.AIR.getDefaultState(), 3);
                    }
                }
            }
        }
    }

    // ------------------------------- STYLE AND DECORATORS -------------------------------

    public void treeDecorator() {
        // Use the Feature class to generate a tree
        TreeFeatureConfig config = new TreeFeatureConfig.Builder(
                BlockStateProvider.of(Blocks.OAK_LOG.getDefaultState()),
                new StraightTrunkPlacer(5, 2, 0),
                BlockStateProvider.of(Blocks.OAK_LEAVES.getDefaultState()),
                new BlobFoliagePlacer(ConstantIntProvider.create(2), ConstantIntProvider.create(0), 3),
                new TwoLayersFeatureSize(10, 10, 12)).build();

        TreeFeature treeFeature = new TreeFeature(Codec.unit(() -> config));

        for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
            for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                for (int y = 0; y < ISLAND_CONTAINER_SIZE; y++) {
                    if (shape[x][y][z] && blocks[x][y][z] == Blocks.GRASS_BLOCK) {
                        if (Math.abs(OpenSimplex2S.noise2(SEED, x, z))> 0.75 && distanceToNeighbour(trees, x, z, 3) == -1) {
                            trees[x][z] = true;
                            treeFeature.generateIfValid(config, (StructureWorldAccess) context.getWorld(),
                                    null,
                                    context.getWorld().getRandom(),
                                    new BlockPos(context.getBlockPos().getX() + x, context.getBlockPos().getY() + y, context.getBlockPos().getZ() + z));
                        }
                    }
                }
            }
        }
    }

    public void blockPopulator() {
        for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
            for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                for (int y = 0; y < ISLAND_CONTAINER_SIZE; y++) {
                    if(water[x][y][z]) {
                        blocks[x][y][z] = Blocks.WATER;
                    }
                    else if (shape[x][y][z]) {
                        if (distanceToSurface(x, y, z) <= (OpenSimplex2S.noise2(SEED,
                                x * DIRT_FREQUENCY, z * DIRT_FREQUENCY) + 3) * DIRT_MAGNITUDE) {
                            if (isSurface(x, y, z)) {
                                blocks[x][y][z] = Blocks.GRASS_BLOCK;
                            }
                            else {
                                blocks[x][y][z] = Blocks.DIRT;
                            }
                        }
                        else if (shape[x][y][z]) {
                            blocks[x][y][z] = Blocks.STONE;
                        }
                        // Place air if there is no block
                        else {
                            blocks[x][y][z] = Blocks.AIR;
                        }
                    }
                }
            }
        }
    }

    // ------------------------------- WATER FILLER -------------------------------
    // A recursive function that looks at each block surrounding the current block
    // and checks if the noise value is less than -0.25, if so, it will remove the blocks down to the
    // height value below the ground layer, then do the same for the blocks surrounding the
    // current block
    public void lakeCreator(int x, int z) {
        float noise =
                (OpenSimplex2S.noise2(SEED, x * FREQUENCY / 5, z * FREQUENCY / 5) +  0.6f) * 2;
        int height = (int) (ISLAND_GROUND_HEIGHT + noise);
        if ((x < 0 || x >= ISLAND_CONTAINER_SIZE || z < 0 ||
                z >= ISLAND_CONTAINER_SIZE))
            return;

        if(!shape[x][height][z] ||
                water[x][height][z])
            return;

        if (noise < -0.1f) {
            for (int y = height; y <= ISLAND_GROUND_HEIGHT; y++) {
                shape[x][y][z] = false;
            }
            lakeCreator(x + 1, z);
            lakeCreator(x - 1, z);
            lakeCreator(x, z + 1);
            lakeCreator(x, z - 1);
        }
    }

    public void lakeGenerator() {
        // Check if the island should have a lake
        boolean generateLake = generateRandomNumber(0, 1) == 1;

        int lowestX = ISLAND_CONTAINER_SIZE / 2;
        int lowestZ = ISLAND_CONTAINER_SIZE / 2;

        if (generateLake || SEED == 192) {
            // Search the topside array for the lowest value that is lower than 0 and is more than 3
            // blocks away from the edge
            float lowest = 10f;

            for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
                for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                    if (topsideNoise[x][z] < lowest && topsideNoise[x][z] < 0f && groundLayer.getBlock(x,
                            z) && distanceFromEdge(x, z) > 3) {
                        lowest = topsideNoise[x][z];
                        lowestX = x;
                        lowestZ = z;
                    }
                }
            }

            lakeCreator(lowestX, lowestZ);

            // Use flood fill in 3 dimensions to fill the water layer
            floodFill3D(lowestX, ISLAND_GROUND_HEIGHT, lowestZ);
        }
    }

    // 3D Flood Fill algorithm to fill the water layer
    public void floodFill3D(int x, int y, int z) {
        if (x < 0 || x >= ISLAND_CONTAINER_SIZE || y < 0 || y > ISLAND_GROUND_HEIGHT || z < 0 || z >= ISLAND_CONTAINER_SIZE) {
            return;
        }

        if (!groundLayer.getBlock(x, z) || shape[x][y][z] || water[x][y][z]) {
            return;
        }
        
        water[x][y][z] = true;
        floodFill3D(x + 1, y, z);
        floodFill3D(x - 1, y, z);
        floodFill3D(x, y + 1, z);
        floodFill3D(x, y - 1, z);
        floodFill3D(x, y, z + 1);
        floodFill3D(x, y, z - 1);
    }


    // ------------------------------- TOPSIDE GENERATOR -------------------------------
    public void topsideGenerator() {
        // Generate a 2d array that represents how far from an edge the block is

        // Loop over the groundLayer and set the topside array to the distance from the edge
        for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
            for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                // If there is a corresponding block in the groundLayer, determine its distance from the edge
                if (groundLayer.getBlock(x, z)) {
                    topside[x][z] = distanceFromEdge(x, z);
                }
            }
        }

        // Loop over the topside array and place the value from the topside array in the 3D
        // array above the ground layer, a value of 4 means there are 4 blocks above the ground layer
        for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
            for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                // Ensure there is a block in the ground layer
                if (!groundLayer.getBlock(x, z)) {
                    continue;
                }
                // Once the topside value is more than 3, the value should be set to 3
                int edgeMultiplier = topside[x][z] > 3 ? 3 : (int) topside[x][z];

                // Generate noise to add to the height of the island
                float noise =
                        (OpenSimplex2S.noise2(SEED, x * FREQUENCY / 5, z * FREQUENCY / 5) + 0.6f) * (TOPSIDE_MAGNITUDE * edgeMultiplier / 3);

                topsideNoise[x][z] = noise;
                // Since height can be negative, start at the height, and go towards the ground layer
                // A negative height will set the blocks at that position to false
                int height = (int) (ISLAND_GROUND_HEIGHT + noise);
                if (noise < 0 && topside[x][z] > 3) {
                    for (int y = height + 1; y <= ISLAND_GROUND_HEIGHT; y++) {
                        shape[x][y][z] = false;
                    }
                } else {
                    for (int y = ISLAND_GROUND_HEIGHT; y <= height; y++) {
                        shape[x][y][z] = true;
                    }
                }
            }
        }
    }


    // ------------------------------- UNDERSIDE GENERATOR -------------------------------
    public void undersideGenerator() {
        // Generate a 2d array that represents how far from an edge the block is
        float[][] underside = new float[ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE];

        // Loop over the groundLayer and set the underside array to the distance from the edge
        for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
            for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                // If there is a corresponding block in the groundLayer, determine its distance from the edge
                if (groundLayer.getBlock(x, z)) {
                    underside[x][z] = distanceFromEdge(x, z);
                }
            }
        }

        // Loop over the underside array and place the value from the underside array in the 3D
        // array under the ground layer, a value of 4 means there are 4 blocks under the ground layer
        for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
            for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                // Ensure there is a block in the ground layer
                if (!groundLayer.getBlock(x, z)) {
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
                    shape[x][y][z] = true;
                }
            }
        }

    }

    // ------------------------------- UTILITY FUNCTIONS -------------------------------

    public float distanceFromEdge(int x, int z) {
        // Find the nearest value in the groundLayer that is false
        int distance = 0;
        while (true) {
            for (int i = -distance; i <= distance; i++) {
                if (x + i >= 0 && x + i < ISLAND_CONTAINER_SIZE && z - distance >= 0 && z - distance < ISLAND_CONTAINER_SIZE) {
                    if (!groundLayer.getBlock(x + i, z - distance)) {
                        return distance;
                    }
                }
                if (x + i >= 0 && x + i < ISLAND_CONTAINER_SIZE && z + distance >= 0 && z + distance < ISLAND_CONTAINER_SIZE) {
                    if (!groundLayer.getBlock(x + i, z + distance)) {
                        return distance;
                    }
                }
                if (z + i >= 0 && z + i < ISLAND_CONTAINER_SIZE && x - distance >= 0 && x - distance < ISLAND_CONTAINER_SIZE) {
                    if (!groundLayer.getBlock(x - distance, z + i)) {
                        return distance;
                    }
                }
                if (z + i >= 0 && z + i < ISLAND_CONTAINER_SIZE && x + distance >= 0 && x + distance < ISLAND_CONTAINER_SIZE) {
                    if (!groundLayer.getBlock(x + distance, z + i)) {
                        return distance;
                    }
                }
            }
            distance++;
        }
    }

    // Takes the coordinates of a block and returns the distance to the surface of the island
    // If the block is on the surface, the distance will be 0
    // The surface is a block in the shape array that itself is true has a false value in the y
    // value above it
    public int distanceToSurface(int x, int y, int z) {
        int distance = 0;
        while (y + distance < ISLAND_CONTAINER_SIZE) {
            if (shape[x][y + distance][z]) {
                distance++;
            } else
                return distance;
        }
        return -1;
    }

    // Does a block have an air block above it
    public boolean isSurface(int x, int y, int z) {
        return shape[x][y][z] && !shape[x][y + 1][z];
    }

    // Gets the next random number based on the seed
    public int generateRandomNumber(int lowerBound, int upperBound) {
        return random.nextInt(upperBound - lowerBound + 1) + lowerBound;
    }

    public int distanceToNeighbour(boolean[][] layer, int x, int z, int searchDistance) {
        int distance = 0;
        while (distance < searchDistance) {
            for (int i = -distance; i <= distance; i++) {
                if (x + i >= 0 && x + i < ISLAND_CONTAINER_SIZE && z - distance >= 0 && z - distance < ISLAND_CONTAINER_SIZE) {
                    if (layer[x + i][z - distance]) {
                        return distance;
                    }
                }
                if (x + i >= 0 && x + i < ISLAND_CONTAINER_SIZE && z + distance >= 0 && z + distance < ISLAND_CONTAINER_SIZE) {
                    if (layer[x + i][z + distance]) {
                        return distance;
                    }
                }
                if (z + i >= 0 && z + i < ISLAND_CONTAINER_SIZE && x - distance >= 0 && x - distance < ISLAND_CONTAINER_SIZE) {
                    if (layer[x - distance][z + i]) {
                        return distance;
                    }
                }
                if (z + i >= 0 && z + i < ISLAND_CONTAINER_SIZE && x + distance >= 0 && x + distance < ISLAND_CONTAINER_SIZE) {
                    if (layer[x + distance][z + i]) {
                        return distance;
                    }
                }
            }
            distance++;
        }
        return -1;
    }
}
