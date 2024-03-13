package net.nick6464.flyingislands.item.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.FillBiomeCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BuiltinBiomes;
import net.minecraft.world.biome.OverworldBiomeCreator;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.*;
import net.nick6464.flyingislands.FlyingIslands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class IslandDecorators {

    private static FlyingIsland island;
    static LocalRandom random;

    public IslandDecorators(FlyingIsland island) {
        super();
        IslandDecorators.island = island;
        IslandDecorators.random = new LocalRandom(FlyingIsland.SEED);
    }

    public static void jungleDecorator() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Get all chunks in the island
        BlockPos placedPos = island.context.getBlockPos();
        BlockPos to = placedPos.add(FlyingIsland.ISLAND_CONTAINER_SIZE, FlyingIsland.ISLAND_CONTAINER_SIZE, FlyingIsland.ISLAND_CONTAINER_SIZE);

        ServerWorld world = (ServerWorld) island.context.getWorld();
        MinecraftServer server = world.getServer();


        DynamicRegistryManager registryManager = server.getRegistryManager();
        Biome biome = registryManager.get(RegistryKeys.BIOME).get(Identifier.tryParse("plains"));

        FillBiomeCommand fillBiomeCommand = new FillBiomeCommand();
        Method privateMethod = FillBiomeCommand.class.getDeclaredMethod("execute", ServerCommandSource.class, BlockPos.class, BlockPos.class, RegistryEntry.Reference.class, Predicate.class);
        privateMethod.setAccessible(true);

        assert biome != null;
        FlyingIslands.LOGGER.info("Biomes: " + biome.getGenerationSettings().getFeatures());


        for (int x = 0; x < FlyingIsland.ISLAND_CONTAINER_SIZE; x++) {
            for (int z = 0; z < FlyingIsland.ISLAND_CONTAINER_SIZE; z++) {
                // For each feature in the biome, place it in the island
                int groundY = getGroundHeight(x, z);
                if(groundY == -1) continue;

                BlockPos featurePos = new BlockPos(placedPos.getX() + x,
                        placedPos.getY() + groundY,
                        z + placedPos.getZ());

                FlyingIslands.LOGGER.info("X: " + featurePos.getX() + " Z: " + featurePos.getZ() + " " +
                        "Y: " + featurePos.getY());

                for (RegistryEntry<PlacedFeature> featureEntry :
                        biome.getGenerationSettings().getFeatures().get(9)) {
                    PlacedFeature feature = featureEntry.value();
                    feature.generate(world,
                            world.getChunkManager().getChunkGenerator(),
                            random,
                            featurePos);
                }
            }
        }
    }

    private static int getGroundHeight(int x, int z) {
        for (int y = FlyingIsland.ISLAND_CONTAINER_SIZE - 1; y >= 0; y--) {
            Block block = island.getBlock(x, y, z);
            if (block != Blocks.AIR && block != null && block != Blocks.WATER)
                return y + 1;
        }
        return -1;
    }
}
