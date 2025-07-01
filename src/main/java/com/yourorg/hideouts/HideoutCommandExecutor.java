package com.yourorg.hideouts;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class HideoutCommandExecutor implements CommandExecutor {
    private final HideoutManager manager;

    public HideoutCommandExecutor(HideoutManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may use this command.");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("hideout.use")) {
            player.sendMessage("You do not have permission to use hideouts.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage("Usage: /hideout <create|warp|delete>");
            return true;
        }
        String sub = args[0].toLowerCase();
        try {
            switch (sub) {
                case "create":
                    if (manager.hasHideout(player)) {
                        player.sendMessage("You already have a hideout.");
                    } else {
                        manager.createHideout(player);
                        player.sendMessage("Hideout created.");
                    }
                    break;
                case "warp":
                    if (!manager.hasHideout(player)) {
                        player.sendMessage("You don't have a hideout. Use /hideout create first.");
                    } else {
                        manager.warpToHideout(player);
                        player.sendMessage("Warped to your hideout.");
                    }
                    break;
                case "leave":
                    manager.leaveHideout(player);
                    player.sendMessage("You have left your hideout and returned to the main world.");
                    break;
                case "delete":
                    if (!manager.hasHideout(player)) {
                        player.sendMessage("You don't have a hideout.");
                    } else {
                        manager.deleteHideout(player);
                        player.sendMessage("Your hideout was deleted.");
                    }
                    break;
                default:
                    player.sendMessage("Unknown subcommand. Use create, warp or delete.");
            }
        } catch (IOException e) {
            player.sendMessage("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }
}
