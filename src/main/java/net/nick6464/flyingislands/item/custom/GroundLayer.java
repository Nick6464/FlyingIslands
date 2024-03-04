package net.nick6464.flyingislands.item.custom;

public class GroundLayer {
    private static int RADIAL_DETAIL_MULTIPLIER = 0;
    private static boolean[][] groundLayer = new boolean[0][];
    private static int ISLAND_SIZE = 0;
    private static int ISLAND_RADIUS = 0;
    private static int ISLAND_CONTAINER_SIZE = 0;
    private static int ISLAND_CONTAINER_RADIUS = 0;
    private static int SEED = 0;
    private static float FREQUENCY = 0.12f;
    static float MAGNITUDE = 2f;
    static int SMOOTHING_PASSES = 2;

    public GroundLayer(int islandContainerSize, int islandSize, int seed) {
        groundLayer = new boolean[islandContainerSize][islandContainerSize];
        ISLAND_SIZE = islandSize;
        ISLAND_RADIUS = islandSize / 2;
        ISLAND_CONTAINER_SIZE = islandContainerSize;
        ISLAND_CONTAINER_RADIUS = islandContainerSize / 2;
        RADIAL_DETAIL_MULTIPLIER = Math.round((float) ISLAND_RADIUS /  3f);
        SEED = seed;
    }

    public static void generateGroundLayer() {
        radialNoiseGenerator();
        smoothHardCorners();
        floodFill(ISLAND_CONTAINER_SIZE / 2, ISLAND_CONTAINER_SIZE / 2);
    }

    public boolean[][] getGroundLayer() {
        return groundLayer;
    }

    public boolean getBlock(int x, int z) {
        return groundLayer[x][z];
    }

    public static void radialNoiseGenerator(){
        // Use the block count in the circle to determine the number of blocks in a perfect circle
        int blockCount = RADIAL_DETAIL_MULTIPLIER * blocksInCircle();

        // Divide a perfect circle by the number of blocks in a circle, this is the number of
        // angles to use to generate the circle. A block will be placed at each angle, and 1D noise
        // will be added or subtracted to the distance from the center of the circle to the block.

        // Loop over every angle
        for (int angle = 0; angle < blockCount; angle++) {
            // Determine the distance from the center of the circle to the block
            float noise = OpenSimplex2S.noise2(SEED, 1, (double) angle / RADIAL_DETAIL_MULTIPLIER * FREQUENCY);
            double distance = ISLAND_RADIUS + noise * MAGNITUDE;

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
}
