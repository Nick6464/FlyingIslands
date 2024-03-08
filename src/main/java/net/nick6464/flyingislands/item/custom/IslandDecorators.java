package net.nick6464.flyingislands.item.custom;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.*;
import net.nick6464.flyingislands.FlyingIslands;

public class IslandDecorators {

    private static FlyingIsland island;
    static LocalRandom random;

    public IslandDecorators(FlyingIsland island) {
        super();
        IslandDecorators.island = island;
        IslandDecorators.random = new LocalRandom(island.SEED);
    }
    public static void plainsDecorator() {
        FlyingIslands.LOGGER.info("Decorating plains");

        for (int x = 0; x < FlyingIsland.ISLAND_CONTAINER_SIZE; x++) {
            for (int z = 0; z < FlyingIsland.ISLAND_CONTAINER_SIZE; z++) {
                if (island.groundLayer.getBlock(x, z)) {
                    if (FlyingIsland.generateRandomNumber(1, 30) != 1)
                        continue;

                    int groundY = getGroundHeight(x, z);
                    int rand = FlyingIsland.generateRandomNumber(0, 10);

                    if (island.getBlock(x, groundY, z) == Blocks.GRASS_BLOCK) {
                        if (rand < 5)
                            decorator(x, (groundY), z,
                                    VegetationConfiguredFeatures.TREES_PLAINS.value());
                        else if (rand < 6)
                            decorator(x, groundY, z,
                                    VegetationConfiguredFeatures.FLOWER_PLAIN.value());
                        else if (rand < 7)
                            decorator(x, groundY, z,
                                    VegetationConfiguredFeatures.PATCH_GRASS.value());
                        else if (rand < 9)
                            decorator(x, groundY, z,
                                    VegetationConfiguredFeatures.PATCH_TALL_GRASS.value());
                        else
                            decorator(x, groundY, z,
                                    VegetationConfiguredFeatures.FLOWER_FLOWER_FOREST.value());
                    }
                    else if (island.getBlock(x, groundY, z) == Blocks.SAND) {
                        // Sand can have sugar cane or seagrass
                        FlyingIslands.LOGGER.info("Sand at " + x + ", " + groundY + ", " + z);
                        if(island.getBlock(x, groundY + 1, z) != Blocks.WATER) {
                            decorator(x, groundY, z,
                                    VegetationConfiguredFeatures.PATCH_SUGAR_CANE.value());
                        }
                        else {
                            if(rand < 2)
                                decorator(x, groundY, z,
                                        OceanConfiguredFeatures.SEAGRASS_MID.value());
                            else if(rand < 4)
                                decorator(x, groundY, z,
                                        OceanConfiguredFeatures.SEAGRASS_SHORT.value());
                            else if(rand < 6)
                                decorator(x, groundY, z,
                                        OceanConfiguredFeatures.SEAGRASS_TALL.value());
                            else
                                decorator(x, groundY, z,
                                    OceanConfiguredFeatures.KELP.value());
                        }
                    }
                }
            }
        }
    }


    public static void decorator(int x, int y, int z, ConfiguredFeature<?, ?> feature) {
        // Only Trees and flowers can be placed, and they require different heights
        // Trees require 2 blocks of air above the ground
        // Flowers require 1 block of air above the ground
        int yLevel = island.context.getBlockPos().getY() + y;
        boolean tree = false;

        boolean flowers = feature.generate((StructureWorldAccess) island.context.getWorld(),
                ((ServerWorld) island.context.getWorld()).getChunkManager().getChunkGenerator(),
                random,
                new BlockPos(island.context.getBlockPos().getX() + x,
                        yLevel,
                        island.context.getBlockPos().getZ() + z));
        if(!flowers)
            tree = feature.generate((StructureWorldAccess) island.context.getWorld(),
                    ((ServerWorld) island.context.getWorld()).getChunkManager().getChunkGenerator(),
                    random,
                    new BlockPos(island.context.getBlockPos().getX() + x,
                            yLevel + 1,
                            island.context.getBlockPos().getZ() + z));

        if(tree || flowers)
            FlyingIslands.LOGGER.info("Decorated at " + x + ", " + y + ", " + z);
        else
            FlyingIslands.LOGGER.info("Failed to decorate at " + x + ", " + y + ", " + z);

    }

    private static int getGroundHeight(int x, int z) {
        for (int y = FlyingIsland.ISLAND_CONTAINER_SIZE - 1; y > 0; y--) {
            Block block = island.getBlock(x, y, z);
            if (block != Blocks.AIR && block != null && block != Blocks.WATER)
                return y;
        }
        return -1;
    }
}






