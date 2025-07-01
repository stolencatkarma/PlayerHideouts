package com.yourorg.hideouts;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class HideoutWorldUnloadListener implements Listener {
    private final HideoutManager manager;

    public HideoutWorldUnloadListener(HideoutManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        World left = event.getFrom();
        checkAndUnload(left);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        World world = event.getPlayer().getWorld();
        // Delay to after quit so player is removed from world
        Bukkit.getScheduler().runTaskLater(manager.getPlugin(), () -> checkAndUnload(world), 1L);
    }

    private void checkAndUnload(World world) {
        if (world == null) return;
        String name = world.getName();
        // Only unload hideout worlds
        if (!name.startsWith("hideout_")) return;
        List<Player> players = world.getPlayers();
        if (players.isEmpty()) {
            Bukkit.unloadWorld(world, false);
        }
    }
}
