# Installation Guide for LobbyLynx

This guide will walk you through the process of installing LobbyLynx on your Minecraft server.

## Prerequisites

- A Minecraft server running Spigot or Paper (version 1.16 or higher)
- Server operator status or file access to your server

## Step-by-Step Installation

1. **Download LobbyLynx**
   - Go to the [LobbyLynx GitHub Releases page](https://github.com/yourusername/LobbyLynx/releases)
   - Download the latest version of `LobbyLynx.jar`

2. **Install the Plugin**
   - Stop your Minecraft server if it's running
   - Navigate to your server's `plugins` folder
   - Copy the `LobbyLynx.jar` file into this folder

3. **Start Your Server**
   - Start your Minecraft server
   - The plugin will generate its configuration files

4. **Configure LobbyLynx**
   - Navigate to `plugins/LobbyLynx/`
   - Open `config.yml` with a text editor
   - Adjust the settings according to your preferences
   - Save the file and close it

5. **Reload the Plugin**
   - In the server console or as an OP in-game, run the command:
     ```
     /lynx reload
     ```

6. **Verify Installation**
   - In-game, run the command:
     ```
     /lynx
     ```
   - If you see the LobbyLynx help menu, the installation was successful!

## Troubleshooting

- If the plugin doesn't load, check the server console for error messages
- Ensure your server version is compatible with LobbyLynx
- Check that the `LobbyLynx.jar` file is in the correct directory

For more help, please refer to our [FAQ](FAQ.md) or [open an issue](https://github.com/yourusername/LobbyLynx/issues) on our GitHub page.
