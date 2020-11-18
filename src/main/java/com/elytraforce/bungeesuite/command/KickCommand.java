package com.elytraforce.bungeesuite.command;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.model.Punishment;
import com.elytraforce.bungeesuite.util.TimeFormatUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.logging.Level;

public class KickCommand extends BungeeCommand {

    public KickCommand(Main plugin) {
        super(plugin, "kick", "elytraforce.helper");
        // "/mute <player> <reason>"
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "/kick <player> <reason>");
            return;
        }

        // ban <player> [duration] <reason>
        try (Connection connection = getPlugin().getDatabase().getConnection()) {
            UUID id = getUuidFromArg(connection, 0, args);
            ProxiedPlayer target = getPlugin().getProxy().getPlayer(id);

            if (target == null) {
                // Never joined the server
                sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "That player is not online!");
            } else {
                String reason = getReasonFromArgs(1, args);

                try (PreparedStatement ps = connection.prepareStatement("INSERT INTO player_punish(banned_id, sender_id, reason, creation_date, expiry_date, type) " +
                        "VALUES (?, ?, ?, ?, ?, 'kick')")) {
                    ps.setString(1, id.toString());
                    ps.setString(2, getPlugin().getUniqueIdSafe(sender));
                    ps.setString(3, reason);
                    ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                    ps.setTimestamp(5, null);
                    ps.executeUpdate();
                }

                target.disconnect(ChatColor.translateAlternateColorCodes('&',
                        String.format("&cYou were kicked from &b&lElytra&f&lForce" +
                                "\n\n&cAuthor: &7%s" +
                                "\n&cReason: &7%s" +
                                "\n\n&c&lAppeal at &7elytraforce.com", sender.getName(), reason)));


                String targetName = target.getName();
                getPlugin().broadcast(ChatColor.RED + String.format(getConfig().getPrefix() + "%s was kicked by %s for (%s)", targetName, sender.getName(), reason), "elytraforce.helper");
            }
        } catch (SQLException e) {
            sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "An error occurred when issuing the warning");
            getPlugin().getLogger().log(Level.SEVERE, "Failed to issue warning", e);
        }
    }
}

