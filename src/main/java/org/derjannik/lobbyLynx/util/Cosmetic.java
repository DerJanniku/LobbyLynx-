package org.derjannik.lobbyLynx.util;

import org.bukkit.entity.Player;

public abstract class Cosmetic {

    private final String name;

    public Cosmetic(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract void apply(Player player);
}
