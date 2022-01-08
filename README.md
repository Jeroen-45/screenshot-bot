# ScreenshotBot
A Minecraft Fabric mod that adds a simple socket server to your Minecraft client that can be used to let the client join or leave specific servers, take screenshots and run arbitrary commands that could normally be typed in chat. When taking a screenshot the mod first waits for chunk loading activity to stop. This mod is particularly useful for aiding automation of screenshotting using external programs, but could also be used for any other program you're making that needs an easy way to interface with a Minecraft client to join/leave minecraft servers and/or send client commands.

## Usage
Connect to port **4000** and send any of the following lines:
- `connect example.com`: Connect to the given server. Works with any server address that can also be connected to manually via 'direct connect'.
- `disconnect`: Disconnect from the current server.
- `/command`: Run the given in-game command. Any line starting with `/` will be processed as such a command.
- `screenshot filename.png`: Wait until no chunk loading activity is detected anymore, then take a screenshot and save it with the given filename (in the normal Minecraft screenshot directory).
