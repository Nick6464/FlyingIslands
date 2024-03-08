package net.nick6464.flyingislands.item.custom;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.*;
import net.nick6464.flyingislands.FlyingIslands;

public class IslandDecorators {

    private static FlyingIsland island;
    static LocalRandom random;

    public IslandDecorators(FlyingIsland island) {
        IslandDecorators.island = island;
        IslandDecorators.random = new LocalRandom(island.SEED);
    }
    public static void plainsDecorator() {

        for (int x = 0; x < FlyingIsland.ISLAND_CONTAINER_SIZE; x++) {
            for (int z = 0; z < FlyingIsland.ISLAND_CONTAINER_SIZE; z++) {
                if(island.groundLayer.getBlock(x, z)) {
//                    if (FlyingIsland.generateRandomNumber(1, 1) != 1)
//                        continue;
//
//                    int rand = FlyingIsland.generateRandomNumber(0, 30);
//                    if (rand < 10)
//                        decorator(x, getGroundHeight(x, z), z,
//                                VegetationConfiguredFeatures.TREES_PLAINS.value());
//                    else if (rand < 30)
                    decorator(x, getGroundHeight(x, z), z,
                                VegetationConfiguredFeatures.FLOWER_PLAIN.value());
                }
            }
        }
    }

    public static void decorator(int x, int y, int z, ConfiguredFeature<?, ?> feature) {
        if(y == -1)
            return;
        boolean passed = feature.generate((StructureWorldAccess) island.context.getWorld(),
                ((ServerWorld) island.context.getWorld()).getChunkManager().getChunkGenerator(),
                random.split(),
                new BlockPos(island.context.getBlockPos().getX() + x,
                        island.context.getBlockPos().getY() + y - 1,
                        island.context.getBlockPos().getZ() + z));
        if(passed)
            // Message player that the feature was placed
            island.context.getPlayer().sendMessage(Text.of("Placed"), false);
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






