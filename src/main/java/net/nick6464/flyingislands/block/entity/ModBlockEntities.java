package net.nick6464.flyingislands.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.nick6464.flyingislands.FlyingIslands;
import net.nick6464.flyingislands.block.ModBlocks;
import net.nick6464.flyingislands.block.entity.custom.IslandSpawnerBlockEntity;

public class ModBlockEntities {
    public static final BlockEntityType<IslandSpawnerBlockEntity> ISLAND_SPAWNER_BLOCK_ENTITY =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(FlyingIslands.MOD_ID,
                            "island_spawner_be"),
                    FabricBlockEntityTypeBuilder.create(IslandSpawnerBlockEntity::new,
                            ModBlocks.ISLAND_SPAWNER_BLOCK).build());

    public static void registerBlockEntities() {
        FlyingIslands.LOGGER.info("Registering Block Entities for " + FlyingIslands.MOD_ID);
    }
}