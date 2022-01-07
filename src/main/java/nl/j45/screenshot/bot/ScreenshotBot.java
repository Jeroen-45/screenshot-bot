package nl.j45.screenshot.bot;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Timer;
import java.util.TimerTask;

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

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;


public class ScreenshotBot extends TimerTask implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("screenshotbot");
	private static final Timer timer = new Timer();
	private static MinecraftClient client = MinecraftClient.getInstance();

	private long lastChunkLoadTime = 0L;
	private long maxChunkLoadDuration = 0L;
	private String pendingScreenshotFilename = null;

	@Override
	public void onInitialize() {
		ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
			/* Update chunk loading time info upon each chunk load */
			long chunkLoadDuration = System.nanoTime() - this.lastChunkLoadTime;
			if (this.lastChunkLoadTime != 0 && chunkLoadDuration > this.maxChunkLoadDuration) {
				this.maxChunkLoadDuration = chunkLoadDuration;
			}
			this.lastChunkLoadTime = System.nanoTime();
		});
	}

	public void takeScreenshotAwaitChunkloading(String filename) {
		if (this.pendingScreenshotFilename != null) {
			/* Another screenshot is already pending */
			return;
		}

		this.pendingScreenshotFilename = filename;
		takeScreenshotAwaitChunkloading(true);
	}

	public void run() {
		/* TimerTask run to check for chunk loading to be done after waiting */
		this.takeScreenshotAwaitChunkloading(false);
	}

	public void takeScreenshotAwaitChunkloading(boolean reset) {
		if (reset) {
			this.lastChunkLoadTime = 0L;
			this.maxChunkLoadDuration = 0L;
		}

		/* Check if chunk loading has finished */
		int renderDistance = client.options.viewDistance;
		int targetChunksLoaded = (int)Math.pow(renderDistance * 2 + 1, 2) / 2;
		int chunksLoaded = client.worldRenderer.getCompletedChunkCount();
		if (chunksLoaded >= targetChunksLoaded
				&& System.nanoTime() - this.lastChunkLoadTime >= this.maxChunkLoadDuration * 2
				&& client.worldRenderer.isTerrainRenderComplete()) {
			/* No new chunks were loaded for at least twice the highest time
			 * between chunk loads, at least half the chunks that should be
			 * loaded based on the render distance are actually loaded,
			 * and no chunks are currently being rendered.
			 * We assume chunk loading activity is done, take the screenshot. */
			takeScreenshot(this.pendingScreenshotFilename);
			this.pendingScreenshotFilename = null;
		} else {
			/* Chunk loading isn't finished, wait again. */
			long waitTime = (this.maxChunkLoadDuration * 2) / 1000000;
			if (waitTime == 0) {
				waitTime = 250L;
			}
			timer.schedule(this, waitTime);
		}
	}

	public static Consumer<Text> simpleLogConsumer = value -> {
		LOGGER.info(value);
	};

	/* Hide the HUD, then take a screenshot and store it with the given filename,
	 * then return the HUD to its previous state. */
	public static void takeScreenshot(String filename) {
		boolean oldHudHidden = client.options.hudHidden;
		client.options.hudHidden = true;

		Framebuffer framebuffer = client.getFramebuffer();
		ScreenshotRecorder.saveScreenshot(client.runDirectory, filename, framebuffer, simpleLogConsumer);

		client.options.hudHidden = oldHudHidden;
	}

	/* Connect to the server at the given address. */
	public static void serverConnect(String address) {
		MultiplayerScreen mpScreen = new MultiplayerScreen(new TitleScreen());
		ServerAddress serverAddress = ServerAddress.parse(address);
		ServerInfo server = new ServerInfo("", address, true);
		ConnectScreen.connect(mpScreen, client, serverAddress, server);
	}

	/* Run the given command as if it were input in the client chat. */
	public static void runClientCommand(String command) {
		client.player.sendChatMessage(command);
	}
}
