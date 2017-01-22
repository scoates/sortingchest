package com.seancoates.minecraft.sortingchest;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.logging.Level;
import java.io.IOException;

public class SortingChestPlugin extends JavaPlugin implements Listener {

    private File configFile;
    private YamlConfiguration config;

    public Permission sort_allowed = new Permission("sortingchest.sort");

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        // take care of configuration
        configFile = new File(getDataFolder(), "config.yml");
        saveDefaultConfig();
        reloadConfig();

        getLogger().info("Enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled.");
    }

    @EventHandler
    public void onInv(InventoryCloseEvent event) {
        // when a user closes a chest
        // (or other inventory screen, but exits when not a chest)
        ChestHandler chesthandler = new ChestHandler(this, event);
        chesthandler.doInvClose();
    }

    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    @Override
    public void reloadConfig() {
        config = new YamlConfiguration();
        config.options().pathSeparator('/');
        try {
            config.load(configFile);
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Could not load configuration", ex);
        }
    }

    @Override
    public void saveConfig() {
        // Make sure there are keys
        if (config.getKeys(true).size() == 0) {
            return;
        }

        try {
            config.save(configFile);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save configuration", ex);
        }
    }

    // debug messaging
    public void debug(String message, Player player) {
        if (getConfig().getBoolean("debug", false)) {
            getLogger().info("Debug: " + message);
        }
        if (getConfig().getBoolean("debug_to_player", false)) {
            player.sendMessage("[ChestSorter]Debug: " + message);
        }
    }
}
