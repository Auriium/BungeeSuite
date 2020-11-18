package com.elytraforce.bungeesuite.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.model.Ban;

public class UnbanCommand extends BungeeCommand {

    public UnbanCommand(Main plugin) {
        super(plugin, "unban", "elytraforce.admin");
    }

    @SuppressWarnings("deprecation")
	@Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            // TODO: Better usage messages
            sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Usage: /unban <player> [reason]");
            return;
        }

        // ban <player> [duration] <reason>
        try (Connection connection = getPlugin().getDatabase().getConnection()) {
            UUID id = getUuidFromArg(connection, 0, args);

            if (id == null) {
                // Never joined the server
                sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "That player has never joined the server");
            } else {
                Ban ban = getPlugin().getActiveBan(connection, id);
                if (ban == null) {
                    sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "That user is not banned");
                } else {
                    String reason = args.length > 1 ? getReasonFromArgs(1, args) : null;

                    if (reason == null && !sender.hasPermission("elytraforce.admin")) { // Check sender perms
                        sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Please specify a valid unban reason");
                        sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Usage: /unban <player> [reason]");
                        return;
                    }

                    try (PreparedStatement insertUnban = connection.prepareStatement("INSERT INTO player_punish_reverse" +
                            "(punish_id, banned_id, sender_id, reason) " +
                            "VALUES (?, ?, ?, ?)")) {
                        insertUnban.setInt(1, ban.getId());
                        insertUnban.setString(2, ban.getPunished().toString());
                        insertUnban.setString(3, getPlugin().getUniqueIdSafe(sender));
                        insertUnban.setString(4, reason);
                        insertUnban.executeUpdate();
                    }

                    getPlugin().broadcast(getConfig().getPrefix() + ChatColor.RED + String.format("%s was unbanned by %s", args[0], sender.getName()), "elytraforce.helper");
                }
            }
        } catch (SQLException e) {
            sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "An error occurred when issuing the unban");
            getPlugin().getLogger().log(Level.SEVERE, "Failed to issue unban", e);
        }
    }
}
