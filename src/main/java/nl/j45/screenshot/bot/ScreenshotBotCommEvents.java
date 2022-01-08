package nl.j45.screenshot.bot;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;


public final class ScreenshotBotCommEvents {
	/**
	 * Called when a connection to the ScreenshotBot Comm socket is started.
	 */
	public static final Event<ScreenshotBotCommEvents.Start> COMM_START = EventFactory.createArrayBacked(ScreenshotBotCommEvents.Start.class, callbacks -> () -> {
		for (ScreenshotBotCommEvents.Start callback : callbacks) {
			callback.onCommStart();
		}
	});

	/**
	 * Called when a connection to the ScreenshotBot Comm socket is closed.
	 */
	public static final Event<ScreenshotBotCommEvents.End> COMM_END = EventFactory.createArrayBacked(ScreenshotBotCommEvents.End.class, callbacks -> () -> {
		for (ScreenshotBotCommEvents.End callback : callbacks) {
			callback.onCommEnd();
		}
	});

	/**
	 * Called when a message is sent to the ScreenshotBot Comm socket.
	 */
	public static final Event<ScreenshotBotCommEvents.Message> COMM_MESSAGE = EventFactory.createArrayBacked(ScreenshotBotCommEvents.Message.class, callbacks -> (message) -> {
		for (ScreenshotBotCommEvents.Message callback : callbacks) {
			callback.onCommMessage(message);
		}
	});

	@FunctionalInterface
	public interface Start {
		void onCommStart();
	}

	@FunctionalInterface
	public interface End {
		void onCommEnd();
	}

	@FunctionalInterface
	public interface Message {
		void onCommMessage(String message);
	}
}
