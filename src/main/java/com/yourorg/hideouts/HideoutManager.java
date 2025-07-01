package com.yourorg.hideouts;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class HideoutManager {
    private final JavaPlugin plugin;
    // Store the main world name at startup for leave logic
    private final String mainWorldName;
    private final YamlConfiguration cfg;
    private final File cfgFile;

    public HideoutManager(JavaPlugin plugin) {
        this.plugin = plugin;
        // Capture the primary world name (first loaded)
        this.mainWorldName = plugin.getServer().getWorlds().get(0).getName();
        // ...existing code... (no template dir initialization)
        this.cfgFile = new File(plugin.getDataFolder(), "hideouts.yml");
        this.cfg = YamlConfiguration.loadConfiguration(cfgFile);
    }

    /**
     * Teleports player back to the main world spawn.
     */
    public void leaveHideout(Player p) {
        // Try to get the default world by name, fallback to first loaded world
        // Teleport back to the server's main world
        org.bukkit.World main = plugin.getServer().getWorld(mainWorldName);
        p.teleport(main.getSpawnLocation());
    }

    public boolean hasHideout(Player p) {
        return cfg.contains(p.getUniqueId().toString());
    }

    public void createHideout(Player p) throws IOException {
        UUID id = p.getUniqueId();
        String worldName = "hideout_" + id;
        // Generate a random new world for this player
        long seed = new java.util.Random().nextLong();
        org.bukkit.WorldCreator wc = new org.bukkit.WorldCreator(worldName)
            .seed(seed)
            .environment(org.bukkit.World.Environment.NORMAL)
            .generateStructures(true);
        org.bukkit.World w = Bukkit.createWorld(wc);
        // Set a default world border around spawn
        org.bukkit.WorldBorder border = w.getWorldBorder();
        org.bukkit.Location spawn = w.getSpawnLocation();
        border.setCenter(spawn.getX(), spawn.getZ());
        border.setSize(1000);
        cfg.set(id.toString(), worldName);
        cfg.save(cfgFile);
        p.teleport(w.getSpawnLocation());
    }

    public void warpToHideout(Player p) {
        String worldName = cfg.getString(p.getUniqueId().toString());
        World w = Bukkit.getWorld(worldName);
        if (w != null) p.teleport(w.getSpawnLocation());
    }

    public void deleteHideout(Player p) throws IOException {
        String worldName = cfg.getString(p.getUniqueId().toString());
        Bukkit.unloadWorld(worldName, false);
        // Remove the world folder from the server root
        File worldFolder = new File(plugin.getServer().getWorldContainer(), worldName);
        org.apache.commons.io.FileUtils.deleteDirectory(worldFolder);
        cfg.set(p.getUniqueId().toString(), null);
        cfg.save(cfgFile);
    }

    // No copyFolder needed since each hideout is a fresh world created at runtime.
}
