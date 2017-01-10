package com.seancoates.minecraft.sortingchest;

import org.bukkit.permissions.Permission;
//import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class SortingChest extends JavaPlugin {

//	public PlayerListener playerListener = new PlayerListener();
	public ChestListener blockListener = new ChestListener();

	public Permission permCreateAny = new Permission("sortingchest.create.any");

	public void onEnable() {
//		getServer().getPluginManager().registerEvents(this.playerListener, this);
		getServer().getPluginManager().registerEvents(this.blockListener, this);

		getLogger().info("Enabled.");
	}

	public void onDisable() {
		getLogger().info("Disabled.");
	}

}