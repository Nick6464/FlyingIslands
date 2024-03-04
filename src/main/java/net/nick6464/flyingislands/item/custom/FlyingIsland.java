package net.nick6464.flyingislands.item.custom;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.message.MessageType;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class FlyingIsland extends GroundLayer {
    private int SEED;
    private final boolean[][][] blocks;
    private final boolean[][][] water;
    static int ISLAND_SIZE = 50;
    static int ISLAND_GROUND_HEIGHT = (int) (ISLAND_SIZE / 1.8);
    static int ISLAND_CONTAINER_SIZE = (int) (ISLAND_SIZE * 1.5);
    static float FREQUENCY = 0.12f;
    static float UNDERSIDE_MAGNITUDE = 2f;
    static float TOPSIDE_MAGNITUDE = 2f;
    GroundLayer groundLayer = new GroundLayer(ISLAND_CONTAINER_SIZE, ISLAND_SIZE, SEED);

    public FlyingIsland(int seed) {
        super(ISLAND_CONTAINER_SIZE, ISLAND_SIZE, seed);
        this.SEED = seed;
        this.blocks = new boolean[ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE];
        this.water = new boolean[ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE];
    }

    public void generateIsland() {
        generateGroundLayer();
        undersideGenerator();
        topsideGenerator();
        waterFiller();
    }

    public void placeIsland(World world, BlockPos pos) {
        for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
            for (int y = 0; y < ISLAND_CONTAINER_SIZE; y++) {
                for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                    if (blocks[x][y][z]) {
                        world.setBlockState(pos.add(x, y, z), Blocks.STONE.getDefaultState(), 3);
                    }
                }
            }
        }
        for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
            for (int y = 0; y < ISLAND_CONTAINER_SIZE; y++) {
                for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                    if (water[x][y][z]) {
                        world.setBlockState(pos.add(x, y, z), Blocks.WATER.getDefaultState(), 3);
                    }
                }
            }
        }
    }

    public void deleteIsland(World world, BlockPos pos) {
        for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
            for (int y = 0; y < ISLAND_CONTAINER_SIZE; y++) {
                for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                    if (blocks[x][y][z]) {
                        world.removeBlock(pos.add(x, y, z), false);
                    }
                }
            }
        }

        for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
            for (int y = 0; y < ISLAND_CONTAINER_SIZE; y++) {
                for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                    if (water[x][y][z]) {
                        world.setBlockState(pos.add(x, y, z), Blocks.AIR.getDefaultState(), 3);
                    }
                }
            }
        }
    }

    // ------------------------------- WATER FILLER -------------------------------
    public void waterFiller() {
        boolean[][] waterLayer = new boolean[ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE];
        // compare the ground layer to the blocks array and set the same position in the water array to true
        // if the ground layer is true and the blocks array is false
        for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
            for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                if (groundLayer.getBlock(x, z) && !blocks[x][ISLAND_GROUND_HEIGHT][z]) {
                    waterLayer[x][z] = true;
                }
            }
        }

        // Place the waterLayer into the 3D array at the ground height
        for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
            for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                if (waterLayer[x][z]) {
                    water[x][ISLAND_GROUND_HEIGHT][z] = true;
                }
            }
        }

        int depth = 1;
        boolean completed = false;
        while(!completed) {
            completed = true;
            for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
                for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                    if (waterLayer[x][z] && blocks[x][ISLAND_GROUND_HEIGHT - depth][z]) {
                        water[x][ISLAND_GROUND_HEIGHT - 1][z] = true;
                        completed = false;
                        depth++;
                    }
                }
            }
        }

        // Loop over the water and blocks array to compare. Ensure each water column has at least
        // one block below it, if not, set the water to false
        for (int x = 0; x < ISLAND_CONTAINER_SIZE; x++) {
            for (int z = 0; z < ISLAND_CONTAINER_SIZE; z++) {
                if (waterLayer[x][z]) {
                    for (int y = ISLAND_GROUND_HEIGHT - 1; y > 0; y--) {
                        if (!blocks[x][y][z]) {
                            water[x][y][z] = false;
                        }
                    }
                }
            }
        }
    }


    // ------------------------------- TOPSIDE GENERATOR -------------------------------
    public void topsideGenerator() {
        // Generate a 2d array that represents how far from an edge the block is
        float[][] topside = new float[ISLAND_CONTAINER_SIZE][ISLAND_CONTAINER_SIZE];

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

                // Since height can be negative, start at the height, and go towards the ground layer
                // A negative height will set the blocks at that position to false
                int height = (int) (ISLAND_GROUND_HEIGHT + noise);
                if (noise < 0 && topside[x][z] > 3) {
                    for (int y = height + 1; y <= ISLAND_GROUND_HEIGHT; y++) {
                        blocks[x][y][z] = false;
                    }
                } else {
                    for (int y = ISLAND_GROUND_HEIGHT; y <= height; y++) {
                        blocks[x][y][z] = true;
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
                    blocks[x][y][z] = true;
                }
            }
        }

    }

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

}
