# LobbyLynx Permissions Guide

This document outlines all the permissions used by LobbyLynx.

## General Permissions

- `lynx.use` - Allows use of basic LobbyLynx commands
- `lynx.admin` - Grants access to all admin functionalities

## Navigator Permissions

- `lynx.navigator` - Allows access to the navigator GUI
- `lynx.navigator.use.<minigame>` - Allows use of specific minigame teleporters

## Settings Permissions

- `lynx.settings` - Allows access to the settings GUI
- `lynx.settings.flight` - Allows toggling of flight mode
- `lynx.settings.pvp` - Allows toggling of PvP mode

## Game Rule Permissions

- `lynx.gamerules` - Allows access to the game rules GUI
- `lynx.gamerules.edit` - Allows editing of game rules

## Friend System Permissions

- `lynx.friend.use` - Allows use of the friend system
- `lynx.friend.teleport` - Allows teleporting to friends
- `lynx.friend.manage` - Allows managing friends (add, remove, block)

## Cosmetic Permissions

- `lynx.hat` - Allows use of the hat system
- `lynx.hat.<hatname>` - Allows use of a specific hat

## Admin Permissions

- `lynx.reload` - Allows reloading of the plugin configuration
- `lynx.setminigame` - Allows setting of minigame locations
- `lynx.setlobbyspawn` - Allows setting of the lobby spawn point
- `lynx.serversigns.admin` - Allows creation and removal of server signs

## Other Permissions

- `lynx.bypass.cooldown` - Bypasses cooldowns on commands and actions
- `lynx.vip` - Grants access to VIP features (if implemented)

Remember to prefix all permissions with your plugin's name (e.g., `lobbylynx.use` instead of just `lynx.use`) when implementing them in your plugin.
