# LobbyLynx
LobbyLynx Plugin Plan
Overview

LobbyLynx is designed to enhance the lobby experience in Minecraft by providing players with seamless navigation, social interaction features, and a customizable environment. The plugin offers robust tools for admins to manage settings, player interactions, and server events effectively.
Core Features and Enhancements
1. Navigator System

    Purpose: Allows players to teleport to different servers or locations.
    Admin Commands:
        /lynx set minigame <name> <slot> <item> <x> <y> <z>: Set minigame locations and GUI slots.
        /lynx set lobbyspawn <slot> <item> <x> <y> <z>: Define the main lobby spawn.
    Messages:
        Success: “Lynx: Spawn set successfully!”
        Error: “Lynx: Invalid coordinates or item.”
    Player Commands:
        /lobby, /hub, /spawn: Teleports players to the main lobby spawn.
    Features:
        Configurable GUI with teleport icons and tooltips.
        Dynamic updates for minigame availability.
        Teleport confirmation prompts.
        Customizable messages for teleportation and errors.

2. Custom Tab List

    Tab Layout: Configurable tab layout with placeholders like [PlayerName], [Rank], and [ServerName].
    Dynamic Content: Automatically updates with player count, server MOTD, and date.
    Commands:
        /lynx tab reload: Reloads tab configuration.
    Messages: “Lynx: Tab list refreshed.”
    Enhancements:
        Multiple tab layouts based on player count or time of day.
        Dynamic colors for ranks and statuses.

3. Custom Scoreboard

    Scoreboard Design: Configurable display with server name, rank, and stats.
    Admin Commands:
        /lynx scoreboard reload: Reload scoreboard layout.
    Messages: “Lynx: Scoreboard updated successfully!”
    Enhancements:
        Real-time updates without needing to reload.
        Configurable display duration for specific stats.

4. Friend System

    GUI and Interaction: A “Friends” player head item in slot 9 opens a GUI for friend interactions.
    Player Commands:
        /friend list: Lists all friends.
        /friend add <player>: Sends a friend request.
        /friend remove <player>: Removes a friend.
        /friend requests: Lists pending friend requests.
        /friend accept <player>: Accepts a friend request.
    Storage: Store friends data in .yml files.
    Messages: Notify players for friend requests, acceptance, or rejections.
    Enhancements:
        Notifications for friends going online/offline.
        Block/unblock feature for unwanted interactions.

5. Clan System

    Commands and GUI:
        /clan create <name>: Creates a clan.
        /clan invite <player>: Invites a player to the clan.
        /clan list: Lists clans or clan members.
        /clan chat: Enables clan-only chat.
    Clan Data Storage: Store clan data in .yml or database for persistent records.
    Messages: Notify users of clan invitations, joins, or errors.
    Enhancements:
        Clan ranks and roles with specific permissions.
        Clan events and challenges with rewards.

6. Coin System

    Economy for Rewards: Players earn coins from voting, events, or achievements. Coins can be spent on items, cosmetics, or abilities.
    Admin Commands:
        /lynx coins give <player> <amount>: Adds coins.
        /lynx coins take <player> <amount>: Removes coins.
    Player GUI: A “Wallet” GUI showing balance and transaction history.
    Messages: Confirm coin transactions, balance checks, and errors.
    Enhancements:
        Coin multipliers for events.
        Detailed transaction history.

7. Cosmetic System

    Inventory Item: A “Cosmetics” chest in slot 5.
    GUI Interface: Opens a “Cosmetics” menu with purchasable items like hats, trails, and particle effects.
    Messages: Notify players when items are equipped, removed , or insufficient coins.
    Enhancements:
        Preview feature for cosmetics.
        Limited-time offers for seasonal or event-based cosmetics.

8. Voting System with NPCs

    Spawn NPC: /lynx spawn vote npc: Creates a voting NPC.
    Vote GUI: Clicking the NPC opens a GUI for voting options and rewards.
    Messages: Show successful voting and reward claims.
    Enhancements:
        Voting history tracking to prevent abuse.
        Reward tiers based on the number of votes.

9. BungeeCord Sign System

    Admin Commands:
        /lynx bungeesign <server>: Links a sign to a BungeeCord server.
        /lynx removebungeesign: Removes the linked sign.
    Sign Placeholders:
        %cplayers%: Current player count.
        %mplayers%: Maximum player count.
        %motd%: Server message.
    Permissions:
        Admin: lynxBungeeSigns.create, lynxBungeeSigns.remove.
        Player: lynxBungeeSigns.use.
    Messages: Confirm sign creation, errors, and removals.
    Enhancements:
        Sign customization options (e.g., colors, text styles).
        Cooldowns for using signs to prevent spamming.

10. Lynx Settings GUI for Admins

    Settings Menu: /lynx settings: Opens a GUI for various configuration options.
    GUI Features:
        Toggle features on/off.
        Access logs.
        Manage Friends/Clans/Coins settings.
    Messages: “Settings saved successfully” or “Error: Invalid configuration.”
    Enhancements:
        Backup and restore options for settings.
        Change log for settings adjustments.

Additional Enhancements
Event System

    Admin Command: /lynx createevent <name>: Creates a timed event with rewards.
    Features:
        Schedule announcements.
        Add custom rewards.
        Event-specific tasks.
    Messages: Notify players of event start/end and rewards.
    Enhancements:
        Event types (e.g., PvP tournaments, treasure hunts) with unique rules and rewards.
        Event notifications through the tab list or scoreboard.

Mini-Game Stats Tracker

    Stat Commands: /lynx stats <minigame>: Shows player stats.
    Features:
        Tracks player-specific stats like kills, score, or completion time.
    Messages: Display player achievements and rankings.
    Enhancements:
        Leaderboard feature for each minigame.
        Achievements based on mini-game performance.

Admin Messaging System

    Announcements: /lynx announce <message>: Broadcasts formatted server announcements.
    Features:
        Allows admin to make colorful announcements, event notices, or maintenance alerts.
    Messages: Confirms the announcement broadcast.
    Enhancements:
        Scheduled messages for future broadcast.
        Customizable formats for announcements.

Enhanced Spawn Management

    Spawn Commands: /lynx set <area> spawn: Sets unique spawn points for different zones or events.
    Features:
        Area-specific welcome messages for players.
    Messages: Confirms successful area setup or errors.
    Enhancements:
        Spawn protection to prevent player attacks or disruptions.
        Custom spawn effects (e.g., particle effects, sound effects).

Permissions Overview
Player Permissions

    Basic Commands: /friend commands, /msg, /lobby, /hub, and /spawn.
    BungeeSigns: lynxBungeeSigns.use to teleport via BungeeCord signs.
    Advanced Interaction: Permissions for using specific features (e.g., cosmetics, voting) can be fine-tuned for different player ranks.

Admin Permissions

    Control Commands: /lynx commands like setting spawn points, managing economy, configuring navigation.
    BungeeSigns: lynxBungeeSigns.create, lynxBungeeSigns.remove for server link management.
    Granular Control: Provide detailed permissions for managing specific features (e.g., managing clans, viewing player stats).

