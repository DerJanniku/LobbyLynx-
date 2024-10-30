package org.derjannik.lobbyLynx;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class SettingsGUI implements Listener {

    private final LobbyLynx plugin;

    public SettingsGUI(LobbyLynx plugin) {
        this.plugin = plugin;
    }

    public void openSettingsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, "Admin GUI (Settings Menu)");

        // Erstelle das Gamerules-Item (Slot 1)
        ItemStack swordItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta swordMeta = swordItem.getItemMeta();
        swordMeta.setDisplayName("Gamerules");
        swordMeta.setLore(List.of("Öffne die Gamerule-Einstellungen", "und passe die Spielregeln an.")); // Beschreibung
        swordItem.setItemMeta(swordMeta);
        gui.setItem(0, swordItem); // Platziere im ersten Slot

        // Erstelle das Command Block-Item (Slot 2)
        ItemStack commandBlockItem = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta commandBlockMeta = commandBlockItem.getItemMeta();
        commandBlockMeta.setDisplayName("Admin Commands");
        commandBlockMeta.setLore(List.of("Klicke, um alle Admin-Befehle zu sehen.")); // Beschreibung
        commandBlockItem.setItemMeta(commandBlockMeta);
        gui.setItem(2, commandBlockItem); // Platziere im Slot 2

        // Erstelle das Anvil-Item für die Spielerverwaltung (Slot 3)
        ItemStack playerManagementItem = new ItemStack(Material.ANVIL);
        ItemMeta playerManagementMeta = playerManagementItem.getItemMeta();
        playerManagementMeta.setDisplayName("Player Management");
        playerManagementMeta.setLore(List.of("Verwalte Spieler-Optionen", "und führe den /uperms-Befehl aus.")); // Beschreibung
        playerManagementItem.setItemMeta(playerManagementMeta);
        gui.setItem(3, playerManagementItem); // Platziere im Slot 3

        // Erstelle das Chest-Item für die Kosmetik-Einstellungen (Slot 4)
        ItemStack cosmeticsSetupItem = new ItemStack(Material.CHEST);
        ItemMeta cosmeticsSetupMeta = cosmeticsSetupItem.getItemMeta();
        cosmeticsSetupMeta.setDisplayName("Cosmetics Setup");
        cosmeticsSetupMeta.setLore(List.of("Passe die Kosmetik-Optionen an.", "Diese Funktion wird später hinzugefügt.")); // Beschreibung
        cosmeticsSetupItem.setItemMeta(cosmeticsSetupMeta);
        gui.setItem(4, cosmeticsSetupItem); // Platziere im Slot 4

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Admin GUI (Settings Menu)")) {
            event.setCancelled(true); // Verhindere die Bewegung von Items

            if (event.getCurrentItem() != null) {
                Player player = (Player) event.getWhoClicked();
                switch (event.getCurrentItem().getType()) {
                    case DIAMOND_SWORD:
                        // Öffne die Gamerule-GUI, wenn das Schwert angeklickt wird
                        new GameruleGUI(plugin).openGameruleGUI(player);
                        break;
                    case COMMAND_BLOCK:
                        // Zeige Admin-Befehle an (kann nach Bedarf implementiert werden)
                        player.sendMessage("Admin-Befehle: /lynx, /uperms, usw."); // Beispielnachricht
                        break;
                    case ANVIL:
                        // Führe automatisch den /uperms-Befehl aus
                        player.performCommand("uperms");
                        break;
                    case CHEST:
                        // Öffne Kosmetik-Einstellungen (kann später implementiert werden)
                        player.sendMessage("Kosmetik-Einstellungen sind noch nicht implementiert.");
                        break;
                }
            }
        }
    }
}