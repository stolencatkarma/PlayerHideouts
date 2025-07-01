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
        // Invite/Uninvite/List commands
        if (args.length >= 2 && (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("uninvite"))) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players may use this command.");
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("hideout.use")) {
                player.sendMessage("You do not have permission to use hideouts.");
                return true;
            }
            if (!manager.hasHideout(player)) {
                player.sendMessage("You don't have a hideout.");
                return true;
            }
            String targetName = args[1];
            Player target = org.bukkit.Bukkit.getPlayerExact(targetName);
            java.util.UUID targetId = null;
            if (target != null) {
                targetId = target.getUniqueId();
            } else {
                org.bukkit.OfflinePlayer offline = org.bukkit.Bukkit.getOfflinePlayer(targetName);
                if (offline != null && offline.hasPlayedBefore()) {
                    targetId = offline.getUniqueId();
                }
            }
            if (targetId == null) {
                sender.sendMessage("Player not found: " + targetName);
                return true;
            }
            try {
                if (args[0].equalsIgnoreCase("invite")) {
                    boolean added = manager.inviteToHideout(player, targetId);
                    if (added) {
                        player.sendMessage("Invited " + targetName + " to your hideout.");
                    } else {
                        player.sendMessage(targetName + " is already invited.");
                    }
                } else {
                    boolean removed = manager.uninviteFromHideout(player, targetId);
                    if (removed) {
                        player.sendMessage("Uninvited " + targetName + ".");
                    } else {
                        player.sendMessage(targetName + " was not invited.");
                    }
                }
            } catch (IOException e) {
                player.sendMessage("An error occurred: " + e.getMessage());
                e.printStackTrace();
            }
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("invited")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players may use this command.");
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("hideout.use")) {
                player.sendMessage("You do not have permission to use hideouts.");
                return true;
            }
            if (!manager.hasHideout(player)) {
                player.sendMessage("You don't have a hideout.");
                return true;
            }
            java.util.List<java.util.UUID> invited = manager.getInvitedPlayers(player);
            if (invited.isEmpty()) {
                player.sendMessage("No one is invited to your hideout.");
            } else {
                StringBuilder sb = new StringBuilder("Invited: ");
                for (java.util.UUID uuid : invited) {
                    org.bukkit.OfflinePlayer off = org.bukkit.Bukkit.getOfflinePlayer(uuid);
                    sb.append(off.getName() != null ? off.getName() : uuid.toString()).append(", ");
                }
                player.sendMessage(sb.substring(0, sb.length() - 2));
            }
            return true;
        }
        // Admin commands
        if (args.length >= 2 && args[0].equalsIgnoreCase("admin")) {
            if (!sender.hasPermission("hideout.admin")) {
                sender.sendMessage("You do not have permission to use admin hideout commands.");
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage("Usage: /hideout admin <enter|delete> <player>");
                return true;
            }
            String adminSub = args[1].toLowerCase();
            String targetName = args[2];
            Player target = org.bukkit.Bukkit.getPlayerExact(targetName);
            java.util.UUID targetId = null;
            if (target != null) {
                targetId = target.getUniqueId();
            } else {
                // Try offline player
                org.bukkit.OfflinePlayer offline = org.bukkit.Bukkit.getOfflinePlayer(targetName);
                if (offline != null && offline.hasPlayedBefore()) {
                    targetId = offline.getUniqueId();
                }
            }
            if (targetId == null) {
                sender.sendMessage("Player not found: " + targetName);
                return true;
            }
            try {
                switch (adminSub) {
                    case "enter":
                        if (!(sender instanceof Player)) {
                            sender.sendMessage("Only players can be teleported.");
                            return true;
                        }
                        Player admin = (Player) sender;
                        boolean success = manager.warpToHideout(targetId, admin);
                        if (success) {
                            admin.sendMessage("Teleported to " + targetName + "'s hideout.");
                        } else {
                            admin.sendMessage("That player does not have a hideout or it is not loaded.");
                        }
                        break;
                    case "delete":
                        boolean deleted = manager.deleteHideout(targetId);
                        if (deleted) {
                            sender.sendMessage("Hideout deleted for " + targetName + ".");
                        } else {
                            sender.sendMessage("That player does not have a hideout.");
                        }
                        break;
                    default:
                        sender.sendMessage("Unknown admin subcommand. Use enter or delete.");
                }
            } catch (IOException e) {
                sender.sendMessage("An error occurred: " + e.getMessage());
                e.printStackTrace();
            }
            return true;
        }

        // Player commands
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
            player.sendMessage("Usage: /hideout <create|warp|leave|delete>");
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
                    player.sendMessage("Unknown subcommand. Use create, warp, leave or delete.");
            }
        } catch (IOException e) {
            player.sendMessage("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }
}
