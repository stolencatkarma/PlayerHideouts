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
    /**
     * Add a player to the hideout whitelist.
     */
    public boolean inviteToHideout(Player owner, UUID invitee) throws IOException {
        String key = owner.getUniqueId().toString() + ".invited";
        java.util.List<String> list = cfg.getStringList(key);
        String inviteeStr = invitee.toString();
        if (!list.contains(inviteeStr)) {
            list.add(inviteeStr);
            cfg.set(key, list);
            cfg.save(cfgFile);
            return true;
        }
        return false;
    }

    /**
     * Remove a player from the hideout whitelist.
     */
    public boolean uninviteFromHideout(Player owner, UUID invitee) throws IOException {
        String key = owner.getUniqueId().toString() + ".invited";
        java.util.List<String> list = cfg.getStringList(key);
        String inviteeStr = invitee.toString();
        boolean removed = list.remove(inviteeStr);
        if (removed) {
            cfg.set(key, list);
            cfg.save(cfgFile);
        }
        return removed;
    }

    /**
     * Get the list of invited player UUIDs for a hideout owner.
     */
    public java.util.List<UUID> getInvitedPlayers(Player owner) {
        String key = owner.getUniqueId().toString() + ".invited";
        java.util.List<String> list = cfg.getStringList(key);
        java.util.List<UUID> uuids = new java.util.ArrayList<>();
        for (String s : list) {
            try {
                uuids.add(UUID.fromString(s));
            } catch (IllegalArgumentException ignored) {}
        }
        return uuids;
    }

    /**
     * Check if a player is invited to another's hideout.
     */
    public boolean isInvited(Player owner, UUID player) {
        String key = owner.getUniqueId().toString() + ".invited";
        java.util.List<String> list = cfg.getStringList(key);
        return list.contains(player.toString());
    }
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
        long seed = new java.util.Random().nextLong();
        org.bukkit.WorldCreator wc = new org.bukkit.WorldCreator(worldName)
            .seed(seed)
            .environment(org.bukkit.World.Environment.NORMAL)
            .generateStructures(true);

        // Use Paper's async world creation
        wc.createWorldAsync(world -> {
            if (world == null) {
                p.sendMessage("§cFailed to create your hideout. Please try again later.");
                return;
            }
            // Set a default world border around spawn
            org.bukkit.WorldBorder border = world.getWorldBorder();
            org.bukkit.Location spawn = world.getSpawnLocation();
            border.setCenter(spawn.getX(), spawn.getZ());
            border.setSize(1000);
            cfg.set(id.toString(), worldName);
            try {
                cfg.save(cfgFile);
            } catch (IOException e) {
                p.sendMessage("§cFailed to save hideout config!");
            }
            p.teleport(world.getSpawnLocation());
            p.sendMessage("§aYour hideout is ready!");
        });
    }

    public void warpToHideout(Player p) {
        String worldName = cfg.getString(p.getUniqueId().toString());
        World w = Bukkit.getWorld(worldName);
        if (w != null) p.teleport(w.getSpawnLocation());
    }

    /**
     * Teleport an admin to another player's hideout.
     * @param playerId The UUID of the player whose hideout to enter
     * @param admin The admin to teleport
     * @return true if successful, false if world not found
     */
    public boolean warpToHideout(UUID playerId, Player admin) {
        String worldName = cfg.getString(playerId.toString());
        World w = Bukkit.getWorld(worldName);
        if (w != null) {
            admin.teleport(w.getSpawnLocation());
            return true;
        }
        return false;
    }

    /**
     * Get the World object for a player's hideout.
     */
    public World getHideoutWorld(UUID playerId) {
        String worldName = cfg.getString(playerId.toString());
        return Bukkit.getWorld(worldName);
    }

    /**
     * Delete any player's hideout by UUID (admin version).
     * @param playerId The UUID of the player whose hideout to delete
     * @return true if deleted, false if not found
     */
    public boolean deleteHideout(UUID playerId) throws IOException {
        String worldName = cfg.getString(playerId.toString());
        if (worldName == null) return false;
        Bukkit.unloadWorld(worldName, false);
        File worldFolder = new File(plugin.getServer().getWorldContainer(), worldName);
        org.apache.commons.io.FileUtils.deleteDirectory(worldFolder);
        cfg.set(playerId.toString(), null);
        cfg.save(cfgFile);
        return true;
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
