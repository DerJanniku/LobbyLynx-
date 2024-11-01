# LobbyLynx

LobbyLynx is a comprehensive Minecraft server lobby management plugin that provides essential features for managing your server's lobby experience.

## Features

- **Friend System**
  - Add/remove friends
  - Friend requests
  - Friend groups
  - Favorites system
  - Activity feed
  - Friend statistics
  - Privacy settings

- **Navigator System**
  - Custom GUI for server navigation
  - Configurable teleport locations
  - Server signs with status display
  - Animated sign support

- **Settings Management**
  - Customizable game rules
  - Server-wide settings
  - Player-specific settings
  - Permission-based access control

- **Cosmetics**
  - Hat system
  - More cosmetics planned

- **Admin Tools**
  - Comprehensive command system
  - Server sign management
  - Configuration reload system
  - Detailed permissions system

## Commands

- `/lynx` - Main plugin command
  - `/lynx navigator` - Open navigator GUI
  - `/lynx settings` - Open settings GUI
  - `/lynx gamerules` - Open gamerules GUI
  - `/lynx set minigame` - Set minigame location
  - `/lynx set lobbyspawn` - Set lobby spawn
  - `/lynx reload` - Reload configuration
  - `/lynx serversign` - Manage server signs

- `/friend` - Friend system commands
  - `/friend add <player>` - Send friend request
  - `/friend remove <player>` - Remove friend
  - `/friend list` - View friends list

## Installation

1. Download the latest version of LobbyLynx
2. Place the .jar file in your server's plugins folder
3. Restart your server
4. Configure the plugin in the config.yml file

## Configuration

The plugin uses several configuration files:
- `config.yml` - Main configuration
- `friends.yml` - Friend system data
- `signs.yml` - Server sign configuration
- `stats.yml` - Statistics storage

## Permissions

Basic permissions:
- `lynx.navigator` - Access to navigator
- `lynx.settings` - Access to settings
- `lynx.gamerules` - Access to game rules
- `lynx.reload` - Permission to reload plugin
- `lynx.serversigns.admin` - Manage server signs

## Support

For support, please create an issue on our GitHub repository or contact us through:
- Discord: [Link to Discord]
- Email: [Contact Email]

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
