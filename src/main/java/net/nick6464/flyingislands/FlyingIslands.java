package net.nick6464.flyingislands;

import net.fabricmc.api.ModInitializer;
import net.nick6464.flyingislands.item.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlyingIslands implements ModInitializer {

	public static final String MOD_ID = "flyingislands";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override

	public void onInitialize() {
		ModItems.registerModItems();
	}
}
