package net.nick6464.flyingislands.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.nick6464.flyingislands.FlyingIslands;
import net.nick6464.flyingislands.item.custom.IslandSpawnerItem;

public class ModItems {
    public static final Item ISLAND_SPAWNER = registerItem("island_spawner",
            new IslandSpawnerItem(new FabricItemSettings().maxCount(1)));

    public static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(FlyingIslands.MOD_ID, name), item);
    }

    public static void registerModItems() {
        FlyingIslands.LOGGER.info("Registering mod items for " + FlyingIslands.MOD_ID + "...");

    }
}
