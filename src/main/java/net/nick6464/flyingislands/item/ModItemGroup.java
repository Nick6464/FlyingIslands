package net.nick6464.flyingislands.item;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.nick6464.flyingislands.FlyingIslands;

public class ModItemGroup {
    public static final ItemGroup MOD_ITEM_GROUP = FabricItemGroupBuilder.build(
            new Identifier(FlyingIslands.MOD_ID, "flyingislands"),
            () -> new ItemStack(ModItems.ISLAND_SPAWNER));
}

