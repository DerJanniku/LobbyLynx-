package org.derjannik.lobbyLynx.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HatCosmetic extends Cosmetic {

    private final Material hatMaterial;

    public HatCosmetic(String name, Material hatMaterial) {
        super(name);
        this.hatMaterial = hatMaterial;
    }

    @Override
    public void apply(Player player) {
        player.getInventory().setHelmet(new ItemStack(hatMaterial));
        player.sendMessage("You have equipped the " + getName() + " hat!");
    }
}
