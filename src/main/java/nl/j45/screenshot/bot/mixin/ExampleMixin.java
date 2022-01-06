package nl.j45.screenshot.bot.mixin;

import nl.j45.screenshot.bot.ScreenshotBot;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class ExampleMixin {
	@Inject(at = @At("HEAD"), method = "init()V")
	private void init(CallbackInfo info) {
		ScreenshotBot.LOGGER.info("This line is printed by an example mod mixin!");
		ScreenshotBot.takeScreenshot("TESTTT.png");
		ScreenshotBot.serverConnect("192.168.1.212");
	}
}
