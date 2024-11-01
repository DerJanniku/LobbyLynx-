package org.derjannik.lobbyLynx.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.derjannik.lobbyLynx.managers.FriendManager;
import org.derjannik.lobbyLynx.util.FriendStatistics;
import org.derjannik.lobbyLynx.LobbyLynx;

import java.util.*;

public class FriendGUI implements Listener {
    private final LobbyLynx plugin;
    private final FriendManager friendManager;
    private final int ITEMS_PER_PAGE = 45;
    private final Map<UUID, GUIType> activeGUIs = new HashMap<>();
    private final Map<UUID, String> selectedFriends = new HashMap<>();
    private final Map<UUID, Integer> currentPageMap = new HashMap<>();

    public enum GUIType {
        MAIN,
        REQUESTS,
        SETTINGS,
        BLOCKED,
        FAVORITES,
        GROUPS,
        ACTIVITY_FEED,
        STATISTICS,
        SEARCH,
        FRIEND_OPTIONS
    }

    public FriendGUI(LobbyLynx plugin, FriendManager friendManager) {
        this.plugin = plugin;
        this.friendManager = friendManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startHeadUpdateTask();
    }

    private void startHeadUpdateTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getOpenInventory().getTitle().contains("Friends")) {
                    int currentPage = getCurrentPage(player);
                    openMainGUI(player, currentPage); // Refresh the GUI
                }
            }
        }, 20L * 30, 20L * 30); // Update every 30 seconds
    }

    public void openGUI(Player player, GUIType type) {
        openGUI(player, type, 0);
    }

    public void openGUI(Player player, GUIType type, int page) {
        activeGUIs.put(player.getUniqueId(), type);
        currentPageMap.put(player.getUniqueId(), page);

        switch (type) {
            case MAIN:
                openMainGUI(player, page);
                break;
            case REQUESTS:
                openRequestsGUI(player, page);
                break;
            case SETTINGS:
                openSettingsGUI(player);
                break;
            case BLOCKED:
                openBlockedGUI(player, page);
                break;
            case FAVORITES:
                openFavoritesGUI(player, page);
                break;
            case GROUPS:
                openGroupsGUI(player, page);
                break;
            case ACTIVITY_FEED:
                openActivityFeedGUI(player, page);
                break;
            case STATISTICS:
                openStatisticsGUI(player);
                break;
            case SEARCH:
                openSearchGUI(player);
                break;
        }
    }

    private void openMainGUI(Player player, int page) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Friends - Page " + (page + 1));
        List<String> friends = friendManager.getFriends(player.getName());

        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, friends.size());

        for (int i = startIndex; i < endIndex; i++) {
            String friendName = friends.get(i);
            ItemStack head = createPlayerHead(friendName);
            gui.addItem(head);
        }

        addNavigationItems(gui, page, friends.size());

        gui.setItem(45, createGuiItem(Material.BOOK, "Friend Requests"));
        gui.setItem(46, createGuiItem(Material.BARRIER, "Blocked Players"));
        gui.setItem(47, createGuiItem(Material.GOLDEN_APPLE, "Favorites"));
        gui.setItem(48, createGuiItem(Material.CHEST, "Groups"));
        gui.setItem(49, createGuiItem(Material.PAPER, "Activity Feed"));
        gui.setItem(50, createGuiItem(Material.REDSTONE, "Settings"));
        gui.setItem(51, createGuiItem(Material.COMPASS, "Search"));
        gui.setItem(52, createGuiItem(Material.EXPERIENCE_BOTTLE, "Statistics"));

        player.openInventory(gui);
    }

    private void openFavoritesGUI(Player player, int page) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Favorites - Page " + (page + 1));
        List<String> favorites = friendManager.getFavorites(player.getName());

        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, favorites.size());

        for (int i = startIndex; i < endIndex; i++) {
            String favoriteName = favorites.get(i);
            ItemStack head = createPlayerHead(favoriteName);
            gui.addItem(head);
        }

        addNavigationItems(gui, page, favorites.size());
        gui.setItem(49, createGuiItem(Material.ARROW, "Back to Main Menu"));

        player.openInventory(gui);
    }

    private void openBlockedGUI(Player player, int page) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Blocked Players - Page " + (page + 1));
        List<String> blockedPlayers = new ArrayList<>(friendManager.getBlockedPlayers(player.getName()));

        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, blockedPlayers.size());

        for (int i = startIndex; i < endIndex; i++) {
            String blockedName = blockedPlayers.get(i);
            ItemStack head = createPlayerHead(blockedName);
            gui.addItem(head);
        }

        addNavigationItems(gui, page, blockedPlayers.size());
        gui.setItem(49, createGuiItem(Material.ARROW, "Back to Main Menu"));

        player.openInventory(gui);
    }

    private void openSettingsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Settings");
        gui.setItem(11, createGuiItem(Material.NAME_TAG, "Change Username"));
        gui.setItem(13, createGuiItem(Material.GOLDEN_APPLE, "Toggle Notifications"));
        gui.setItem(15, createGuiItem(Material.BARRIER, "Clear Blocked Players"));
        gui.setItem(26, createGuiItem(Material.ARROW, "Back to Main Menu"));

        player.openInventory(gui);
    }

    private void openRequestsGUI(Player player, int page) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Friend Requests - Page " + (page + 1));
        List<String> requests = friendManager.getRequests(player.getName());

        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, requests.size());

        for (int i = startIndex; i < endIndex; i++) {
            String requestName = requests.get(i);
            ItemStack head = createPlayerHead(requestName);
            gui.addItem(head);
        }

        addNavigationItems(gui, page, requests.size());
        gui.setItem(49, createGuiItem(Material.ARROW, "Back to Main Menu"));

        player.openInventory(gui);
    }

    private void openGroupsGUI(Player player, int page) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Friend Groups - Page " + (page + 1));
        Collection<String> groups = friendManager.getFriendGroups(player.getName());

        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, groups.size());

        List<String> groupsList = new ArrayList<>(groups);
        for (int i = startIndex; i < endIndex; i++) {
            String groupName = groupsList.get(i);
            gui.addItem(createGuiItem(Material.BOOK, groupName));
        }

        addNavigationItems(gui, page, groups.size());
        gui.setItem(49, createGuiItem(Material.ARROW, "Back to Main Menu"));

        player.openInventory(gui);
    }

    private void openActivityFeedGUI(Player player, int page) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Activity Feed - Page " + (page + 1));
        List<String> activities = friendManager.getActivityFeed(player.getName());

        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, activities.size());

        for (int i = startIndex; i < endIndex; i++) {
            String activity = activities.get(i);
            gui.addItem(createGuiItem(Material.PAPER, activity));
        }

        addNavigationItems(gui, page, activities.size());
        gui.setItem(49, createGuiItem(Material.ARROW, "Back to Main Menu"));

        player.openInventory(gui);
    }

    public void openStatisticsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Friend Statistics");
        FriendStatistics stats = friendManager.getFriendStatistics(player.getName());

        gui.setItem(10, createGuiItem(Material.DIAMOND, "Total Friends", String.valueOf(stats.getTotalFriends())));
        gui.setItem(11, createGuiItem(Material.EMERALD, "Online Friends", String.valueOf(stats.getOnlineFriends())));
        gui.setItem(12, createGuiItem(Material.GOLDEN_APPLE, "Favorite Friends", String.valueOf(stats.getFavoriteFriends())));
        gui.setItem(13, createGuiItem(Material.BOOK, "Friend Groups", String.valueOf(stats.getFriendGroups())));
        gui.setItem(14, createGuiItem(Material.CLOCK, "Average Friendship Duration", formatDuration(stats.getAverageFriendshipDuration())));
        gui.setItem(15, createGuiItem(Material.EXPERIENCE_BOTTLE, "Total Friendship Level", String.valueOf(stats.getTotalFriendshipLevel())));
        gui.setItem(16, createGuiItem(Material.NETHER_STAR, "Longest Friendship", stats.getLongestFriendshipName()));

        gui.setItem(26, createGuiItem(Material.ARROW, "Back to Main Menu"));

        player.openInventory(gui);
    }

    private void openSearchGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Search Friends");

        gui.setItem(11, createGuiItem(Material.NAME_TAG, "Search by Name"));
        gui.setItem(13, createGuiItem(Material.COMPASS, "Search by Server"));
        gui.setItem(15, createGuiItem(Material.CLOCK, "Search by Last Seen"));

        gui.setItem(26, createGuiItem(Material.ARROW, "Back to Main Menu"));

        player.openInventory(gui);
    }

    private void openFriendOptionsGUI(Player player, String friendName) {
        Inventory optionsGui = Bukkit.createInventory(null, 27,
                ChatColor.GOLD + "Options for " + friendName);

        optionsGui.setItem(10, createGuiItem(Material.PAPER, "Send Message"));
        optionsGui.setItem(11, createGuiItem(Material.GOLDEN_APPLE,
                friendManager.isFavorite(player.getName(), friendName) ?
                        "Remove from Favorites" : "Add to Favorites"));
        optionsGui.setItem(12, createGuiItem(Material.NAME_TAG, "Set Nickname"));
        optionsGui.setItem(13, createGuiItem(Material.BARRIER, "Remove Friend"));
        optionsGui.setItem(14, createGuiItem(Material.BOOK, "View Statistics"));
        optionsGui.setItem(15, createGuiItem(Material.CHEST, "Add to Group"));
        optionsGui.setItem(16, createGuiItem(Material.REDSTONE, "Block Player"));

        player.openInventory(optionsGui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        event.setCancelled(true);

        String title = event.getView().getTitle();
        if (title.startsWith(ChatColor.GOLD + "Friends")) {
            handleMainGUIClick(player, clickedItem, event.getClick());
        } else if (title.startsWith(ChatColor.GOLD + "Friend Requests")) {
            handleRequestsGUIClick(player, clickedItem, event.getClick());
        } else if (title.startsWith(ChatColor.GOLD + "Blocked Players")) {
            handleBlockedGUIClick(player, clickedItem, event.getClick());
        } else if (title.startsWith(ChatColor.GOLD + "Settings")) {
            handleSettingsGUIClick(player, clickedItem, event.getClick());
        } else if (title.startsWith(ChatColor.GOLD + "Friend Groups")) {
            handleGroupsGUIClick(player, clickedItem, event.getClick());
        } else if (title.startsWith(ChatColor.GOLD + "Activity Feed")) {
            handleActivityFeedGUIClick(player, clickedItem, event.getClick());
        } else if (title.startsWith(ChatColor.GOLD + "Friend Statistics")) {
            handleStatisticsGUIClick(player, clickedItem, event.getClick());
        } else if (title.startsWith(ChatColor.GOLD + "Search Friends")) {
            handleSearchGUIClick(player, clickedItem, event.getClick());
        } else if (title.startsWith(ChatColor.GOLD + "Options for")) {
            handleFriendOptionsGUIClick(player, clickedItem, event.getClick());
        }
    }

    private void handleMainGUIClick(Player player, ItemStack clickedItem, ClickType clickType) {
        if (clickedItem.getType() == Material.PLAYER_HEAD) {
            SkullMeta meta = (SkullMeta) clickedItem.getItemMeta();
            if (meta != null && meta.getOwningPlayer() != null) {
                String friendName = ChatColor.stripColor(meta.getDisplayName());

                if (clickType == ClickType.RIGHT) {
                    openFriendOptionsGUI(player, friendName);
                } else if (clickType == ClickType.LEFT) {
                    if (Bukkit.getPlayer(friendName) != null) {
                        player.sendMessage(ChatColor.GREEN + "Teleporting to " + friendName + "...");
                        player.teleport(Bukkit.getPlayer(friendName));
                    } else {
                        player.sendMessage(ChatColor.RED + friendName + " is currently offline!");
                    }
                }
                return;
            }
        }

        switch (clickedItem.getType()) {
            case ARROW:
                int currentPage = getCurrentPage(player);
                if (clickedItem.getItemMeta().getDisplayName().contains("Previous")) {
                    if (currentPage > 0) {
                        openGUI(player, GUIType.MAIN, currentPage - 1);
                    }
                } else if (clickedItem.getItemMeta().getDisplayName().contains("Next")) {
                    openGUI(player, GUIType.MAIN, currentPage + 1);
                }
                break;
            case BOOK:
                openGUI(player, GUIType.REQUESTS);
                break;
            case BARRIER:
                openGUI(player, GUIType.BLOCKED);
                break;
            case GOLDEN_APPLE:
                openGUI(player, GUIType.FAVORITES);
                break;
            case CHEST:
                openGUI(player, GUIType.GROUPS);
                break;
            case PAPER:
                openGUI(player, GUIType.ACTIVITY_FEED);
                break;
            case REDSTONE:
                openGUI(player, GUIType.SETTINGS);
                break;
            case COMPASS:
                openGUI(player, GUIType.SEARCH);
                break;
            case EXPERIENCE_BOTTLE:
                openGUI(player, GUIType.STATISTICS);
                break;
        }
    }

    private void handleFriendOptionsGUIClick(Player player, ItemStack clickedItem, ClickType clickType) {
        String friendName = ChatColor.stripColor(
                player.getOpenInventory().getTitle().replace("Options for ", ""));

        switch (clickedItem.getType()) {
            case PAPER: // Send Message
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Type your message to " + friendName);
                // You'll need to implement a chat listener for this
                break;
            case GOLDEN_APPLE: // Toggle Favorite
                friendManager.toggleFavoriteFriend(player.getName(), friendName);
                openFriendOptionsGUI(player, friendName); // Refresh the GUI
                break;
            case NAME_TAG: // Set Nickname
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Type the new nickname for " + friendName);
                // You'll need to implement a chat listener for this
                break;
            case BARRIER: // Remove Friend
                friendManager.removeFriend(player.getName(), friendName);
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "Removed " + friendName + " from your friends list");
                break;
            case BOOK: // View Statistics
                openFriendStatisticsGUI(player, friendName);
                break;
            case CHEST: // Add to Group
                openGroupSelectionGUI(player, friendName);
                break;
            case REDSTONE: // Block Player
                friendManager.blockPlayer(player.getName(), friendName);
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "Blocked " + friendName);
                break;
        }
    }

    private ItemStack createPlayerHead(String playerName) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
            meta.setDisplayName(ChatColor.GOLD + playerName);

            List<String> lore = new ArrayList<>();
            boolean isOnline = Bukkit.getPlayer(playerName) != null;
            String status = friendManager.getStatus(playerName);

            lore.add(isOnline ? ChatColor.GREEN + "Online" : ChatColor.RED + "Offline");
            if (status != null && !status.isEmpty()) {
                lore.add(ChatColor.GRAY + "Status: " + ChatColor.WHITE + status);
            }

            FriendStatistics stats = friendManager.getFriendStatistics(playerName);
            if (stats != null) {
                long friendSince = stats.getFriendSince();
                if (friendSince > 0) {
                    lore.add(ChatColor.GRAY + "Friend since: " +
                            ChatColor.WHITE + new Date(friendSince).toString());
                }
            }

            if (friendManager.isFavorite(playerName, playerName)) {
                lore.add(ChatColor.GOLD + "★ Favorite Friend");
            }

            lore.add("");
            lore.add(ChatColor.YELLOW + "Left-Click to teleport");
            lore.add(ChatColor.YELLOW + "Right-Click for options");

            meta.setLore(lore);
            head.setItemMeta(meta);
        }
        return head;
    }

    private void openFriendStatisticsGUI(Player player, String friendName) {
        Inventory gui = Bukkit.createInventory(null, 27,
                ChatColor.GOLD + "Statistics for " + friendName);

        FriendStatistics stats = friendManager.getFriendStatistics(friendName);

        gui.setItem(11, createGuiItem(Material.CLOCK, "Friend Since",
                new Date(stats.getFriendSince()).toString()));
        gui.setItem(12, createGuiItem(Material.PAPER, "Messages Sent",
                String.valueOf(stats.getMessagesSent())));
        gui.setItem(13, createGuiItem(Material.DIAMOND_SWORD, "Games Played",
                String.valueOf(stats.getGamesPlayed())));
        gui.setItem(14, createGuiItem(Material.EXPERIENCE_BOTTLE, "Friendship Level",
                String.valueOf(stats.getFriendshipLevel())));
        gui.setItem(15, createGuiItem(Material.CLOCK, "Last Interaction",
                formatDuration(System.currentTimeMillis() - stats.getLastInteraction()) + " ago"));

        gui.setItem(26, createGuiItem(Material.ARROW, "Back"));

        player.openInventory(gui);
    }

    private void openGroupSelectionGUI(Player player, String friendName) {
        Inventory gui = Bukkit.createInventory(null, 27,
                ChatColor.GOLD + "Select Group for " + friendName);

        Collection<String> groups = friendManager.getFriendGroups(player.getName());
        int slot = 0;
        for (String group : groups) {
            gui.setItem(slot++, createGuiItem(Material.BOOK, group));
        }

        gui.setItem(26, createGuiItem(Material.ARROW, "Back"));

        player.openInventory(gui);
    }

    private void handleRequestsGUIClick(Player player, ItemStack clickedItem, ClickType clickType) {
        if (clickedItem.getType() == Material.PLAYER_HEAD) {
            String requestName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            if (clickType == ClickType.LEFT) {
                friendManager.acceptRequest(player.getName(), requestName);
                player.sendMessage(ChatColor.GREEN + "You accepted the friend request from " + requestName + "!");
            } else if (clickType == ClickType.RIGHT) {
                friendManager.denyRequest(player.getName(), requestName);
                player.sendMessage(ChatColor.RED + "You declined the friend request from " + requestName + ".");
            }
            openGUI(player, GUIType.REQUESTS);
        } else if (clickedItem.getType() == Material.ARROW) {
            openGUI(player, GUIType.MAIN);
        }
    }

    private void handleBlockedGUIClick(Player player, ItemStack clickedItem, ClickType clickType) {
        if (clickedItem.getType() == Material.PLAYER_HEAD) {
            String blockedName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            if (clickType == ClickType.RIGHT) {
                friendManager.unblockPlayer(player.getName(), blockedName);
                player.sendMessage(ChatColor.GREEN + "You unblocked " + blockedName + ".");
                openGUI(player, GUIType.BLOCKED);
            }
        } else if (clickedItem.getType() == Material.ARROW) {
            openGUI(player, GUIType.MAIN);
        }
    }

    private void handleSettingsGUIClick(Player player, ItemStack clickedItem, ClickType clickType) {
        switch (clickedItem.getType()) {
            case NAME_TAG:
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Enter your new username in chat.");
                // Implement chat listener for username change
                break;
            case GOLDEN_APPLE:
                boolean notificationsEnabled = friendManager.toggleNotifications(player.getName());
                player.sendMessage(ChatColor.GREEN + "Notifications " + (notificationsEnabled ? "enabled" : "disabled") + ".");
                break;
            case BARRIER:
                friendManager.clearBlockedPlayers(player.getName());
                player.sendMessage(ChatColor.RED + "All blocked players have been cleared.");
                break;
            case ARROW:
                openGUI(player, GUIType.MAIN);
                break;
        }
    }

    private void handleGroupsGUIClick(Player player, ItemStack clickedItem, ClickType clickType) {
        if (clickedItem.getType() == Material.BOOK) {
            String groupName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            openGroupMembersGUI(player, groupName);
        } else if (clickedItem.getType() == Material.ARROW) {
            openGUI(player, GUIType.MAIN);
        }
    }

    private void openGroupMembersGUI(Player player, String groupName) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Group: " + groupName);
        List<String> members = friendManager.getGroupMembers(player.getName(), groupName);

        for (String member : members) {
            gui.addItem(createPlayerHead(member));
        }

        gui.setItem(49, createGuiItem(Material.ARROW, "Back to Groups"));
        player.openInventory(gui);
    }

    private void handleActivityFeedGUIClick(Player player, ItemStack clickedItem, ClickType clickType) {
        if (clickedItem.getType() == Material.ARROW) {
            openGUI(player, GUIType.MAIN);
        }
    }

    private void handleStatisticsGUIClick(Player player, ItemStack clickedItem, ClickType clickType) {
        if (clickedItem.getType() == Material.ARROW) {
            openGUI(player, GUIType.MAIN);
        }
    }

    private void handleSearchGUIClick(Player player, ItemStack clickedItem, ClickType clickType) {
        switch (clickedItem.getType()) {
            case NAME_TAG:
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Enter the player name to search for in chat.");
                // Implement chat listener for name search
                break;
            case COMPASS:
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Enter the server name to search for in chat.");
                // Implement chat listener for server search
                break;
            case CLOCK:
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Enter the time period to search for in chat.");
                // Implement chat listener for last seen search
                break;
            case ARROW:
                openGUI(player, GUIType.MAIN);
                break;
        }
    }

    private ItemStack createGuiItem(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + displayName);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createGuiItem(Material material, String displayName, String lore) {
        ItemStack item = createGuiItem(material, displayName);
        ItemMeta meta = item.getItemMeta();
        meta.setLore(Collections.singletonList(ChatColor.GRAY + lore));
        item.setItemMeta(meta);
        return item;
    }

    private void addNavigationItems(Inventory gui, int page, int totalItems) {
        int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
        gui.setItem(45, createGuiItem(Material.ARROW, "Previous Page"));
        gui.setItem(53, createGuiItem(Material.ARROW, "Next Page"));
        gui.setItem(49, createGuiItem(Material.BOOK, "Page " + (page + 1) + " of " + totalPages));
    }

    private int getCurrentPage(Player player) {
        return currentPageMap.getOrDefault(player.getUniqueId(), 0);
    }

    private String formatDuration(long duration) {
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        StringBuilder formattedDuration = new StringBuilder();
        if (days > 0) {
            formattedDuration.append(days).append(" days ");
        }
        if (hours > 0) {
            formattedDuration.append(hours % 24).append(" hours ");
        }
        if (minutes > 0) {
            formattedDuration.append(minutes % 60).append(" minutes ");
        }
        formattedDuration.append(seconds % 60).append(" seconds");
        return formattedDuration.toString();
    }
}