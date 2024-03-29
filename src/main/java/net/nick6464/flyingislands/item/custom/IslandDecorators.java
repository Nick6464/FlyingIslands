package net.nick6464.flyingislands.item.custom;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.feature.*;
import net.nick6464.flyingislands.FlyingIslands;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.server.command.FillBiomeCommand.UNLOADED_EXCEPTION;

public class IslandDecorators {

    private static FlyingIsland island;

    public IslandDecorators(FlyingIsland island) {
        super();
        IslandDecorators.island = island;
    }

    public void randomDecorator(Random random, BlockPos placedPos) throws CommandSyntaxException {
        // Get all chunks in the island
        ServerWorld world = island.serverWorld;

        for (int x = 0; x < island.ISLAND_CONTAINER_SIZE; x += 4) {
            for (int z = 0; z < island.ISLAND_CONTAINER_SIZE; z += 4) {
                // For each feature in the biome, place it in the island
                int groundY = getGroundHeight(x, z) + 75;
                FlyingIslands.LOGGER.info("Ground Y: " + groundY);
                if(groundY == -1) continue;

                BlockPos featurePos = new BlockPos(placedPos.getX() + x,
                        placedPos.getY() + groundY,
                        z + placedPos.getZ());


                for (RegistryEntry<PlacedFeature> featureEntry :
                        island.biome.getGenerationSettings().getFeatures().get(9)) {

                    PlacedFeature feature = featureEntry.value();

                    feature.generate(world,
                            world.getChunkManager().getChunkGenerator(),
                            random,
                            featurePos);
                }
            }
        }
    }

    private int getGroundHeight(int x, int z) {
        for (int y = island.ISLAND_CONTAINER_SIZE - 1; y >= 0; y--) {
            Block block = island.getBlock(x, y, z);
            if (block != Blocks.AIR && block != null && block != Blocks.WATER)
                return y + 1;
        }
        return -1;
    }

    public int setBiome(BlockPos from,
                        BlockPos to,
                        RegistryEntry<Biome> biomeRegistryEntry,
                        List<Chunk> chunks) throws CommandSyntaxException {

        BlockBox blockBox = BlockBox.create(from, to);

        MutableInt mutableInt = new MutableInt(0);
        for (Chunk chunk : chunks) {
            chunk.populateBiomes(createBiomeSupplier(mutableInt, chunk, blockBox,
                    biomeRegistryEntry),
                    island.serverWorld.getChunkManager().getNoiseConfig().getMultiNoiseSampler());
            chunk.setNeedsSaving(true);
        }

        island.serverWorld.getChunkManager().threadedAnvilChunkStorage.sendChunkBiomePackets(chunks);
        // Logging feedback about biome changes

        return mutableInt.getValue();
    }

    private BiomeSupplier createBiomeSupplier(MutableInt counter, Chunk chunk, BlockBox box, RegistryEntry<Biome> biome) {
        return (x, y, z, noise) -> {
            int i = BiomeCoords.toBlock(x);
            int j = BiomeCoords.toBlock(y);
            int k = BiomeCoords.toBlock(z);
            RegistryEntry<Biome> registryEntry2 = chunk.getBiomeForNoiseGen(x, y, z);
            if (box.contains(i, j, k)) {
                counter.increment();
                return biome;
            }
            return registryEntry2;
        };
    }
}
