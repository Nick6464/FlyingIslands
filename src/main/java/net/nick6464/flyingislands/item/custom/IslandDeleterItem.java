package net.nick6464.flyingislands.item.custom;

import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;

import static net.nick6464.flyingislands.item.custom.IslandSpawnerItem.generateIsland;

public class IslandDeleterItem extends Item {

        public IslandDeleterItem(Settings settings) {
            super(settings);
        }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {

        boolean[][][] blocks = generateIsland();

        deleteBlocks(context, blocks);

        return ActionResult.SUCCESS;
    }

    public void deleteBlocks(ItemUsageContext context, boolean[][][] blocks) {
        for (int x = 0; x < IslandSpawnerItem.ISLAND_CONTAINER_SIZE; x++) {
            for (int y = 0; y < IslandSpawnerItem.ISLAND_CONTAINER_SIZE; y++) {
                for (int z = 0; z < IslandSpawnerItem.ISLAND_CONTAINER_SIZE; z++) {
                    if (blocks[x][y][z]) {
                        context.getWorld().removeBlock(context.getBlockPos().add(x, y, z), false);
                    }
                }
            }
        }
    }
}
