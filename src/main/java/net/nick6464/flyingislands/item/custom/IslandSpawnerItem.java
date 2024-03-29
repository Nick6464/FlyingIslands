package net.nick6464.flyingislands.item.custom;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.nick6464.flyingislands.FlyingIslands;
import net.minecraft.entity.player.PlayerEntity;

public class IslandSpawnerItem extends Item {

    public int SEED = 998;
//    public int SEED = 674;
    public IslandSpawnerItem(Settings settings) {
        super(settings);
    }


    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!context.getWorld().isClient()) {
            FlyingIslands.LOGGER.info("Using IslandSpawnerItem on block");
            FlyingIsland flyingIsland = new FlyingIsland(SEED, context);
            flyingIsland.generateIsland();
            if (context.getHand() == Hand.MAIN_HAND) {
                flyingIsland.placeIsland(context.getWorld(), context.getBlockPos());
                IslandDecorators decorator = new IslandDecorators(flyingIsland);
                try {
                    decorator.randomDecorator(flyingIsland.random,
                            new BlockPos(context.getBlockPos().getX() - flyingIsland.ISLAND_RADIUS,
                                    100,
                                    context.getBlockPos().getZ() - flyingIsland.ISLAND_RADIUS));
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
            else if (context.getHand() == Hand.OFF_HAND) {
                flyingIsland.deleteIsland(context.getWorld(), context.getBlockPos());
                return ActionResult.SUCCESS;
            }
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }

    // When shift right-clicking not at a block, randomise the SEED and display it in the chat
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        // If the player is sneaking and not in the air
        if (user.isSneaking() && !world.isClient()) {
            // Randomise the SEED
            setSEED((int) (Math.random() * Integer.MAX_VALUE));
            user.sendMessage(Text.of("SEED: " + SEED), false);
            FlyingIslands.LOGGER.info("SEED: " + SEED);
        }
        return super.use(world, user, hand);
    }

    public void setSEED(int SEED) {
        this.SEED = SEED;
    }
}
