package org.derjannik.lobbyLynx.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;

public class PetCosmetic extends Cosmetic {

    private final EntityType petType;

    public PetCosmetic(String name, EntityType petType) {
        super(name);
        this.petType = petType;
    }

    @Override
    public void apply(Player player) {
        Entity pet = player.getWorld().spawnEntity(player.getLocation(), petType);
        pet.setCustomName(player.getName() + "'s " + getName());
        pet.setCustomNameVisible(true);
        player.sendMessage("You have summoned a " + getName() + " pet!");
    }
}
