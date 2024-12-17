package org.derjannik.lobbyLynx.managers;

import java.util.HashMap;
import java.util.Map;

public class PartyPermission {
    private String partyId;
    private Map<String, Boolean> permissions;

    public PartyPermission(String partyId) {
        this.partyId = partyId;
        this.permissions = new HashMap<>();
    }

    /**
     * Grants a permission to the party.
     * @param permission The permission to grant.
     */
    public void grantPermission(String permission) {
        permissions.put(permission, true);
        // Logic to grant permission
    }

    /**
     * Revokes a permission from the party.
     * @param permission The permission to revoke.
     */
    public void revokePermission(String permission) {
        permissions.put(permission, false);
        // Logic to revoke permission
    }

    public boolean checkPermission(String permission) {
        return permissions.getOrDefault(permission, false);
    }
}
