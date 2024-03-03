package net.nick6464.flyingislands.mixin;

<<<<<<< Updated upstream:src/main/java/net/fabricmc/example/mixin/ExampleMixin.java
=======
import net.nick6464.flyingislands.FlyingIslands;
>>>>>>> Stashed changes:src/main/java/net/nick6464/flyingislands/mixin/ExampleMixin.java
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class ExampleMixin {
	@Inject(at = @At("HEAD"), method = "init()V")
	private void init(CallbackInfo info) {
<<<<<<< Updated upstream:src/main/java/net/fabricmc/example/mixin/ExampleMixin.java
		System.out.println("This line is printed by an example mod mixin!");
=======
		FlyingIslands.LOGGER.info("This line is printed by an example mod mixin!");
>>>>>>> Stashed changes:src/main/java/net/nick6464/flyingislands/mixin/ExampleMixin.java
	}
}
