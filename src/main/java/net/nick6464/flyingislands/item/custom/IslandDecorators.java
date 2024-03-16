package net.nick6464.flyingislands.item.custom;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
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
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.feature.*;
import net.nick6464.flyingislands.FlyingIslands;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static net.minecraft.server.command.FillBiomeCommand.UNLOADED_EXCEPTION;

public class IslandDecorators {

    private static FlyingIsland island;
    private final LocalRandom random;

    public IslandDecorators(FlyingIsland island, int seed) {
        super();
        IslandDecorators.island = island;
        this.random = new LocalRandom(seed);
    }

    public void randomDecorator() throws CommandSyntaxException {
        if (island.context.getWorld().isClient())
            return;

        // Get all chunks in the island
        BlockPos placedPos = island.context.getBlockPos();
        BlockPos to = placedPos.add(FlyingIsland.ISLAND_CONTAINER_SIZE, FlyingIsland.ISLAND_CONTAINER_SIZE, FlyingIsland.ISLAND_CONTAINER_SIZE);

        ServerWorld world = (ServerWorld) island.context.getWorld();
        MinecraftServer server = world.getServer();

        DynamicRegistryManager registryManager = server.getRegistryManager();

        List<Biome> validBiomes = new ArrayList<>();
        List<Biome> biomes = new ArrayList<>();
        registryManager.get(RegistryKeys.BIOME).forEach(biomes::add);

        for (Biome biome : biomes) {

            RegistryEntry<Biome> regEntry = registryManager.get(RegistryKeys.BIOME).getEntry(biome);

            if (regEntry.isIn(BiomeTags.IS_OVERWORLD) &&
                    !regEntry.isIn(BiomeTags.IS_NETHER) &&
                    !regEntry.isIn(BiomeTags.IS_END) &&
                    !regEntry.isIn(BiomeTags.IS_OCEAN) &&
                    !regEntry.isIn(BiomeTags.IS_RIVER) &&
                    !regEntry.isIn(BiomeTags.IS_DEEP_OCEAN) &&
                    !regEntry.isIn(BiomeTags.IS_BEACH)){
                validBiomes.add(biome);
            }
        }

        FlyingIslands.LOGGER.info("Valid biomes: " + validBiomes.size());

        // Select a random biome from valid biomes
        int biomeIndex = random.nextInt(validBiomes.size());
        Biome randBiome = validBiomes.get(biomeIndex);

        String biomeName = Objects.requireNonNull(registryManager.get(RegistryKeys.BIOME).getId(validBiomes.get(biomeIndex))).getPath();

        // Tell the player which biome was chosen
        Objects.requireNonNull(island.context.getPlayer()).sendMessage(Text.of("Biome: " + biomeName), false);

        RegistryEntry<Biome> biomeEntry = registryManager.get(RegistryKeys.BIOME).getEntry(randBiome);

        int failed = setBiome(placedPos, to, biomeEntry);

        assert randBiome != null;
        for (int x = 0; x < FlyingIsland.ISLAND_CONTAINER_SIZE; x += 4) {
            for (int z = 0; z < FlyingIsland.ISLAND_CONTAINER_SIZE; z += 4) {
                // For each feature in the biome, place it in the island
                int groundY = getGroundHeight(x, z);
                if(groundY == -1) continue;

                BlockPos featurePos = new BlockPos(placedPos.getX() + x,
                        placedPos.getY() + groundY,
                        z + placedPos.getZ());


                for (RegistryEntry<PlacedFeature> featureEntry :
                        randBiome.getGenerationSettings().getFeatures().get(9)) {

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

        BlockBox blockBox = BlockBox.create(from, to);
        ServerWorld serverWorld = (ServerWorld) island.context.getWorld();
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
}
