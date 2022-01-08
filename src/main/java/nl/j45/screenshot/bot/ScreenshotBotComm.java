package nl.j45.screenshot.bot;

import nl.j45.screenshot.bot.ScreenshotBotCommEvents;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.*;


public class ScreenshotBotComm extends Thread {
	public static final Logger LOGGER = LogManager.getLogger("screenshotbot");
	private int port;
	private ServerSocket ss;
	private Socket s;
	private BufferedReader in = null;
	private BufferedWriter out = null;

	public ScreenshotBotComm(int port) {
		this.port = port;
	}

	/* Send a message to the current client if there is one */
	public void sendMessage(String message) {
		if (this.out == null) return;
		try {
			this.out.write(message);
			this.out.newLine();
			this.out.flush();
		} catch (IOException e) {
			LOGGER.error("Socket error");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		/* Open server socket */
		try {
			ss = new ServerSocket(this.port);
		} catch (IOException e) {
			LOGGER.error("Could not open server socket");
			return;
		}

		/* Keep listening for client connections */
		while (true) {
			this.s = null;

			try {
				/* Wait for new client connection */
				this.s = ss.accept();

				/* Client connected */
				LOGGER.info("A new client connected: " + s);
				ScreenshotBotCommEvents.COMM_START.invoker().onCommStart();

				/* Obtain input and output streams */
				DataInputStream is = new DataInputStream(s.getInputStream());
				DataOutputStream os = new DataOutputStream(s.getOutputStream());
				this.in = new BufferedReader(new InputStreamReader(is));
				this.out = new BufferedWriter(new OutputStreamWriter(os));

				/* Read messages and pass them to the event invoker. */
				String message;
				while ((message = this.in.readLine()) != null) {
					ScreenshotBotCommEvents.COMM_MESSAGE.invoker().onCommMessage(message);
				}

				/* Socket was closed */
				ScreenshotBotCommEvents.COMM_END.invoker().onCommEnd();
				this.in.close();
				this.in = null;
				this.out.close();
				this.out = null;
				this.s.close();
			} catch (IOException e) {
				LOGGER.error("Socket error");
				e.printStackTrace();
			}
		}
	}
}