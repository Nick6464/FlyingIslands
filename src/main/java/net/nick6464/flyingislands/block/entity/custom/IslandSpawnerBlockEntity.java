package net.nick6464.flyingislands.block.entity.custom;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.entity.BlockEntity;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.nick6464.flyingislands.FlyingIslands;
import net.nick6464.flyingislands.block.entity.ModBlockEntities;
import net.nick6464.flyingislands.item.custom.FlyingIsland;
import net.nick6464.flyingislands.item.custom.IslandDecorators;

import java.util.ArrayList;
import java.util.List;


public class IslandSpawnerBlockEntity extends BlockEntity {
    private FlyingIsland island;
    private final List<ChunkPos> chunksCoords = new ArrayList<>();
    private List<Chunk> chunks = new ArrayList<>();

    private int xFrom;
    private int zFrom;

    private int xTo;
    private int zTo;
    public IslandSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ISLAND_SPAWNER_BLOCK_ENTITY, pos, state);
    }

    private void placeAndDecorate () throws CommandSyntaxException {
        // Generate the island
        IslandDecorators islandDecorators = new IslandDecorators(island);

        BlockPos posFrom = new BlockPos(pos.getX() - (island.ISLAND_RADIUS + 10),
                pos.getY() - island.ISLAND_RADIUS,
                pos.getZ() - (island.ISLAND_RADIUS + 10));

        BlockPos posTo = new BlockPos(pos.getX() + (island.ISLAND_RADIUS + 10),
                pos.getY() + island.ISLAND_RADIUS,
                pos.getZ() + (island.ISLAND_RADIUS + 10));

        island.generateIslandStructure();
        island.placeIsland(world, posFrom);

        islandDecorators.setBiome(
                posFrom,
                posTo,
                island.biomeRegistryEntry,
                chunks);


        islandDecorators.randomDecorator(island.random, posFrom);

        // Delete the block entity
        assert world != null;
        world.removeBlockEntity(pos);
        world.removeBlock(pos, false);
    }

    public void tick(World world, BlockPos pos) throws CommandSyntaxException {
        if (world.isClient() || world.getTime() % 20L != 0L)
            return;

        FlyingIslands.LOGGER.info("IslandSpawnerBlockEntity ticked");

        if (island == null) {
            ServerWorld serverWorld = (ServerWorld) world;
            int seed = Math.abs((pos.getX() + pos.getY() + pos.getZ()) % Integer.MAX_VALUE);
            island = new FlyingIsland(seed, serverWorld);
            FlyingIslands.LOGGER.info("Island created with seed " + seed);
        }

        xFrom = pos.getX() - island.ISLAND_RADIUS;
        zFrom = pos.getZ() - island.ISLAND_RADIUS;

        xTo = pos.getX() + island.ISLAND_RADIUS;
        zTo = pos.getZ() + island.ISLAND_RADIUS;

        FlyingIslands.LOGGER.info("Island area set");

        // Get a block that is in each chunk within the island area
        FlyingIslands.LOGGER.info("BlockState Fetching");
        int xPos = xFrom - 16;
        while(xPos < xTo + 16) {
            zLooper(xPos);
            if(xPos + 16 > xTo)
                zLooper(xTo);
            xPos += 16;
        }

        try {
            for (ChunkPos chunkPos : chunksCoords) {
                Chunk chunk = world.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, true);
                if (chunk != null)
                    chunks.add(chunk);
            }
        } catch (Exception e) {
            FlyingIslands.LOGGER.info("Chunk not found, do next tick");
            return;
        }


        // Force load the chunks
        for (ChunkPos chunkPos : chunksCoords) {
            world.getChunkManager().setChunkForced(chunkPos, true);
        }

        placeAndDecorate();

        // Unforce load the chunks
        for (ChunkPos chunkPos : chunksCoords) {
            world.getChunkManager().setChunkForced(chunkPos, false);
        }

        FlyingIslands.LOGGER.info("Island generated at " + pos.getX() + ", " + pos.getZ());

    }

    private void zLooper(int xPos) {
        int zPos = zFrom - 16;
        while(zPos < zTo + 16) {
            assert world != null;
            ChunkPos chunkPos = null;

            try {
                chunkPos = new ChunkPos(new BlockPos(xPos, 0, zPos));
            } catch (Exception e) {
                FlyingIslands.LOGGER.info("Generating Chunk");
                world.getChunk(xPos, zPos, ChunkStatus.FULL, true);
            }

            if(chunkPos != null && !chunksCoords.contains(chunkPos)) {
                chunksCoords.add(chunkPos);
            } else
                FlyingIslands.LOGGER.info("Chunk already added to Force Load");
            zPos += 16;
        }
    }
}