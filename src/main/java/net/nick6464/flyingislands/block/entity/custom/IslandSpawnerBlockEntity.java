package net.nick6464.flyingislands.block.entity.custom;

import net.minecraft.block.entity.BlockEntity;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.nick6464.flyingislands.FlyingIslands;
import net.nick6464.flyingislands.block.entity.ModBlockEntities;


public class IslandSpawnerBlockEntity extends BlockEntity {

    public IslandSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ISLAND_SPAWNER_BLOCK_ENTITY, pos, state);
    }


    public void tick(World world, BlockPos pos, BlockState state) {
        if (world.getTime() % 20L == 0L && !world.isClient()) {
            FlyingIslands.LOGGER.info("Entity Ticking");
        }
    }
}