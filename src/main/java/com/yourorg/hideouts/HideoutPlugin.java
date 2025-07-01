package com.yourorg.hideouts;

import org.bukkit.plugin.java.JavaPlugin;

public class HideoutPlugin extends JavaPlugin {

    private HideoutManager hideoutManager;

    @Override
    public void onEnable() {
        // Ensure plugin data folders
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        // Initialize manager
        hideoutManager = new HideoutManager(this);
        // Register command
        getCommand("hideout").setExecutor(new HideoutCommandExecutor(hideoutManager));
        // Register world unload listener
        getServer().getPluginManager().registerEvents(new HideoutWorldUnloadListener(hideoutManager), this);
        getLogger().info("HideoutPlugin enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("HideoutPlugin disabled.");
    }
}
