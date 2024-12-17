package org.derjannik.lobbyLynx.managers;

import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class PartyChatManager {
    private static final String PARTY_PREFIX = "§6[Party Chat] ";
    
    public void sendMessage(UUID senderId, String message, Party party) {
        Player sender = Bukkit.getPlayer(senderId);
        if (sender == null) return;
        
        String formattedMessage = PARTY_PREFIX + "§f" + sender.getName() + ": §7" + message;
        
        // Send to all party members
        for (UUID memberId : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage(formattedMessage);
            }
        }
    }

    public void receiveMessage(UUID senderId, String message) {
        Player sender = Bukkit.getPlayer(senderId);
        if (sender != null && sender.isOnline()) {
            String formattedMessage = PARTY_PREFIX + "§f" + sender.getName() + ": §7" + message;
            sender.sendMessage(formattedMessage);
        }
    }

    public void broadcastPartyMessage(Party party, String message) {
        String formattedMessage = PARTY_PREFIX + "§7" + message;
        for (UUID memberId : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage(formattedMessage);
            }
        }
    }
}
