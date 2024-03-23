package net.nick6464.flyingislands;

import net.fabricmc.api.ModInitializer;
import net.nick6464.flyingislands.block.ModBlocks;
import net.nick6464.flyingislands.block.entity.ModBlockEntities;
import net.nick6464.flyingislands.item.ModItemGroup;
import net.nick6464.flyingislands.item.ModItems;
import net.nick6464.flyingislands.structure.ModGenerators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlyingIslands implements ModInitializer {

	public static final String MOD_ID = "flyingislands";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override

	public void onInitialize() {
		ModItems.registerModItems();
		ModItemGroup.registerItemGroup();
		ModGenerators.registerGenerators();
		ModBlocks.registerModBlocks();
		ModBlockEntities.registerBlockEntities();
		LOGGER.info("Flying Islands Mod has been initialized");
	}
}
