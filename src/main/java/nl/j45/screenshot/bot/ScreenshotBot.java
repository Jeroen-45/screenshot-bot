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

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;


public class ScreenshotBot implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("screenshotbot");
	private static MinecraftClient client = MinecraftClient.getInstance();

	private long lastChunkLoadTime = 0L;
	private long maxChunkLoadDuration = 0L;
	private String pendingScreenshotFilename = null;

	private ScreenshotBotComm comm;

	@Override
	public void onInitialize() {
		this.comm = new ScreenshotBotComm(4000);
		this.comm.start();

		ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
			/* Update chunk loading time info upon each chunk load */
			long chunkLoadDuration = System.nanoTime() - this.lastChunkLoadTime;
			if (this.lastChunkLoadTime != 0 && chunkLoadDuration > this.maxChunkLoadDuration) {
				this.maxChunkLoadDuration = chunkLoadDuration;
			}
			this.lastChunkLoadTime = System.nanoTime();
		});

		ScreenshotBotCommEvents.COMM_MESSAGE.register((message) -> {
			/* Handle command through socket */
			if (message.startsWith("/")) {
				runClientCommand(message);
			} else if (message.startsWith("connect ")) {
				serverConnect(message.split("\\s")[1]);
			} else if (message.startsWith("screenshot ")) {
				this.takeScreenshotAwaitChunkloading(message.split("\\s")[1]);
			}
		});
	}

	public void takeScreenshotAwaitChunkloading(String filename) {
		if (this.pendingScreenshotFilename != null) {
			/* Another screenshot is already pending */
			return;
		}

		this.pendingScreenshotFilename = filename;
		this.takeScreenshotAwaitChunkloading(true);
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
			/* No new chunks were loaded for at least the highest time
			 * between chunk loads, at least half the chunks that should be
			 * loaded based on the render distance are actually loaded,
			 * and no chunks are currently being rendered.
			 * We assume chunk loading activity is done, take the screenshot. */
			this.comm.sendMessage("Chunks loaded");
			takeScreenshot(this.pendingScreenshotFilename);
			this.comm.sendMessage("Screenshot taken");
			this.pendingScreenshotFilename = null;
		} else {
			/* Chunk loading isn't finished, wait and check again later. */
			long waitTime = this.maxChunkLoadDuration / 1000000;
			if (waitTime == 0) {
				waitTime = 250L;
			}

			try {
				Thread.sleep(waitTime);
			} catch (InterruptedException e) {
				LOGGER.error("Thread interrupted while waiting for next chunk load check");
			}
			this.takeScreenshotAwaitChunkloading(false);
		}
	}

	public static Consumer<Text> simpleLogConsumer = value -> {
		LOGGER.info(value);
	};

	/* Hide the HUD, then take a screenshot and store it with the given filename. */
	public static void takeScreenshot(String filename) {
		client.options.hudHidden = true;

		Framebuffer framebuffer = client.getFramebuffer();
		ScreenshotRecorder.saveScreenshot(client.runDirectory, filename, framebuffer, simpleLogConsumer);
	}

	/* Connect to the server at the given address. */
	public static void serverConnect(String address) {
		client.execute(() -> {
			MultiplayerScreen mpScreen = new MultiplayerScreen(new TitleScreen());
			ServerAddress serverAddress = ServerAddress.parse(address);
			ServerInfo server = new ServerInfo("", address, true);
			ConnectScreen.connect(mpScreen, client, serverAddress, server);
		});
	}

	/* Run the given command as if it were input in the client chat. */
	public static void runClientCommand(String command) {
		client.player.sendChatMessage(command);
	}
}
