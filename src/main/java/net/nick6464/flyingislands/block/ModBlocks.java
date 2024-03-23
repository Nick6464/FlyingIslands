package net.nick6464.flyingislands.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.nick6464.flyingislands.FlyingIslands;
import net.nick6464.flyingislands.block.custom.IslandSpawnerBlock;

public class ModBlocks {

    public static final Block ISLAND_SPAWNER_BLOCK = registerBlock("island_spawner_block",
            new IslandSpawnerBlock(FabricBlockSettings.copyOf(Blocks.BARRIER)));
    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(FlyingIslands.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, new Identifier(FlyingIslands.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }

    public static void registerModBlocks() {
        FlyingIslands.LOGGER.info("Registering ModBlocks for " + FlyingIslands.MOD_ID);
    }
}
