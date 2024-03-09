package net.nick6464.flyingislands.item.custom;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.nick6464.flyingislands.FlyingIslands;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;


public class FlyingIsland extends GroundLayer {
    public static int SEED;
    ItemUsageContext context;
    static boolean[][][] shape = new boolean[0][][];
    private static boolean[][][] water = new boolean[0][][];
    final Block[][][] blocks;
    protected boolean[][] trees;
    int ISLAND_GROUND_HEIGHT;
    double UNDERSIDE_FREQUENCY;
    float UNDERSIDE_MAGNITUDE;
    float UNDERSIDE_STEEPNESS;
    float TOPSIDE_MAGNITUDE;
    double TOPSIDE_FREQUENCY;
    float TOPSIDE_OFFSET;
    private final float[][] topside;
    private final float[][] topsideNoise;
    static float DIRT_MAGNITUDE = 1.5f;
    static float DIRT_FREQUENCY = 0.5f;

    GroundLayer groundLayer = new GroundLayer(SEED);

    public FlyingIsland(int seed, ItemUsageContext context) {
        super(seed);

        SEED = seed;

        this.context = context;
        shape = new boolean[ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE];
        water = new boolean[ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE];
        this.blocks = new Block[ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE];
        this.trees = new boolean[ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE];
        this.topside = new float[ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE];
        this.topsideNoise = new float[ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE];

        float topMagnitudeMultiplyer =  ISLAND_SIZE / 10f;
        ISLAND_GROUND_HEIGHT = (int) (ISLAND_SIZE / 1.8);

        TOPSIDE_MAGNITUDE = generateRandomFloat(topMagnitudeMultiplyer / 10,
                topMagnitudeMultiplyer);
        TOPSIDE_FREQUENCY = generateRandomFloat(0.01f, 0.05f);
        TOPSIDE_OFFSET = TOPSIDE_MAGNITUDE;

        UNDERSIDE_FREQUENCY = generateRandomFloat(0.5f, 1f);
        UNDERSIDE_MAGNITUDE = generateRandomFloat(0.2f, 1.2f);
        UNDERSIDE_STEEPNESS = generateRandomFloat(0.5f, 1.5f);

    }

    public void generateIsland() {
        // Generate the shape of the island
        context.getPlayer().sendMessage(Text.of("SEED: " + SEED), false);
        context.getPlayer().sendMessage(Text.of("Topside Magnitude: " + TOPSIDE_MAGNITUDE), false);
        context.getPlayer().sendMessage(Text.of("Topside Frequency: " + TOPSIDE_FREQUENCY), false);
        context.getPlayer().sendMessage(Text.of("Topside Offset: " + TOPSIDE_OFFSET), false);
        context.getPlayer().sendMessage(Text.of("Underside Magnitude: " + UNDERSIDE_MAGNITUDE), false);
        context.getPlayer().sendMessage(Text.of("Underside Frequency: " + UNDERSIDE_FREQUENCY), false);
        context.getPlayer().sendMessage(Text.of("Island Ground Height: " + ISLAND_GROUND_HEIGHT), false);
        context.getPlayer().sendMessage(Text.of("Island Size: " + ISLAND_SIZE), false);
        context.getPlayer().sendMessage(Text.of("Island Container Size: " + ISLAND_CONTAINER_SIZE), false);
        context.getPlayer().sendMessage(Text.of("Island Container Radius: " + ISLAND_CONTAINER_RADIUS), false);
        context.getPlayer().sendMessage(Text.of("Radial Detail Multiplier: " + RADIAL_DETAIL_MULTIPLIER), false);
        context.getPlayer().sendMessage(Text.of("Ground Frequency: " + GROUNDLAYER_FREQUENCY),
                false);
        context.getPlayer().sendMessage(Text.of("Ground Magnitude: " + GROUNDLAYER_MAGNITUDE),
                false);

        generateGroundLayer();

        // Insert the ground layer into the shape array
//        for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
//            for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
//                shape[x][ISLAND_GROUND_HEIGHT][z] = groundLayer.getBlock(x, z);
//            }
//        }

        undersideGenerator();
        topsideGenerator();

        // Add water to the island and generate lakes and rivers
        lakeGenerator();

        // Generates the blocks according to desired biome along with foliage and trees
        blockPopulator();

        // Smoothing the edges of the island
        blockSmoothing();

        sandDecorator();


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
                    // If the block is at 0, 0, 0, don't remove it
                    if (pos.getX() == 0 && pos.getY() == 0 && pos.getZ() == 0) {
                        continue;
                    }

                    if (block != Blocks.AIR) {
                        world.removeBlock(pos.add(x, y, z), false);
                        if(block.getDefaultState() != null && block.getDefaultState().getBlock() != Blocks.AIR)
                            world.setBlockState(pos.add(x, y, z), Blocks.AIR.getDefaultState(), 3);
                    }
                }
            }
        }
    }

    // ------------------------------- BLOCK POPULATOR -------------------------------

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
                            continue;
                        }
                        blocks[x][y][z] = Blocks.STONE;
                    }
                }
            }
        }
    }

    // ------------------------------- BLOCK SMOOTHING -------------------------------
    // If a Grass or Dirt block has 3 or more blocks of air around it, set it to air
    public void blockSmoothing() {
        int passes = 3;

        int blocksRemoved = 0;
        for (int pass = 0; pass < passes; pass++) {
            for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
                for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                    for (int y = 0; y < ISLAND_CONTAINER_SIZE; y++) {
                        if (blocks[x][y][z] == Blocks.GRASS_BLOCK || blocks[x][y][z] == Blocks.DIRT) {
                            int touchingBlocks = getTouchingAirBlocks(x, y, z);
                            if(touchingBlocks >= 4){
                                blocks[x][y][z] = Blocks.AIR;
                                blocksRemoved++;
                            }
                        }
                        if(blocks[x][y][z] == Blocks.STONE) {
                            int touchingBlocks = getTouchingAirBlocks(x, y, z);
                            if(touchingBlocks == 6) {
                                blocks[x][y][z] = Blocks.AIR;
                                blocksRemoved++;
                            }
                        }
                    }
                }
            }
        }
        FlyingIslands.LOGGER.info("Blocks Removed: " + blocksRemoved);
    }

    private boolean airAbove(int x, int y, int z) {
        if(blocks[x][y + 1][z] == null)
            return true;
        return blocks[x][y + 1][z].getDefaultState() == Blocks.AIR.getDefaultState();
    }

    private int getTouchingAirBlocks(int x, int y, int z) {
        int touchingBlocks = 0;
        if(blocks[x][y][z + 1] == null || blocks[x][y][z + 1].getDefaultState() == Blocks.AIR.getDefaultState()){
            touchingBlocks++;
        }
        if(blocks[x][y][z - 1] == null || blocks[x][y][z - 1].getDefaultState() == Blocks.AIR.getDefaultState()){
            touchingBlocks++;
        }
        if(blocks[x + 1][y][z] == null || blocks[x + 1][y][z].getDefaultState() == Blocks.AIR.getDefaultState()){
            touchingBlocks++;
        }
        if(blocks[x - 1][y][z] == null || blocks[x - 1][y][z].getDefaultState() == Blocks.AIR.getDefaultState()){
            touchingBlocks++;
        }
        if(blocks[x][y + 1][z] == null || blocks[x][y + 1][z].getDefaultState() == Blocks.AIR.getDefaultState()){
            touchingBlocks++;
        }
        return touchingBlocks;
    }

    // ------------------------------- WATER FILLER -------------------------------
    // A recursive function that looks at each block surrounding the current block
    // and checks if the noise value is less than -0.25, if so, it will remove the blocks down to the
    // height value below the ground layer, then do the same for the blocks surrounding the
    // current block
    public void lakeCreator(int x, int z) {

        float noise =
                (OpenSimplex2S.noise2(SEED, x * TOPSIDE_FREQUENCY, z * TOPSIDE_FREQUENCY)) * TOPSIDE_MAGNITUDE * 2;
        int height = (int) (ISLAND_GROUND_HEIGHT + noise);

        if ((x < 0 || x >= ISLAND_CONTAINER_SIZE || z < 0 || z >= ISLAND_CONTAINER_SIZE))
            return;

        if(!shape[x][height][z] || water[x][height][z])
            return;

        FlyingIslands.LOGGER.info("Noise: " + noise);

        if (noise < -0.1f) {
            for (int y = height; y <= ISLAND_GROUND_HEIGHT; y++) {
                shape[x][y][z] = false;
                water[x][y][z] = true;
            }
            lakeCreator(x + 1, z);
            lakeCreator(x - 1, z);
            lakeCreator(x, z + 1);
            lakeCreator(x, z - 1);
        }
    }

    public void lakeGenerator() {
        int lowestX = ISLAND_CONTAINER_SIZE / 2;
        int lowestZ = ISLAND_CONTAINER_SIZE / 2;

            // Search the topside array for the lowest value that is lower than 0 and is more than 3
            // blocks away from the edge
            float lowest = 9999f;

            for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
                for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                    if (topsideNoise[x][z] < lowest && groundLayer.getBlock(x, z) && distanceFromEdge(x, z) > 3) {
                        lowest = topsideNoise[x][z];
                        lowestX = x;
                        lowestZ = z;
                    }
                }
            }

            FlyingIslands.LOGGER.info("Lowest: " + lowest);

            lakeCreator(lowestX, lowestZ);

            // Use flood fill in 3 dimensions to fill the water layer
            floodFill3D(lowestX, ISLAND_GROUND_HEIGHT, lowestZ);

        // Generate rivers
        riverGenerator();
    }

    public void riverGenerator() {
        boolean genRiver = generateRandomNumber(0, 1) == 1;

        if(!genRiver)
            return;

        // Find the furthest block of water away from the center of the island
        int furthestX = 0;
        int furthestZ = 0;
        int furthestDistance = 0;

        for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
            for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                if (water[x][ISLAND_GROUND_HEIGHT][z]) {
                    int distance = (int) Math.sqrt(Math.pow(x - (double) ISLAND_CONTAINER_SIZE / 2, 2) + Math.pow(z - (double) ISLAND_CONTAINER_SIZE / 2, 2));
                    if (distance > furthestDistance) {
                        furthestDistance = distance;
                        furthestX = x;
                        furthestZ = z;
                    }
                }
            }
        }

        // Generate a river from the furthest block of water outwards
        riverCreator(furthestX, furthestZ);
    }

    public void riverCreator(int x, int z) {
        //
    }

    // 3D Flood Fill algorithm to fill the water layer
    public void floodFill3D(int startX, int startY, int startZ) {
        // Create a stack to keep track of the points to be filled
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{startX, startY, startZ});

        while (!stack.isEmpty()) {
            int[] current = stack.pop();
            int x = current[0];
            int y = current[1];
            int z = current[2];

            if (x < 0 || x >= ISLAND_CONTAINER_SIZE || y < 0 || y >= ISLAND_GROUND_HEIGHT || z < 0 || z >= ISLAND_CONTAINER_SIZE) {
                continue;
            }

            if (!groundLayer.getBlock(x, z) || shape[x][y][z] || water[x][y][z]) {
                continue;
            }

            water[x][y][z] = true;

            // Add neighboring points to the stack
            stack.push(new int[]{x + 1, y, z});
            stack.push(new int[]{x - 1, y, z});
            stack.push(new int[]{x, y + 1, z});
            stack.push(new int[]{x, y - 1, z});
            stack.push(new int[]{x, y, z + 1});
            stack.push(new int[]{x, y, z - 1});
        }
    }

    public void sandDecorator() {
        int[] waterLocation = hasWater();
        if (waterLocation == null)
            return;

        for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
            for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                for (int y = 0; y < ISLAND_CONTAINER_SIZE; y++) {
                    if((distanceToNeighbour(Blocks.WATER, x, y, z, 3) != -1 ||
                            isTouchingWater(x, y, z)) && blocks[x][y][z] == Blocks.GRASS_BLOCK) {
                        if(notFloating(x, y, z))
                            blocks[x][y][z] = Blocks.SAND;
                        else
                            blocks[x][y][z] = Blocks.CLAY;
                    }
                }
            }
        }
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
                        (OpenSimplex2S.noise2(SEED, x * TOPSIDE_FREQUENCY, z * TOPSIDE_FREQUENCY)) *
                                (TOPSIDE_MAGNITUDE);

                topsideNoise[x][z] = noise;
                // Since height can be negative, start at the height, and go towards the ground layer
                // A negative height will set the blocks at that position to false
                int height = (int) (ISLAND_GROUND_HEIGHT + noise);
                if (noise < 0) {
                    for (int y = height + 1; y <= ISLAND_GROUND_HEIGHT + TOPSIDE_OFFSET; y++) {
                        shape[x][y][z] = false;
                    }
                } else {
                    for (int y = (int) (ISLAND_GROUND_HEIGHT + TOPSIDE_OFFSET); y <= height; y++) {
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
                boolean block = groundLayer.getBlock(x, z);

                if (groundLayer.getBlock(x, z)) {
                    underside[x][z] = distanceFromEdge(x, z) * UNDERSIDE_STEEPNESS;
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
                        OpenSimplex2S.noise2(SEED, x * UNDERSIDE_FREQUENCY,
                                z * UNDERSIDE_FREQUENCY) * UNDERSIDE_MAGNITUDE * (underside[x][z] / 3);
                // Start at the ground level and go down to the underside depth then add a layer
                // of noise which can be positive or negative

                // Determine the depth of the underside and ensure it's within the valid range
                int depth = Math.round( ISLAND_GROUND_HEIGHT - (int) underside[x][z] + noise);


                if (depth < 0) {
                    depth = 0;
                }

                for (int y = ISLAND_GROUND_HEIGHT; y >= depth; y--) {
                    shape[x][y][z] = true;
                }
            }
        }
        Objects.requireNonNull(context.getPlayer()).sendMessage(Text.of("Completed"), false);

    }

    // ------------------------------- UTILITY FUNCTIONS -------------------------------

    public float distanceFromEdge(int x, int z) {
        for (int distance = 0; distance < ISLAND_CONTAINER_SIZE; distance++) {
            boolean found = false;
            for (int i = -distance; i <= distance; i++) {
                if (x + i >= 0 && x + i < ISLAND_CONTAINER_SIZE && z - distance >= 0 && z - distance < ISLAND_CONTAINER_SIZE) {
                    if (!groundLayer.getBlock(x + i, z - distance)) {
                        return distance;
                    }
                    found = true;
                }
                if (x + i >= 0 && x + i < ISLAND_CONTAINER_SIZE && z + distance >= 0 && z + distance < ISLAND_CONTAINER_SIZE) {
                    if (!groundLayer.getBlock(x + i, z + distance)) {
                        return distance;
                    }
                    found = true;
                }
                if (z + i >= 0 && z + i < ISLAND_CONTAINER_SIZE && x - distance >= 0 && x - distance < ISLAND_CONTAINER_SIZE) {
                    if (!groundLayer.getBlock(x - distance, z + i)) {
                        return distance;
                    }
                    found = true;
                }
                if (z + i >= 0 && z + i < ISLAND_CONTAINER_SIZE && x + distance >= 0 && x + distance < ISLAND_CONTAINER_SIZE) {
                    if (!groundLayer.getBlock(x + distance, z + i)) {
                        return distance;
                    }
                    found = true;
                }
            }
            // If nothing is found at this distance, break out of the loop
            if (!found) {
                break;
            }
        }
        return -1;
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

    Block getBlock(int x, int y, int z) {
        return blocks[x][y][z];
    }

    // Give Coordinates to a block and checks if there is any water around it
    static boolean isTouchingWater(int x, int y, int z) {
        // Split in individual checks to avoid going outside the array size
        if (x + 1 < ISLAND_CONTAINER_SIZE && getWater(x + 1, y, z)) {
            return true;
        }
        if (x - 1 >= 0 && getWater(x - 1, y, z)) {
            return true;
        }
        if (z + 1 < ISLAND_CONTAINER_SIZE && getWater(x, y, z + 1)) {
            return true;
        }
        if (z - 1 >= 0 && getWater(x, y, z - 1)) {
            return true;
        }
        return y + 1 < ISLAND_CONTAINER_SIZE && getWater(x, y + 1, z);
    }

    private static boolean getWater(int x, int y, int z) {
        return water[x][y][z];
    }

    static boolean notFloating(int x, int y, int z) {
        return shape[x][y - 1][z];
    }

    public int distanceToNeighbour(Block blockType, int x, int y, int z, int searchDistance){
        int distance = 0;
        while (distance < searchDistance) {
            for (int i = -distance; i <= distance; i++) {
                if (x + i >= 0 && x + i < ISLAND_CONTAINER_SIZE && z - distance >= 0 && z - distance < ISLAND_CONTAINER_SIZE) {
                    if (blocks[x + i][y][z - distance] == blockType) {
                        return distance;
                    }
                }
                if (x + i >= 0 && x + i < ISLAND_CONTAINER_SIZE && z + distance >= 0 && z + distance < ISLAND_CONTAINER_SIZE) {
                    if (blocks[x + i][y][z + distance] == blockType) {
                        return distance;
                    }
                }
                if (z + i >= 0 && z + i < ISLAND_CONTAINER_SIZE && x - distance >= 0 && x - distance < ISLAND_CONTAINER_SIZE) {
                    if (blocks[x - distance][y][z + i] == blockType) {
                        return distance;
                    }
                }
                if (z + i >= 0 && z + i < ISLAND_CONTAINER_SIZE && x + distance >= 0 && x + distance < ISLAND_CONTAINER_SIZE) {
                    if (blocks[x + distance][y][z + i] == blockType) {
                        return distance;
                    }
                }
            }
            distance++;
        }
        return -1;
    }

    public int[] hasWater() {
        int[] coords = new int[3]; // Array to store coordinates

        for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
            for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                for (int y = 0; y < ISLAND_CONTAINER_SIZE; y++) {
                    if (water[x][y][z]) {
                        coords[0] = x; // Store x coordinate
                        coords[1] = y; // Store y coordinate
                        coords[2] = z; // Store z coordinate
                        return coords; // Return coordinates
                    }
                }
            }
        }
        return null; // Return null if no water is found
    }
}
