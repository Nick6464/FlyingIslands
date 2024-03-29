package net.nick6464.flyingislands.block.custom;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.nick6464.flyingislands.block.entity.ModBlockEntities;
import net.nick6464.flyingislands.block.entity.custom.IslandSpawnerBlockEntity;
import org.jetbrains.annotations.Nullable;

public class IslandSpawnerBlock extends BlockWithEntity implements BlockEntityProvider {

    public IslandSpawnerBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new IslandSpawnerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, ModBlockEntities.ISLAND_SPAWNER_BLOCK_ENTITY,
                (world1, pos, state1, blockEntity) -> {
                    try {
                        blockEntity.tick(world1, pos);
                    } catch (CommandSyntaxException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
