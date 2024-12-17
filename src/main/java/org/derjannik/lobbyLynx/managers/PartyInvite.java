package org.derjannik.lobbyLynx.managers;

import java.util.UUID;

public class PartyInvite {
    private UUID senderId;
    private UUID receiverId;
    private String partyId; // Added partyId field

    public PartyInvite(UUID senderId, UUID receiverId, String partyId) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.partyId = partyId; // Initialize partyId
    }

    public UUID getSenderId() {
        return senderId;
    }

    public UUID getReceiverId() {
        return receiverId;
    }

    public String getPartyId() {
        return partyId; // Added getter for partyId
    }

    // Additional methods for handling invites can be added here
}
