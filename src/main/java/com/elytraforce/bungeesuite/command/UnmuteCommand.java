package com.elytraforce.bungeesuite.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.model.Mute;

public class UnmuteCommand extends BungeeCommand {

    public UnmuteCommand(Main plugin) {
        super(plugin, "unmute", "elytraforce.mod");
    }

    @SuppressWarnings("deprecation")
	@Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            // TODO: Better usage messages
            sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Usage: /unmute <player> [reason]");
            return;
        }

        try (Connection connection = getPlugin().getDatabase().getConnection()) {
            UUID id = getUuidFromArg(connection, 0, args);

            if (id == null) {
                // Never joined the server
                sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "That player has never joined the server");
            } else {
                Mute mute = getPlugin().getActiveMute(connection, id);
                if (mute == null) {
                    sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "That user is not muted");
                } else {
                    String reason = args.length > 1 ? getReasonFromArgs(1, args) : null;

                    if (reason == null && !sender.hasPermission("elytraforce.admin")) { // Check sender perms
                        sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Please specify a valid unmute reason");
                        sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Usage: /unmute <player> [reason]");
                        return;
                    }

                    try (PreparedStatement insertUnban = connection.prepareStatement("INSERT INTO player_punish_reverse" +
                            "(punish_id, banned_id, sender_id, reason) " +
                            "VALUES (?, ?, ?, ?)")) {
                        insertUnban.setInt(1, mute.getId());
                        insertUnban.setString(2, mute.getPunished().toString());
                        insertUnban.setString(3, getPlugin().getUniqueIdSafe(sender));
                        insertUnban.setString(4, reason);
                        insertUnban.executeUpdate();
                    }

                    ProxiedPlayer target = getPlugin().getProxy().getPlayer(id);
                    if (target != null) {
                        target.sendMessage(getConfig().getPrefix() + ChatColor.GREEN + "You were unmuted by " + sender.getName());
                        getPlugin().unregisterMute(target);
                    }
                    String name = target == null ? args[0] : target.getName();
                    getPlugin().broadcast(getConfig().getPrefix() + ChatColor.RED + String.format("%s was unmuted by %s", name, sender.getName()), "elytraforce.mod");
                }
            }
        } catch (SQLException e) {
            sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "An error occurred when issuing the unmute");
            getPlugin().getLogger().log(Level.SEVERE, "Failed to issue unmute", e);
        }
    }
}
