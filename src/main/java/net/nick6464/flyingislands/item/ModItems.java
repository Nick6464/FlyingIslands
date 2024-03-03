package net.nick6464.flyingislands.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.nick6464.flyingislands.FlyingIslands;
import net.nick6464.flyingislands.item.custom.IslandDeleterItem;
import net.nick6464.flyingislands.item.custom.IslandSpawnerItem;

public class ModItems {
    public static final Item ISLAND_SPAWNER = registerItem("island_spawner",
            new IslandSpawnerItem(new FabricItemSettings().maxCount(1).group(ModItemGroup.MOD_ITEM_GROUP)));
    public static final Item ISLAND_DELETER = registerItem("island_deleter",
            new IslandDeleterItem(new FabricItemSettings().maxCount(1).group(ModItemGroup.MOD_ITEM_GROUP)));


    public static Item registerItem(String name, Item item) {
        return Registry.register(Registry.ITEM, new Identifier(FlyingIslands.MOD_ID, name), item);
    }

    public static void registerModItems() {
        FlyingIslands.LOGGER.info("Registering mod items for " + FlyingIslands.MOD_ID + "...");
    }
}
