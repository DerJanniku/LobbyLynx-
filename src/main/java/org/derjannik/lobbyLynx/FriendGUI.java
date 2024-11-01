package org.derjannik.lobbyLynx;

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
import java.util.UUID; // Ensure you have this import at the top
import org.derjannik.lobbyLynx.FriendStatistics;
import java.util.*;


public class FriendGUI implements Listener {
    private final LobbyLynx plugin;
    private final FriendManager friendManager;
    private final int ITEMS_PER_PAGE = 45;
    private final Map<UUID, GUIType> activeGUIs = new HashMap<>();
    private final Map<UUID, String> selectedFriends = new HashMap<>();
    private final Map<UUID, Integer> currentPageMap = new HashMap<>(); // Added currentPageMap

    public enum GUIType {
        MAIN,
        REQUESTS,
        SETTINGS,
        BLOCKED,
        FAVORITES,
        GROUPS,
        ACTIVITY_FEED,
        STATISTICS,
        SEARCH
    }

    public FriendGUI(LobbyLynx plugin, FriendManager friendManager) {
        this.plugin = plugin;
        this.friendManager = friendManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openGUI(Player player, GUIType type) {

        openGUI(player, type, 0);

    }


    public void openGUI(Player player, GUIType type, int page) {

        activeGUIs.put(player.getUniqueId(), type);

        currentPageMap.put(player.getUniqueId(), page); // Store the current page

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
    }

    private void openFavoritesGUI(Player player, int page) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Favorites - Page " + (page + 1));
        List<String> favorites = friendManager.getFavorites(player.getName());

        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, favorites.size());

        // Add favorite friends heads
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
        List<String> blockedPlayers = (List<String>) friendManager.getBlockedPlayers(player.getName());

        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, blockedPlayers.size());

        // Add blocked players heads
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

        // Add friend request heads
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
        List<String> groups = (List<String>) friendManager.getFriendGroups(player.getName());

        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, groups.size());

        // Add group items
        for (int i = startIndex; i < endIndex; i++) {
            String groupName = groups.get(i);
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

        // Add activity items
        for (int i = startIndex; i < endIndex; i++) {
            String activity = activities.get(i);
            gui.addItem(createGuiItem(Material.PAPER, activity));
        }

        addNavigationItems(gui, page, activities.size());
        gui.setItem(49, createGuiItem(Material.ARROW, "Back to Main Menu"));

        player.openInventory(gui);
    }
    private FriendStatistics convertFriendStatistics(FriendStatistics managerStats) {
        FriendStatistics guiStats = new FriendStatistics();

        // Copy data from managerStats to guiStats
        guiStats.setFriendSince(managerStats.getFriendSince());
        guiStats.setMessagesSent(managerStats.getMessagesSent());
        guiStats.setGamesPlayed(managerStats.getGamesPlayed());
        guiStats.setLastInteraction(managerStats.getLastInteraction());
        guiStats.setStatus(managerStats.getStatus());
        guiStats.setOnline(managerStats.isOnline());
        return guiStats;
    }

    public void openStatisticsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Friend Statistics");
        FriendStatistics stats = friendManager.getFriendStatistics(player.getName());

        gui.setItem(10, createGuiItem(Material.DIAMOND, "Total Friends", new String(stats.getTotalFriends())));
        gui.setItem(11, createGuiItem(Material.EMERALD, "Online Friends", new String(stats.getOnlineFriends())));
        gui.setItem(12, createGuiItem(Material.GOLDEN_APPLE, "Favorite Friends", new String(stats.getFavoriteFriends())));
        gui.setItem(13, createGuiItem(Material.BOOK, "Friend Groups", new String(stats.getFriendGroups())));
        gui.setItem(14, createGuiItem(Material.CLOCK, "Average Friendship Duration", formatDuration(stats.getAverageFriendshipDuration())));
        gui.setItem(15, createGuiItem(Material.EXPERIENCE_BOTTLE, "Total Friendship Level", new String(stats.getTotalFriendshipLevel())));
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
        }
    }

    private void handleMainGUIClick(Player player, ItemStack clickedItem, ClickType clickType) {
        if (clickedItem.getType() == Material.ARROW) {
            // Navigate to the previous page if applicable
            int currentPage = getCurrentPage(player);
            if (currentPage > 0) {
                openGUI(player, GUIType.MAIN, currentPage - 1);
            }
        } else if (clickedItem.getType() == Material.BOOK) {
            // Open friend requests GUI
            openGUI(player, GUIType.REQUESTS);
        } else if (clickedItem.getType() == Material.REDSTONE) {
            // Open settings GUI
            openGUI(player, GUIType.SETTINGS);
        } else if (clickedItem.getType() == Material.BARRIER) {
            // Open blocked players GUI
            openGUI(player, GUIType.BLOCKED);
        } else if (clickedItem.getType() == Material.GOLDEN_APPLE) {
            // Open favorites GUI
            openGUI(player, GUIType.FAVORITES);
        } else if (clickedItem.getType() == Material.CHEST) {
            // Open groups GUI
            openGUI(player, GUIType.GROUPS);
        } else if (clickedItem.getType() == Material.PAPER) {
            // Open activity feed GUI
            openGUI(player, GUIType.ACTIVITY_FEED);
        } else if (clickedItem.getType() == Material.COMPASS) {
            // Open search GUI
            openGUI(player, GUIType.SEARCH);
        }
    }

    private int getCurrentPage(Player player) {

        // Check if the player has an active GUI entry

        return currentPageMap.getOrDefault(player.getUniqueId(), 0); // Return the current page or default to 0

    }


    private void handleRequestsGUIClick(Player player, ItemStack clickedItem, ClickType clickType) {
        // Handle accepting or declining friend requests
        String requestName = clickedItem.getItemMeta().getDisplayName();
        if (clickedItem.getType() == Material.PLAYER_HEAD) {
            if (clickType == ClickType.LEFT) {
                friendManager.acceptRequest(player.getName(), requestName);
                player.sendMessage(ChatColor.GREEN + "You accepted the friend request from " + requestName + "!");
                openGUI(player, GUIType.REQUESTS);
            } else if (clickType == ClickType.RIGHT) {
                friendManager.declineRequest(player.getName(), requestName);
                player.sendMessage(ChatColor.RED + "You declined the friend request from " + requestName + ".");
                openGUI(player, GUIType.REQUESTS);
            }
        }
    }

    private void handleBlockedGUIClick(Player player, ItemStack clickedItem, ClickType clickType) {
        String blockedName = clickedItem.getItemMeta().getDisplayName();
        if (clickedItem.getType() == Material.PLAYER_HEAD) {
            if (clickType == ClickType.RIGHT) {
                friendManager.unblockPlayer(player.getName(), blockedName);
                player.sendMessage(ChatColor.GREEN + "You unblocked " + blockedName + ".");
                openGUI(player, GUIType.BLOCKED);
            }
        }
    }

    private void handleSettingsGUIClick(Player player, ItemStack clickedItem, ClickType clickType) {
        // Handle settings actions based on clicked item
        if (clickedItem.getType() == Material.NAME_TAG) {
            // Logic to change username
            player.sendMessage(ChatColor.YELLOW + "Enter your new username in chat.");
        } else if (clickedItem.getType() == Material.GOLDEN_APPLE) {
            // Toggle notifications logic
            boolean notificationsEnabled = friendManager.toggleNotifications(player.getName());
            player.sendMessage(ChatColor.GREEN + "Notifications " + (notificationsEnabled ? "enabled" : "disabled") + ".");
        } else if (clickedItem.getType() == Material.BARRIER) {
            // Clear blocked players logic
            friendManager.clearBlockedPlayers(player.getName());
            player.sendMessage(ChatColor.RED + "All blocked players have been cleared.");
        }
    }

    private void handleGroupsGUIClick(Player player, ItemStack clickedItem, ClickType clickType) {
        String groupName = clickedItem.getItemMeta().getDisplayName();
        if (clickedItem.getType() == Material.BOOK) {
            // Logic to open group details or manage group
            player.sendMessage(ChatColor.GOLD + "You opened the group: " + groupName);
            // Implement group management logic here
        }
    }

    private void handleActivityFeedGUIClick(Player player, ItemStack clickedItem, ClickType clickType) {
        // Handle activity feed clicks
        String activity = clickedItem.getItemMeta().getDisplayName();
        player.sendMessage(ChatColor.GREEN + "You viewed the activity: " + activity);
    }

    private void handleStatisticsGUIClick(Player player, ItemStack clickedItem, ClickType clickType) {
        // Handle statistics clicks
        String statName = clickedItem.getItemMeta().getDisplayName();
        player.sendMessage(ChatColor.GREEN + "You viewed the statistic: " + statName);
    }

    private void handleSearchGUIClick(Player player, ItemStack clickedItem, ClickType clickType) {
        // Handle search GUI clicks
        String searchType = clickedItem.getItemMeta().getDisplayName();
        if (searchType.equals("Search by Name")) {
            // Logic to search by name
            player.sendMessage(ChatColor.YELLOW + "Enter the player name to search for in chat.");
        } else if (searchType.equals("Search by Server")) {
            // Logic to search by server
            player.sendMessage(ChatColor.YELLOW + "Enter the server name to search for in chat.");
        } else if (searchType.equals("Search by Last Seen")) {
            // Logic to search by last seen
            player.sendMessage(ChatColor.YELLOW + "Enter the time period to search for in chat.");
        }
    }

    private ItemStack createPlayerHead(String playerName) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwner(playerName);
        meta.setDisplayName(ChatColor.GOLD + playerName);
        head.setItemMeta(meta);
        return head;
    }

    private ItemStack createGuiItem(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + displayName);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createGuiItem(Material material, String displayName, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + displayName);
        meta.setLore(Collections.singletonList(ChatColor.GRAY + lore));
        item.setItemMeta(meta);
        return item;
    }

    private void addNavigationItems(Inventory gui, int page, int totalItems) {
        int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
        gui.setItem(48, createGuiItem(Material.ARROW, "Previous Page"));
        gui.setItem(50, createGuiItem(Material.ARROW, "Next Page"));
        gui.setItem(49, createGuiItem(Material.BOOK, "Page " + (page + 1) + " of " + totalPages));
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