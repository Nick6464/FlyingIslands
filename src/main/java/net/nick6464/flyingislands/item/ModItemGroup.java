package net.nick6464.flyingislands.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.nick6464.flyingislands.FlyingIslands;
import net.nick6464.flyingislands.block.ModBlocks;

public class ModItemGroup {

    public static ItemGroup ISLAND_SPAWNER = Registry.register(Registries.ITEM_GROUP,
            new Identifier(FlyingIslands.MOD_ID, "island_group"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.flyingislands"))
                    .icon(() -> new ItemStack(ModItems.ISLAND_SPAWNER)).entries((displayContext, entries) -> {
                        entries.add(ModItems.ISLAND_SPAWNER);

                        entries.add(ModBlocks.ISLAND_SPAWNER_BLOCK);
                    }).build());


    public static void registerItemGroup() {
        ISLAND_SPAWNER = FabricItemGroup.builder()
                .displayName(Text.literal("Flying Islands"))
                .icon(() -> new ItemStack(ModItems.ISLAND_SPAWNER)).build();
    }
}

