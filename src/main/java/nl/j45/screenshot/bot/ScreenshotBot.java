package nl.j45.screenshot.bot;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.MinecraftClient;

import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.client.gl.Framebuffer;
import java.util.function.Consumer;
import net.minecraft.text.Text;


public class ScreenshotBot implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LogManager.getLogger("screenshotbot");
	private static MinecraftClient client = MinecraftClient.getInstance();

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
	}

	public static void serverConnect(String addr) {
		final ServerInfo server = new ServerInfo("", addr, true);
		ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), client, ServerAddress.parse(server.address), server);
	}

	public static Consumer<Text> simpleLogConsumer = value -> {
		LOGGER.info(value);
	};

	public static void takeScreenshot(String filename) {
		final Framebuffer framebuffer = client.getFramebuffer();
		ScreenshotRecorder.saveScreenshot(client.runDirectory, filename, framebuffer, simpleLogConsumer);
	}
}
