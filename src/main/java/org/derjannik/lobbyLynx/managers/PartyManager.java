package org.derjannik.lobbyLynx.managers;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.derjannik.lobbyLynx.managers.Party;
import org.derjannik.lobbyLynx.managers.PartyChatManager;
import org.derjannik.lobbyLynx.managers.PartyInvite;

public class PartyManager {
    private ConcurrentHashMap<UUID, Party> parties;
    private PartyChatManager partyChatManager;

    public PartyManager() {
        this.parties = new ConcurrentHashMap<>();
        this.partyChatManager = new PartyChatManager();
    }

    public void createParty(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) return;

        // Check if player is already in a party
        if (isInParty(playerId)) {
            player.sendMessage("§cYou are already in a party!");
            return;
        }

        // Create a new party
        Party party = new Party(playerId);
        party.addMember(playerId); // Add the creator as first member
        parties.put(playerId, party);
        
        // Notify the player
        player.sendMessage("§6You have created a new party! §7Use /party invite <player> to invite others.");
    }

    public boolean isInParty(UUID playerId) {
        return parties.containsKey(playerId);
    }

    public Party getPartyByMember(UUID playerId) {
        return parties.get(playerId);
    }

    public void invitePlayer(UUID inviterId, UUID inviteeId) {
        Party party = getPartyByMember(inviterId);
        if (party != null) {
            // Check if invitee is already in a party
            if (isInParty(inviteeId)) {
                Player inviter = Bukkit.getPlayer(inviterId);
                if (inviter != null) {
                    inviter.sendMessage("§cThis player is already in a party!");
                }
                return;
            }
            
            // Create and send invite
            PartyInvite invite = new PartyInvite(inviterId, inviteeId, party.getPartyId());
            Player invitee = Bukkit.getPlayer(inviteeId);
            if (invitee != null) {
                invitee.sendMessage("§6You have been invited to join a party by §f" + 
                    Bukkit.getPlayer(inviterId).getName());
                // Could add clickable accept/deny buttons here using spigot chat components
            }
        }
    }

    public void acceptInvite(UUID inviteeId, PartyInvite invite) {
        if (!invite.getReceiverId().equals(inviteeId)) {
            return;
        }

        Party party = getPartyByMember(UUID.fromString(invite.getPartyId()));
        if (party != null) {
            // Add member to party
            party.addMember(inviteeId);
            parties.put(inviteeId, party);

            // Notify party members
            String joinMessage = "§6" + Bukkit.getPlayer(inviteeId).getName() + 
                " §7has joined the party!";
            for (UUID memberId : party.getMembers()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null) {
                    member.sendMessage(joinMessage);
                }
            }
        }
    }

    public void leaveParty(UUID playerId) {
        Party party = getPartyByMember(playerId);
        if (party != null) {
            Player leaver = Bukkit.getPlayer(playerId);
            String leaveMessage = "§6" + (leaver != null ? leaver.getName() : "A player") + 
                " §7has left the party!";
            
            // Remove from party
            party.removeMember(playerId);
            parties.remove(playerId);
            
            // Notify remaining members
            for (UUID memberId : party.getMembers()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null) {
                    member.sendMessage(leaveMessage);
                }
            }
            
            // Notify the player who left
            if (leaver != null) {
                leaver.sendMessage("§7You have left the party!");
            }
            
            // If party is empty, clean it up
            if (party.getMembers().isEmpty()) {
                parties.values().removeIf(p -> p.getMembers().isEmpty());
            }
        }
    }

    public void sendPartyMessage(UUID senderId, String message) {
        Party party = getPartyByMember(senderId);
        if (party != null) {
            partyChatManager.sendMessage(senderId, message, party);
        }
    }

    // Additional methods for accepting invites, leaving parties, etc.
}
