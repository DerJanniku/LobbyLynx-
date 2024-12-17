package org.derjannik.lobbyLynx.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Party {
    private String partyId;
    private List<UUID> members;
    private UUID leader;
    private int maxSize = 8;  // Default max party size

    public Party(UUID leaderId) {
        this.partyId = leaderId.toString();
        this.members = new ArrayList<>();
        this.leader = leaderId;
        addMember(leaderId); // Add leader as first member
    }

    public void addMember(UUID memberId) {
        if (!isFull() && !members.contains(memberId)) {
            members.add(memberId);
        }
    }

    public void removeMember(UUID memberId) {
        members.remove(memberId);
        
        // If leader leaves, assign new leader if there are remaining members
        if (memberId.equals(leader) && !members.isEmpty()) {
            leader = members.get(0);
        }
    }

    public boolean isFull() {
        return members.size() >= maxSize;
    }

    public boolean isLeader(UUID playerId) {
        return leader.equals(playerId);
    }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID newLeader) {
        if (members.contains(newLeader)) {
            this.leader = newLeader;
        }
    }

    public List<UUID> getMembers() {
        return new ArrayList<>(members); // Return copy to prevent external modification
    }

    public String getPartyId() {
        return partyId;
    }

    public int getSize() {
        return members.size();
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
}
