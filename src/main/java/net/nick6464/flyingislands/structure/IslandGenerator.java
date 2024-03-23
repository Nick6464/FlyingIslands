package net.nick6464.flyingislands.structure;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.*;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.nick6464.flyingislands.FlyingIslands;
import net.nick6464.flyingislands.block.ModBlocks;
import net.nick6464.flyingislands.item.custom.FlyingIsland;

public class IslandGenerator extends ShiftableStructurePiece {

    public IslandGenerator(Random random, int x, int z) {
        super(ModGenerators.ISLAND_GEN, x, 100, z, 7, 7, 9, getRandomHorizontalDirection(random));
    }

    public IslandGenerator(NbtCompound nbtCompound) {
        super(ModGenerators.ISLAND_GEN, nbtCompound);
    }

    public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        // Place the island block
        this.addBlock(world, ModBlocks.ISLAND_SPAWNER_BLOCK.getDefaultState(),
                1,
                75,
                1,
                chunkBox);

        FlyingIslands.LOGGER.info("IslandGenerator generated");
    }
}