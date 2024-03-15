package net.nick6464.flyingislands.item.custom;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.command.argument.RegistryEntryPredicateArgumentType;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.FillBiomeCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.feature.*;
import net.nick6464.flyingislands.FlyingIslands;
import org.apache.commons.lang3.mutable.MutableInt;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static net.minecraft.server.command.FillBiomeCommand.UNLOADED_EXCEPTION;

public class IslandDecorators {

    private static FlyingIsland island;
    static LocalRandom random;

    public IslandDecorators(FlyingIsland island) {
        super();
        IslandDecorators.island = island;
        IslandDecorators.random = new LocalRandom(FlyingIsland.SEED);
    }

    public void jungleDecorator() throws CommandSyntaxException {
        if (island.context.getWorld().isClient()) {
            FlyingIslands.LOGGER.info("Client side jungleDecorator() called");
            return;
        }
        // Get all chunks in the island
        BlockPos placedPos = island.context.getBlockPos();
        BlockPos to = placedPos.add(FlyingIsland.ISLAND_CONTAINER_SIZE, FlyingIsland.ISLAND_CONTAINER_SIZE, FlyingIsland.ISLAND_CONTAINER_SIZE);

        ServerWorld world = (ServerWorld) island.context.getWorld();
        MinecraftServer server = world.getServer();

        DynamicRegistryManager registryManager = server.getRegistryManager();
        String biomeId = "jungle";
        Biome biome = registryManager.get(RegistryKeys.BIOME).get(Identifier.of("minecraft", biomeId));

        RegistryEntry<Biome> biomeEntry = registryManager.get(RegistryKeys.BIOME).getEntry(biome);

        FlyingIslands.LOGGER.info("Biome Entry: " + biomeEntry);

        int failed = setBiome(placedPos, to, biomeEntry);
        FlyingIslands.LOGGER.info("Failed to change " + failed + " blocks to " + biomeId);

        assert biome != null;
        for (int x = 0; x < FlyingIsland.ISLAND_CONTAINER_SIZE; x++) {
            for (int z = 0; z < FlyingIsland.ISLAND_CONTAINER_SIZE; z++) {
                // For each feature in the biome, place it in the island
                int groundY = getGroundHeight(x, z);
                if(groundY == -1) continue;

                BlockPos featurePos = new BlockPos(placedPos.getX() + x,
                        placedPos.getY() + groundY,
                        z + placedPos.getZ());


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

    public static int setBiome(BlockPos from,
                                BlockPos to,
                                RegistryEntry<Biome> biome) throws CommandSyntaxException {

        BlockPos blockPos2;
        BlockPos blockPos = convertPos(from);
        BlockBox blockBox = BlockBox.create(blockPos, blockPos2 = convertPos(to));

        ServerWorld serverWorld = (ServerWorld) island.context.getWorld();
        MinecraftServer server = serverWorld.getServer();
        List<Chunk> chunks = new ArrayList<>();

        for (int k = ChunkSectionPos.getSectionCoord(blockBox.getMinZ()); k <= ChunkSectionPos.getSectionCoord(blockBox.getMaxZ()); ++k) {
            for (int l = ChunkSectionPos.getSectionCoord(blockBox.getMinX()); l <= ChunkSectionPos.getSectionCoord(blockBox.getMaxX()); ++l) {
                Chunk chunk = serverWorld.getChunk(l, k, ChunkStatus.FULL, false);
                if (chunk == null) {
                    throw UNLOADED_EXCEPTION.create();
                }
                chunks.add(chunk);
            }
        }

        MutableInt mutableInt = new MutableInt(0);
        for (Chunk chunk : chunks) {
            chunk.populateBiomes(createBiomeSupplier(mutableInt, chunk, blockBox, biome), serverWorld.getChunkManager().getNoiseConfig().getMultiNoiseSampler());
            chunk.setNeedsSaving(true);
        }

        serverWorld.getChunkManager().threadedAnvilChunkStorage.sendChunkBiomePackets(chunks);
        // Logging feedback about biome changes
        FlyingIslands.LOGGER.info("Biomes changed successfully!");
         return mutableInt.getValue();
    }

    private static BiomeSupplier createBiomeSupplier(MutableInt counter, Chunk chunk, BlockBox box, RegistryEntry<Biome> biome) {
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

    private static int convertCoordinate(int coordinate) {
        return BiomeCoords.toBlock(BiomeCoords.fromBlock(coordinate));
    }

    private static BlockPos convertPos(BlockPos pos) {
        return new BlockPos(convertCoordinate(pos.getX()), convertCoordinate(pos.getY()), convertCoordinate(pos.getZ()));
    }

}
