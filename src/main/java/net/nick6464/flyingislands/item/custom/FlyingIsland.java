package net.nick6464.flyingislands.item.custom;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.nick6464.flyingislands.FlyingIslands;

import java.util.*;


public class FlyingIsland extends GroundLayer {
    public static int SEED;
    ItemUsageContext context;
    Block[][][] blocks;
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
        blocks = new Block[ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE];
        this.trees = new boolean[ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE];
        this.topside = new float[ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE];
        this.topsideNoise = new float[ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE];

        float topMagnitudeMultiplier =  ISLAND_SIZE / 10f;
        ISLAND_GROUND_HEIGHT = (int) (ISLAND_SIZE / 1.8);

        TOPSIDE_MAGNITUDE = generateRandomFloat(topMagnitudeMultiplier / 10,
                topMagnitudeMultiplier);
        TOPSIDE_FREQUENCY = generateRandomFloat(0.01f, 0.05f);
        TOPSIDE_OFFSET = TOPSIDE_MAGNITUDE;

        UNDERSIDE_FREQUENCY = generateRandomFloat(0.5f, 1f);
        UNDERSIDE_MAGNITUDE = generateRandomFloat(0.2f, 1.2f);
        UNDERSIDE_STEEPNESS = generateRandomFloat(0.5f, 1.5f);

    }

    public void generateIsland() {
        // Generate the shape of the island
        Objects.requireNonNull(context.getPlayer()).sendMessage(Text.of("SEED: " + SEED), false);
        context.getPlayer().sendMessage(Text.of("Topside Magnitude: " + TOPSIDE_MAGNITUDE), false);
        context.getPlayer().sendMessage(Text.of("Topside Frequency: " + TOPSIDE_FREQUENCY), false);
        context.getPlayer().sendMessage(Text.of("Topside Offset: " + TOPSIDE_OFFSET), false);
        context.getPlayer().sendMessage(Text.of("Underside Magnitude: " + UNDERSIDE_MAGNITUDE), false);
        context.getPlayer().sendMessage(Text.of("Underside Frequency: " + UNDERSIDE_FREQUENCY), false);
        context.getPlayer().sendMessage(Text.of("Island Ground Height: " + ISLAND_GROUND_HEIGHT), false);
        context.getPlayer().sendMessage(Text.of("Island Ground Y: " + Math.addExact(ISLAND_GROUND_HEIGHT, context.getBlockPos().getY())), false);
        context.getPlayer().sendMessage(Text.of("Island Size: " + ISLAND_SIZE), false);
        context.getPlayer().sendMessage(Text.of("Island Container Size: " + ISLAND_CONTAINER_SIZE), false);
        context.getPlayer().sendMessage(Text.of("Island Container Radius: " + ISLAND_CONTAINER_RADIUS), false);
        context.getPlayer().sendMessage(Text.of("Radial Detail Multiplier: " + RADIAL_DETAIL_MULTIPLIER), false);
        context.getPlayer().sendMessage(Text.of("Ground Frequency: " + GROUNDLAYER_FREQUENCY),
                false);
        context.getPlayer().sendMessage(Text.of("Ground Magnitude: " + GROUNDLAYER_MAGNITUDE),
                false);

        generateGroundLayer();

        undersideGenerator();
//        topsideGenerator();

        // Add water to the island and generate lakes and rivers
//        lakeGenerator();

        orphanRemover();

        // Generates the blocks according to desired biome along with foliage and trees
        blockPopulator();

        sandDecorator();
        Objects.requireNonNull(context.getPlayer()).sendMessage(Text.of("Completed"), false);
    }

    // ------------------------------- PLACER AND DELETER -------------------------------

    public void placeIsland(World world, BlockPos pos) {
        for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
            for (int y = 0; y < ISLAND_CONTAINER_SIZE; y++) {
                for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                    if (blocks[x][y][z] != null && blocks[x][y][z] != Blocks.AIR) {
//                        FlyingIslands.LOGGER.info("Placing block at: " + pos.add(x, y, z));
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
        FlyingIslands.LOGGER.info("Island Container Size: " + ISLAND_CONTAINER_SIZE);
        for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
            for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                for (int y = 0; y < ISLAND_CONTAINER_SIZE; y++) {
                    if (blocks[x][y][z] == Blocks.STONE) {
                        if (distanceToSurface(x, y, z) <= (OpenSimplex2S.noise2(SEED, x * DIRT_FREQUENCY, z * DIRT_FREQUENCY) + 3) * DIRT_MAGNITUDE) {
                            if (isSurface(x, y, z)) {
                                blocks[x][y][z] = Blocks.GRASS_BLOCK;
                            }
                            else {
                                blocks[x][y][z] = Blocks.DIRT;
                            }
                        }
                    }
                }
            }
        }
    }

    // ------------------------------- BLOCK SMOOTHING -------------------------------
    // Using a flood fill algorithm track the blocks that are touching and remove any blocks that
    // are orphaned
    public void orphanRemover() {
        // Initial start point
        int[] start = new int[]{ISLAND_CONTAINER_RADIUS,
                getSurfaceHeight(ISLAND_CONTAINER_RADIUS, ISLAND_CONTAINER_RADIUS),
                ISLAND_CONTAINER_RADIUS};

        // Initialize visited set
        Set<String> visited = new HashSet<>();
        List<int[]> connectedBlocks = new ArrayList<>();
        List<int[]> waterBlocks = new ArrayList<>();

        // Start the flood fill algorithm to find the main island
        floodFill(start, visited, connectedBlocks, waterBlocks);

        FlyingIslands.LOGGER.info("Connected Blocks: " + connectedBlocks.size());

        // Find the max and min x, y, z values of the connected blocks to make a new container
        int[][] container  = getNewContainerSize(connectedBlocks);

        int[] lowValues = container[0];
        int[] highValues = container[1];

        int lowest = Math.min(lowValues[0], Math.min(lowValues[1], lowValues[2]));
        int highest = Math.max(highValues[0], Math.max(highValues[1], highValues[2]));

        int newContainerSize = highest - lowest;
        // Create a new 3D array to store the connected blocks
        Block[][][] copy = new Block[newContainerSize][newContainerSize][newContainerSize];

        // Find difference between the new and old container size
        ISLAND_CONTAINER_SIZE = newContainerSize;

        FlyingIslands.LOGGER.info("Lowest: " + Arrays.toString(lowValues));
        FlyingIslands.LOGGER.info("Highest: " + Arrays.toString(highValues));

        // Copy the connected blocks to the new container
        for (int[] block : connectedBlocks) {
            int x = block[0];
            int y = block[1];
            int z = block[2];
            copy[x - lowValues[0]][y - lowValues[1]][z - lowValues[2]] = blocks[x][y][z];
        }

        for (int[] block : waterBlocks) {
            int x = block[0];
            int y = block[1];
            int z = block[2];
            copy[x - lowValues[0]][y - lowValues[1]][z - lowValues[2]] = blocks[x][y][z];
        }

        blocks = copy;

    }

    private int[][] getNewContainerSize(List<int[]> connectedBlocks) {
        int minX = ISLAND_CONTAINER_SIZE;
        int minY = ISLAND_CONTAINER_SIZE;
        int minZ = ISLAND_CONTAINER_SIZE;
        int maxX = 0;
        int maxY = 0;
        int maxZ = 0;

        // Find the max and min x, y, z values of the connected blocks
        for (int[] block : connectedBlocks) {
            int x = block[0];
            int y = block[1];
            int z = block[2];
            if (x < minX) {
                minX = x;
            }
            if (x > maxX) {
                maxX = x;
            }
            if (y < minY) {
                minY = y;
            }
            if (y > maxY) {
                maxY = y;
            }
            if (z < minZ) {
                minZ = z;
            }
            if (z > maxZ) {
                maxZ = z;
            }
        }

        // Find the lowest and highest values
        int[] lowValues = new int[]{minX, minY, minZ};
        int[] highValues = new int[]{maxX, maxY, maxZ};

        return new int[][]{lowValues, highValues};

    }

    private void floodFill(int[] start, Set<String> visited, List<int[]> connectedBlocks, List<int[]> waterBlocks) {
        Stack<int[]> stack = new Stack<>();
        stack.push(start);

        while (!stack.isEmpty()) {
            int[] current = stack.pop();
            int x = current[0];
            int y = current[1];
            int z = current[2];

            // Mark the current block as visited
            visited.add(x + "," + y + "," + z);

            // Check if the current block is not an orphan and add it to connectedBlocks
            if (blocks[x][y][z] != Blocks.AIR && blocks[x][y][z] != null) {
                connectedBlocks.add(current);
            }

            // Define six directions: up, down, left, right, front, back
            int[][] directions = {
                    {0, 1, 0}, {0, -1, 0}, {-1, 0, 0}, {1, 0, 0}, {0, 0, -1}, {0, 0, 1}
            };

            for (int[] dir : directions) {
                int newX = x + dir[0];
                int newY = y + dir[1];
                int newZ = z + dir[2];

                // Check boundary conditions
                if (newX >= 0 && newX < ISLAND_CONTAINER_SIZE &&
                        newY >= 0 && newY < ISLAND_CONTAINER_SIZE &&
                        newZ >= 0 && newZ < ISLAND_CONTAINER_SIZE) {
                    // Check if the neighboring block hasn't been visited
                    if(blocks[newX][newY][newZ] != Blocks.AIR &&
                            blocks[newX][newY][newZ] != null &&
                            !visited.contains(newX + "," + newY + "," + newZ)) {
                        // Can propagate from stone to water or from stone to stone or from water to
                        // water,but not water to stone
                        if (blocks[x][y][z] == Blocks.WATER && blocks[newX][newY][newZ] != Blocks.WATER) {
                            continue;
                        }
                        stack.push(new int[]{newX, newY, newZ});
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
                (OpenSimplex2S.noise2(SEED, x * TOPSIDE_FREQUENCY, z * TOPSIDE_FREQUENCY)) ;
        int height = (int) (ISLAND_GROUND_HEIGHT + noise * (TOPSIDE_MAGNITUDE + LAKE_MAGNITUDE));

        if ((x < 0 || x >= ISLAND_CONTAINER_SIZE || z < 0 || z >= ISLAND_CONTAINER_SIZE))
            return;

        if(blocks[x][height][z] == Blocks.WATER || blocks[x][height][z] == null)
            return;

        if (noise < -0.25) {
            for (int y = height; y <= ISLAND_GROUND_HEIGHT - 1; y++) {
                blocks[x][y][z] = null;
                lakeCreator(x + 1, z);
                lakeCreator(x - 1, z);
                lakeCreator(x, z + 1);
                lakeCreator(x, z - 1);
            }
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
                    if (topsideNoise[x][z] < lowest && groundLayer.getBlock(x, z)) {
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
//        riverGenerator();
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
                if (blocks[x][ISLAND_GROUND_HEIGHT][z] == Blocks.WATER) {
                    int distance = (int) Math.sqrt(Math.pow(x - (double) ISLAND_CONTAINER_SIZE / 2, 2) + Math.pow(z - (double) ISLAND_CONTAINER_SIZE / 2, 2));
                    if (distance > furthestDistance) {
                        furthestDistance = distance;
                        furthestX = x;
                        furthestZ = z;
                    }
                }
            }
        }
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

            if (x < 0 || x > ISLAND_CONTAINER_SIZE || y < 0 || y > ISLAND_GROUND_HEIGHT || z < 0 || z > ISLAND_CONTAINER_SIZE) {
                continue;
            }

            if(blocks[x][y][z] == Blocks.WATER || blocks[x][y][z] == Blocks.STONE)
                continue;

            if (voidBelow(x, y, z) || !groundLayer.getBlock(x, z)) {
                continue;
            }

            blocks[x][y][z] = Blocks.WATER;

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
                    if(blocks[x][y][z] == Blocks.WATER ||
                            blocks[x][y][z] == null ||
                            blocks[x][y][z] == Blocks.AIR)
                        continue;
                    if(distanceToNeighbour(Blocks.WATER, x, y, z, 3) != -1 ||
                            isTouchingWater(x, y, z)) {
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
                    for (int y = height + 1; y <= ISLAND_GROUND_HEIGHT; y++) {
                        blocks[x][y][z] = Blocks.AIR;
                    }
                } else {
                    for (int y = ISLAND_GROUND_HEIGHT; y <= height; y++) {
                        blocks[x][y][z] = Blocks.STONE;
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
                    blocks[x][y][z] = Blocks.STONE;
                }
            }
        }
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
    // The surface is a block in the blocks array that itself is true has a false value in the y
    // value above it
    public int distanceToSurface(int x, int y, int z) {
        int distance = 0;
        while (y + distance < ISLAND_CONTAINER_SIZE) {
            if (blocks[x][y + distance][z] != Blocks.AIR && blocks[x][y + distance][z] != null) {
                distance++;
            } else
                return distance;
        }
        return -1;
    }

    // Does a block have an air block above it
    public boolean isSurface(int x, int y, int z) {
        return (blocks[x][y][z] == Blocks.STONE || blocks[x][y][z] == Blocks.WATER) &&
                blocks[x][y + 1][z] == Blocks.AIR || blocks[x][y + 1][z] == null || blocks[x][y + 1][z] == Blocks.WATER;
    }

    Block getBlock(int x, int y, int z) {
        return blocks[x][y][z];
    }

    // Give Coordinates to a block and checks if there is any water around it
    boolean isTouchingWater(int x, int y, int z) {
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

    private boolean getWater(int x, int y, int z) {
        return blocks[x][y][z] == Blocks.WATER;
    }

    private boolean notFloating(int x, int y, int z) {
        if(y - 1 < 0)
            return false;
        return blocks[x][y - 1][z] != null &&
                blocks[x][y - 1][z] != Blocks.AIR &&
                blocks[x][y - 1][z] != Blocks.WATER;
    }

    private boolean voidBelow(int x, int y, int z) {
        for(int i = y; i >= 0; i--) {
            if(blocks[x][i][z] != null && blocks[x][i][z] != Blocks.AIR)
                return false;
        }
        return true;
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
                    if (blocks[x][y][z] == Blocks.WATER) {
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

    public int getSurfaceHeight(int x, int z) {
        for (int y = ISLAND_GROUND_HEIGHT; y > 0; y--) {
            if (blocks[x][y][z] == Blocks.STONE) {
                return y;
            }
        }
        return -1;
    }


}
