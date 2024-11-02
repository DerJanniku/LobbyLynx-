package org.derjannik.lobbyLynx.managers;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitTask;
import org.derjannik.lobbyLynx.LobbyLynx;
import static org.derjannik.lobbyLynx.util.TimeUtils.formatTime;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class PartyManager {
    private final LobbyLynx plugin;
    private final Map<UUID, Party> parties;
    private final Map<UUID, PartyInvite> partyInvites;
    private final Map<UUID, PartySettings> partySettings;
    private final Map<UUID, BukkitTask> partyTasks;
    private final Map<String, PartyGame> partyGames;
    private final PartyChatManager partyChatManager; // Add this field

    public PartyManager(LobbyLynx plugin) {
        this.plugin = plugin;
        this.parties = new ConcurrentHashMap<>();
        this.partyInvites = new ConcurrentHashMap<>();
        this.partySettings = new ConcurrentHashMap<>();
        this.partyTasks = new ConcurrentHashMap<>();
        this.partyGames = new ConcurrentHashMap<>();
        this.partyChatManager = new PartyChatManager(); // Initialize it here
        initializePartyGames();
    }

    private void initializePartyGames() {
        partyGames.put("PARKOUR", new PartyGame("Parkour Challenge", 2, 8, 300));
        partyGames.put("HIDE_SEEK", new PartyGame("Hide and Seek", 4, 16, 600));
        partyGames.put("BATTLE", new PartyGame("Party Battle", 2, 8, 300));
        partyGames.put("RACE", new PartyGame("Party Race", 2, 16, 180));
    }

    public class Party {
        private UUID partyId;
        private UUID leader;
        private Map<UUID, PartyRank> members;
        private PartySettings settings;
        private PartyState state;
        private PartyGame currentGame;
        private Set<UUID> moderators;
        private List<String> chatHistory;
        private Map<UUID, PartyStats> memberStats;
        private long creationTime;
        private boolean isPrivate;
        private int maxSize;
        private double experienceMultiplier;
        private Set<UUID> bannedPlayers;

        public Party(UUID leader) {
            this.partyId = UUID.randomUUID();
            this.leader = leader;
            this.members = new ConcurrentHashMap<>();
            this.members.put(leader, PartyRank.LEADER);
            this.settings = new PartySettings();
            this.state = PartyState.LOBBY;
            this.moderators = new HashSet<>();
            this.chatHistory = new ArrayList<>();
            this.memberStats = new ConcurrentHashMap<>();
            this.creationTime = System.currentTimeMillis();
            this.isPrivate = true;
            this.maxSize = 8;
            this.experienceMultiplier = 1.0;
            this.bannedPlayers = new HashSet<>();
            initializeMemberStats(leader);
        }

        private void initializeMemberStats(UUID playerId) {
            memberStats.put(playerId, new PartyStats());
        }

        public void broadcast(String message) {
            getOnlineMembers().forEach(player -> {
                player.sendMessage(ChatColor.GOLD + "[Party] " + ChatColor.YELLOW + message);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
            });
            chatHistory.add(message);
        }

        public List<Player> getOnlineMembers() {
            return members.keySet().stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        // Party management methods
        public void promotePlayer(UUID promoter, UUID target) {
            if (!hasPermission(promoter, PartyPermission.PROMOTE)) return;

            PartyRank currentRank = members.get(target);
            if (currentRank == null) return;

            switch (currentRank) {
                case MEMBER:
                    members.put(target, PartyRank.MODERATOR);
                    moderators.add(target);
                    broadcast(Bukkit.getPlayer(target).getName() + " has been promoted to Moderator!");
                    break;
                case MODERATOR:
                    if (promoter.equals(leader)) {
                        UUID oldLeader = leader;
                        leader = target;
                        members.put(oldLeader, PartyRank.MODERATOR);
                        members.put(target, PartyRank.LEADER);
                        broadcast(Bukkit.getPlayer(target).getName() + " is the new party leader!");
                    }
                    break;
            }
        }

        public boolean hasPermission(UUID playerId, PartyPermission permission) {
            PartyRank rank = members.get(playerId);
            if (rank == null) return false;
            return rank.hasPermission(permission);
        }
    }

    public enum PartyRank {
        LEADER(EnumSet.allOf(PartyPermission.class)),
        MODERATOR(EnumSet.of(
                PartyPermission.KICK,
                PartyPermission.INVITE,
                PartyPermission.CHAT,
                PartyPermission.START_GAME
        )),
        MEMBER(EnumSet.of(
                PartyPermission.CHAT
        ));

        private final Set<PartyPermission> permissions;

        PartyRank(Set<PartyPermission> permissions) {
            this.permissions = permissions;
        }

        public boolean hasPermission(PartyPermission permission) {
            return permissions.contains(permission);
        }
    }

    public enum PartyPermission {
        KICK, INVITE, PROMOTE, DEMOTE, START_GAME, CHANGE_SETTINGS, CHAT, BAN
    }

    public enum PartyState {
        LOBBY, IN_GAME, MATCHMAKING, PRIVATE
    }

    public class PartySettings {
        private boolean allowFriendJoin;
        private boolean enablePartyChat;
        private boolean allowGameInvites;
        private int minPlayersForGame;
        private boolean enablePartyAnnouncements;
        private ChatColor partyColor;
        private boolean enablePartyEffects;
        private boolean allowTeleport;

        public PartySettings() {
            this.allowFriendJoin = false;
            this.enablePartyChat = true;
            this.allowGameInvites = true;
            this.minPlayersForGame = 2;
            this.enablePartyAnnouncements = true;
            this.partyColor = ChatColor.GOLD;
            this.enablePartyEffects = true;
            this.allowTeleport = true;
        }
    }

    public class PartyGame {
        private String name;
        private int minPlayers;
        private int maxPlayers;
        private int duration;
        private Map<UUID, Integer> scores;
        private GameState state;
        private long startTime;

        public PartyGame(String name, int minPlayers, int maxPlayers, int duration) {
            this.name = name;
            this.minPlayers = minPlayers;
            this.maxPlayers = maxPlayers;
            this.duration = duration;
            this.scores = new HashMap<>();
            this.state = GameState.WAITING;
        }

        public void start(Party party) {
            if (party.getOnlineMembers().size() < minPlayers) {
                party.broadcast("Not enough players to start " + name);
                return;
            }

            state = GameState.IN_PROGRESS;
            startTime = System.currentTimeMillis();
            party.broadcast("Starting " + name + "!");

            // Initialize game scores
            party.getOnlineMembers().forEach(player -> scores.put(player.getUniqueId(), 0));

            // Start game timer
            BukkitTask gameTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                long timeLeft = duration - ((System.currentTimeMillis() - startTime) / 1000);
                if (timeLeft <= 0) {
                    endGame(party);
                } else if (timeLeft <= 10 || timeLeft % 30 == 0) {
                    party.broadcast("Time remaining: " + timeLeft + " seconds!");
                }
            }, 20L, 20L);

            partyTasks.put(party.partyId, gameTask);
        }

        private void endGame(Party party) {
            state = GameState.ENDED;
            partyTasks.get(party.partyId).cancel();
            partyTasks.remove(party.partyId);

            // Announce winners
            Map.Entry<UUID, Integer> winner = scores.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);

            if (winner != null) {
                Player winnerPlayer = Bukkit.getPlayer(winner.getKey());
                if (winnerPlayer != null) {
                    party.broadcast(winnerPlayer.getName() + " won the game with " + winner.getValue() + " points!");
                    // Award party experience
                    awardPartyExperience(party, winnerPlayer.getUniqueId(), 100);
                }
            }

            // Reset party state
            party.state = PartyState.LOBBY;
            party.currentGame = null;
        }
    }

    public enum GameState {
        WAITING, IN_PROGRESS, ENDED
    }

    public class PartyStats {
        private int gamesPlayed;
        private int gamesWon;
        private int totalPoints;
        private long partyTime;
        private int experiencePoints;
        private int partyLevel;
        private Map<String, Integer> gameSpecificStats;

        public PartyStats() {
            this.gamesPlayed = 0;
            this.gamesWon = 0;
            this.totalPoints = 0;
            this.partyTime = 0;
            this.experiencePoints = 0;
            this.partyLevel = 1;
            this.gameSpecificStats = new HashMap<>();
        }

        public void incrementStat(String stat) {
            gameSpecificStats.merge(stat, 1, Integer::sum);
        }

        public void addExperience(int exp) {
            this.experiencePoints += exp;
            checkLevelUp();
        }

        private void checkLevelUp() {
            int requiredExp = calculateRequiredExp();
            while (experiencePoints >= requiredExp) {
                partyLevel++;
                experiencePoints -= requiredExp;
                requiredExp = calculateRequiredExp();
            }
        }

        private int calculateRequiredExp() {
            return 100 * partyLevel * partyLevel;
        }
    }

    public class PartyInvite {
        private final UUID inviter;
        private final UUID invited;
        private final long timestamp;
        private final long expirationTime;

        public PartyInvite(UUID inviter, UUID invited) {
            this.inviter = inviter;
            this.invited = invited;
            this.timestamp = System.currentTimeMillis();
            this.expirationTime = timestamp + (60 * 1000); // 60 seconds expiration
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }

    // Party Manager Methods
    public void createParty(Player leader) {
        UUID leaderUUID = leader.getUniqueId();
        if (isInParty(leaderUUID)) {
            leader.sendMessage(ChatColor.RED + "You are already in a party!");
            return;
        }

        Party party = new Party(leaderUUID);
        parties.put(party.partyId, party);
        partySettings.put(party.partyId, new PartySettings());

        leader.sendMessage(ChatColor.GREEN + "Party created! Use /party invite to invite players.");

        // Start party experience timer
        startPartyExperienceTimer(party);
    }

    private void startPartyExperienceTimer(Party party) {
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (party.getOnlineMembers().size() >= 2) {
                party.getOnlineMembers().forEach(player ->
                        awardPartyExperience(party, player.getUniqueId(), 5));
            }
        }, 1200L, 1200L); // Award experience every minute

        partyTasks.put(party.partyId, task);
    }

    private void awardPartyExperience(Party party, UUID playerId, int baseExp) {
        PartyStats stats = party.memberStats.get(playerId);
        if (stats != null) {
            int expToAward = (int) (baseExp * party.experienceMultiplier);
            stats.addExperience(expToAward);

            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.sendMessage(ChatColor.GREEN + "+" + expToAward + " Party Experience!");
            }
        }
    }

    public void invitePlayer(Player inviter, Player target) {
        Party party = getPartyByMember(inviter.getUniqueId());
        if (party == null) {
            inviter.sendMessage(ChatColor.RED + "You are not in a party!");
            return;
        }

        if (!party.hasPermission(inviter.getUniqueId(), PartyPermission.INVITE)) {
            inviter.sendMessage(ChatColor.RED + "You don't have permission to invite players!");
            return;
        }

        if (isInParty(target.getUniqueId())) {
            inviter.sendMessage(ChatColor.RED + target.getName() + " is already in a party!");
            return;
        }

        if (party.bannedPlayers.contains(target.getUniqueId())) {
            inviter.sendMessage(ChatColor.RED + target.getName() + " is banned from this party!");
            return;
        }

        PartyInvite invite = new PartyInvite(inviter.getUniqueId(), target.getUniqueId());
        partyInvites.put(target.getUniqueId(), invite);

        // Send fancy invite message
        target.sendMessage("");
        target.sendMessage(ChatColor.GOLD + "═══════════════════════");
        target.sendMessage(ChatColor.YELLOW + inviter.getName() + ChatColor.GOLD + " has invited you to their party!");
        target.sendMessage(ChatColor.GOLD + "Click to " +
                ChatColor.GREEN + "[Accept] " +
                ChatColor.GOLD + "or " +
                ChatColor.RED + "[Decline]");
        target.sendMessage(ChatColor.GOLD + "═══════════════════════");
        target.sendMessage("");

        // Play sound
        target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

        // Expire invite after 60 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (partyInvites.remove(target.getUniqueId()) != null) {
                target.sendMessage(ChatColor.RED + "Party invite from " + inviter.getName() + " has expired.");
                inviter.sendMessage(ChatColor.RED + "Party invite to " + target.getName() + " has expired.");
            }
        }, 1200L);
    }

    public void acceptInvite(Player player) {
        PartyInvite invite = partyInvites.get(player.getUniqueId());
        if (invite == null || invite.isExpired()) {
            player.sendMessage(ChatColor.RED + "You don't have any active party invites!");
            return;
        }

        Party party = getPartyByMember(invite.inviter);
        if (party == null) {
            player.sendMessage(ChatColor.RED + "The party no longer exists!");
            partyInvites.remove(player.getUniqueId());
            return;
        }

        if (party.members.size() >= party.maxSize) {
            player.sendMessage(ChatColor.RED + "The party is full!");
            partyInvites.remove(player.getUniqueId());
            return;
        }

        // Add player to party
        party.members.put(player.getUniqueId(), PartyRank.MEMBER);
        party.initializeMemberStats(player.getUniqueId());
        partyInvites.remove(player.getUniqueId());

        // Broadcast join message
        party.broadcast(player.getName() + " has joined the party!");

        // Show party info to new member
        showPartyInfo(player, party);
    }

    private void showPartyInfo(Player player, Party party) {
        player.sendMessage(ChatColor.GOLD + "═══════ Party Information ═══════");
        player.sendMessage(ChatColor.YELLOW + "Leader: " + Bukkit.getPlayer(party.leader).getName());
        player.sendMessage(ChatColor.YELLOW + "Members: " + party.members.size() + "/" + party.maxSize);
        player.sendMessage(ChatColor.YELLOW + "Party Level: " + party.memberStats.get(party.leader).partyLevel);
        if (party.currentGame != null) {
            player.sendMessage(ChatColor.YELLOW + "Current Game: " + party.currentGame.name);
        }
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
    }

    public void leaveParty(Player player) {
        Party party = getPartyByMember(player.getUniqueId());
        if (party == null) {
            player.sendMessage(ChatColor.RED + "You are not in a party!");
            return;
        }

        if (party.leader.equals(player.getUniqueId())) {
            disbandParty(party);
        } else {
            party.members.remove(player.getUniqueId());
            party.moderators.remove(player.getUniqueId());
            party.broadcast(player.getName() + " has left the party!");
            player.sendMessage(ChatColor.YELLOW + "You have left the party!");
        }
    }

    public void disbandParty(Party party) {
        // Cancel all party tasks
        BukkitTask task = partyTasks.remove(party.partyId);
        if (task != null) {
            task.cancel();
        }

        // Notify all members
        party.broadcast(ChatColor.RED + "The party has been disbanded!");

        // Save party statistics
        savePartyStats(party);

        // Remove party
        parties.remove(party.partyId);
        partySettings.remove(party.partyId);
    }

    public void startPartyGame(Player initiator, String gameName) {
        Party party = getPartyByMember(initiator.getUniqueId());
        if (party == null) {
            initiator.sendMessage(ChatColor.RED + "You are not in a party!");
            return;
        }

        if (!party.hasPermission(initiator.getUniqueId(), PartyPermission.START_GAME)) {
            initiator.sendMessage(ChatColor.RED + "You don't have permission to start games!");
            return;
        }

        PartyGame game = partyGames.get(gameName.toUpperCase());
        if (game == null) {
            initiator.sendMessage(ChatColor.RED + "Invalid game selected!");
            return;
        }

        if (party.state != PartyState.LOBBY) {
            initiator.sendMessage(ChatColor.RED + "The party is already in a game!");
            return;
        }

        if (party.getOnlineMembers().size() < game.minPlayers) {
            initiator.sendMessage(ChatColor.RED + "Not enough players to start " + game.name + "! (" +
                    game.minPlayers + " required)");
            return;
        }

        party.state = PartyState.IN_GAME;
        party.currentGame = game;
        game.start(party);
    }

    public void handlePartyChat(Player player, String message) {
        Party party = getPartyByMember(player.getUniqueId());
        if (party == null) {
            player.sendMessage(ChatColor.RED + "You are not in a party!");
            return;
        }

        PartyRank rank = party.members.get(player.getUniqueId());
        String prefix = rank == PartyRank.LEADER ? "[Leader] " :
                rank == PartyRank.MODERATOR ? "[Mod] " : "";

        party.broadcast(prefix + player.getName() + ": " + message);
    }

    public void togglePartyWarp(Player player) {
        Party party = getPartyByMember(player.getUniqueId());
        if (party == null || !party.hasPermission(player.getUniqueId(), PartyPermission.CHANGE_SETTINGS)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to change party settings!");
            return;
        }

        PartySettings settings = partySettings.get(party.partyId);
        settings.allowTeleport = !settings.allowTeleport;
        party.broadcast("Party warping has been " +
                (settings.allowTeleport ? "enabled" : "disabled") +
                " by " + player.getName());
    }

    public void warpParty(Player initiator, Location location) {
        Party party = getPartyByMember(initiator.getUniqueId());
        if (party == null) {
            initiator.sendMessage(ChatColor.RED + "You are not in a party!");
            return;
        }

        if (!party.hasPermission(initiator.getUniqueId(), PartyPermission.CHANGE_SETTINGS)) {
            initiator.sendMessage(ChatColor.RED + "You don't have permission to warp the party!");
            return;
        }

        PartySettings settings = partySettings.get(party.partyId);
        if (!settings.allowTeleport) {
            initiator.sendMessage(ChatColor.RED + "Party warping is currently disabled!");
            return;
        }

        party.broadcast(ChatColor.YELLOW + "Warping party in 3 seconds...");
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player member : party.getOnlineMembers()) {
                member.teleport(location);
                member.playSound(member.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            }
        }, 60L);
    }

    private void savePartyStats(Party party) {
        // Implementation for saving party statistics to database
        // This would typically involve SQL or other database operations
    }

    public Party getPartyByMember(UUID memberId) {
        return parties.values().stream()
                .filter(party -> party.members.containsKey(memberId))
                .findFirst()
                .orElse(null);
    }

    public boolean isInParty(UUID playerId) {
        return getPartyByMember(playerId) != null;
    }

    // Party GUI
    public void openPartyGUI(Player player) {
        Party party = getPartyByMember(player.getUniqueId());
        if (party == null) {
            player.sendMessage(ChatColor.RED + "You are not in a party!");
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Party Management");

        // Party Info
        gui.setItem(4, createGuiItem(Material.GOLDEN_APPLE, ChatColor.GOLD + "Party Info",
                ChatColor.YELLOW + "Leader: " + Bukkit.getPlayer(party.leader).getName(),
                ChatColor.YELLOW + "Members: " + party.members.size() + "/" + party.maxSize,
                ChatColor.YELLOW + "Level: " + party.memberStats.get(party.leader).partyLevel));

        // Member List
        int slot = 9;
        for (Map.Entry<UUID, PartyRank> entry : party.members.entrySet()) {
            Player member = Bukkit.getPlayer(entry.getKey());
            if (member != null) {
                gui.setItem(slot++, createPlayerHead(member, entry.getValue()));
            }
        }

        // Party Games
        gui.setItem(45, createGuiItem(Material.DIAMOND_SWORD, ChatColor.AQUA + "Party Games"));

        // Party Settings
        gui.setItem(46, createGuiItem(Material.REDSTONE_TORCH, ChatColor.RED + "Party Settings"));

        // Party Stats
        gui.setItem(47, createGuiItem(Material.BOOK, ChatColor.GREEN + "Party Stats"));

        // Leave Party
        gui.setItem(53, createGuiItem(Material.BARRIER, ChatColor.RED + "Leave Party"));

        player.openInventory(gui);
    }

    private ItemStack createPlayerHead(Player player, PartyRank rank) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName(ChatColor.YELLOW + player.getName());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Rank: " + rank.name());
        lore.add(ChatColor.GRAY + "Click to manage");
        meta.setLore(lore);
        head.setItemMeta(meta);
        return head;
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.GOLD + "Party Management")) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        if (clickedItem.getType() == Material.PLAYER_HEAD) {
            handleMemberClick(player, clickedItem);
        } else if (clickedItem.getType() == Material.DIAMOND_SWORD) {
            openPartyGamesGUI(player);
        } else if (clickedItem.getType() == Material.REDSTONE_TORCH) {
            openPartySettingsGUI(player);
        } else if (clickedItem.getType() == Material.BOOK) {
            openPartyStatsGUI(player);
        } else if (clickedItem.getType() == Material.BARRIER) {
            player.closeInventory();
            leaveParty(player);
        }
    }

    private void handleMemberClick(Player player, ItemStack memberHead) {
        SkullMeta meta = (SkullMeta) memberHead.getItemMeta();
        Player targetPlayer = (Player) meta.getOwningPlayer();
        if (targetPlayer == null) return;

        Party party = getPartyByMember(player.getUniqueId());
        if (party == null) return;

        if (!party.hasPermission(player.getUniqueId(), PartyPermission.PROMOTE)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to manage members!");
            return;
        }

        openMemberManagementGUI(player, targetPlayer);
    }

    private void openMemberManagementGUI(Player player, Player target) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Manage: " + target.getName());

        gui.setItem(11, createGuiItem(Material.GOLDEN_HELMET, ChatColor.YELLOW + "Promote"));
        gui.setItem(13, createGuiItem(Material.IRON_BOOTS, ChatColor.RED + "Demote"));
        gui.setItem(15, createGuiItem(Material.BARRIER, ChatColor.DARK_RED + "Kick"));

        player.openInventory(gui);
    }

    private void openPartyGamesGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.AQUA + "Party Games");

        int slot = 0;
        for (PartyGame game : partyGames.values()) {
            gui.setItem(slot++, createGuiItem(Material.DIAMOND_SWORD,
                    ChatColor.GREEN + game.name,
                    ChatColor.YELLOW + "Players: " + game.minPlayers + "-" + game.maxPlayers,
                    ChatColor.YELLOW + "Duration: " + game.duration + " seconds",
                    ChatColor.GRAY + "Click to start!"));
        }

        player.openInventory(gui);
    }

    private void openPartySettingsGUI(Player player) {
        Party party = getPartyByMember(player.getUniqueId());
        if (party == null) return;

        PartySettings settings = partySettings.get(party.partyId);
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.RED + "Party Settings");

        gui.setItem(10, createGuiItem(Material.GOLDEN_APPLE,
                ChatColor.YELLOW + "Allow Friend Join: " + (settings.allowFriendJoin ? "Enabled" : "Disabled")));
        gui.setItem(12, createGuiItem(Material.PAPER,
                ChatColor.YELLOW + "Party Chat: " + (settings.enablePartyChat ? "Enabled" : "Disabled")));
        gui.setItem(14, createGuiItem(Material.ENDER_PEARL,
                ChatColor.YELLOW + "Allow Teleport: " + (settings.allowTeleport ? "Enabled" : "Disabled")));
        gui.setItem(16, createGuiItem(Material.EXPERIENCE_BOTTLE,
                ChatColor.YELLOW + "Experience Multiplier: " + party.experienceMultiplier));

        player.openInventory(gui);
    }

    private void openPartyStatsGUI(Player player) {
        Party party = getPartyByMember(player.getUniqueId());
        if (party == null) return;

        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.GREEN + "Party Stats");

        PartyStats leaderStats = party.memberStats.get(party.leader);
        gui.setItem(13, createGuiItem(Material.GOLDEN_APPLE,
                ChatColor.GOLD + "Party Stats",
                ChatColor.YELLOW + "Level: " + leaderStats.partyLevel,
                ChatColor.YELLOW + "Games Played: " + leaderStats.gamesPlayed,
                ChatColor.YELLOW + "Games Won: " + leaderStats.gamesWon,
                ChatColor.YELLOW + "Total Points: " + leaderStats.totalPoints,
                ChatColor.YELLOW + "Party Time: " + formatTime(leaderStats.partyTime)));

        // Add member-specific stats
        int slot = 0;
        for (Map.Entry<UUID, PartyStats> entry : party.memberStats.entrySet()) {
            Player member = Bukkit.getPlayer(entry.getKey());
            if (member != null) {
                PartyStats stats = entry.getValue();
                gui.setItem(slot++, createPlayerStatsHead(member, stats));
            }
        }

        player.openInventory(gui);
    }


    private ItemStack createPlayerStatsHead(Player player, PartyStats stats) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName(ChatColor.YELLOW + player.getName() + "'s Stats");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Level: " + stats.partyLevel);
        lore.add(ChatColor.GRAY + "Experience: " + stats.experiencePoints + "/" + stats.calculateRequiredExp());
        lore.add(ChatColor.GRAY + "Games Won: " + stats.gamesWon);
        lore.add(ChatColor.GRAY + "Total Points: " + stats.totalPoints);

        // Add game-specific stats
        if (!stats.gameSpecificStats.isEmpty()) {
            lore.add("");
            lore.add(ChatColor.YELLOW + "Game Statistics:");
            stats.gameSpecificStats.forEach((game, value) ->
                    lore.add(ChatColor.GRAY + game + ": " + value));
        }

        meta.setLore(lore);
        head.setItemMeta(meta);
        return head;
    }

    // Party Chat System


    // Party Commands
    public class PartyCommands implements CommandExecutor {
        private final PartyManager partyManager;

        public PartyCommands(PartyManager partyManager) {
            this.partyManager = partyManager;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Only players can use party commands!");
                return true;
            }

            Player player = (Player) sender;

            if (args.length == 0) {
                openPartyGUI(player);
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "create":
                    partyManager.createParty(player);
                    break;

                case "invite":
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.RED + "Usage: /party invite <player>");
                        return true;
                    }
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        player.sendMessage(ChatColor.RED + "Player not found!");
                        return true;
                    }
                    partyManager.invitePlayer(player, target);
                    break;

                case "accept":
                    partyManager.acceptInvite(player);
                    break;

                case "leave":
                    partyManager.leaveParty(player);
                    break;

                case "chat":
                    partyManager.getPartyChatManager().togglePartyChat(player);
                    break;

                case "warp":
                    partyManager.warpParty(player, player.getLocation());
                    break;

                case "game":
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.RED + "Usage: /party game <gamename>");
                        return true;
                    }
                    partyManager.startPartyGame(player, args[1]);
                    break;

                case "settings":
                    openPartySettingsGUI(player);
                    break;

                case "stats":
                    openPartyStatsGUI(player);
                    break;

                case "help":
                    sendHelpMessage(player);
                    break;

                default:
                    player.sendMessage(ChatColor.RED + "Unknown party command. Use /party help for commands.");
                    break;
            }
            return true;
        }

        private void sendHelpMessage(Player player) {
            player.sendMessage(ChatColor.GOLD + "═══════ Party Commands ═══════");
            player.sendMessage(ChatColor.YELLOW + "/party create " + ChatColor.GRAY + "- Create a new party");
            player.sendMessage(ChatColor.YELLOW + "/party invite <player> " + ChatColor.GRAY + "- Invite a player");
            player.sendMessage(ChatColor.YELLOW + "/party accept " + ChatColor.GRAY + "- Accept party invitation");
            player.sendMessage(ChatColor.YELLOW + "/party leave " + ChatColor.GRAY + "- Leave current party");
            player.sendMessage(ChatColor.YELLOW + "/party chat " + ChatColor.GRAY + "- Toggle party chat");
            player.sendMessage(ChatColor.YELLOW + "/party warp " + ChatColor.GRAY + "- Warp party to your location");
            player.sendMessage(ChatColor.YELLOW + "/party game <name> " + ChatColor.GRAY + "- Start a party game");
            player.sendMessage(ChatColor.YELLOW + "/party settings " + ChatColor.GRAY + "- Open party settings");
            player.sendMessage(ChatColor.YELLOW + "/party stats " + ChatColor.GRAY + "- View party statistics");
            player.sendMessage(ChatColor.GOLD + "═════════════════════════");
        }
    }

    public PartyChatManager getPartyChatManager() {
        return partyChatManager;
    }

    public class PartyChatManager {
        private final Map<UUID, Boolean> partyChatToggle = new HashMap<>();

        public void togglePartyChat(Player player) {
            UUID playerId = player.getUniqueId();
            boolean current = partyChatToggle.getOrDefault(playerId, false);
            partyChatToggle.put(playerId, !current);
            player.sendMessage(ChatColor.YELLOW + "Party chat " +
                    (!current ? "enabled" : "disabled") + "!");
        }

        public boolean isInPartyChat(UUID playerId) {
            return partyChatToggle.getOrDefault(playerId, false);
        }

        @EventHandler
        public void onPlayerChat(AsyncPlayerChatEvent event) {
            Player player = event.getPlayer();
            if (isInPartyChat(player.getUniqueId())) {
                event.setCancelled(true);
                handlePartyChat(player, event.getMessage());
            }
        }


        private String formatTime(long milliseconds) {
            long seconds = milliseconds / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            StringBuilder time = new StringBuilder();
            if (days > 0) time.append(days).append("d ");
            if (hours > 0) time.append(hours % 24).append("h ");
            if (minutes > 0) time.append(minutes % 60).append("m ");
            time.append(seconds % 60).append("s");

            return time.toString();
        }
    }
}