package com.elytraforce.bungeesuite.command;

import com.elytraforce.bungeesuite.config.PluginConfig;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.model.Punishment;
import com.elytraforce.bungeesuite.util.TimeFormatUtil;

public class BanCommand extends PunishCommand {

    public BanCommand(Main plugin) {
        super(plugin, "ban", "elytraforce.mod",
                "/ban <player> [duration] <reason>");
    }

    @Override
    public Punishment<?> getExistingPunishment(Connection connection, UUID id) {
        try {
            return getPlugin().getActiveBan(connection, id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("deprecation")
	@Override
    public void issueNewPunishment(CommandSender sender, Connection connection, String targetName, UUID id, long expiry, String reason) {
        if (reason.toLowerCase().startsWith("blacklist")
            && sender != getPlugin().getProxy().getConsole()) {
            sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Blacklist bans may only be issued from the console");
            return;
        }

        banPlayer(getPlugin(), sender, connection, targetName, id, expiry, reason);
    }

    public static void banPlayer(Main plugin, CommandSender sender, Connection connection, String targetName, UUID id, long expiry, String reason) {
        try (PreparedStatement insertBan =
                     connection.prepareStatement("INSERT INTO player_punish(banned_id, sender_id, reason, creation_date, expiry_date, type) " +
                             "VALUES (?, ?, ?, ?, ?, 'ban')")) {
            insertBan.setString(1, id.toString());
            insertBan.setString(2, plugin.getUniqueIdSafe(sender));
            insertBan.setString(3, reason);
            insertBan.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            insertBan.setTimestamp(5, expiry == -1 ? null : new Timestamp(expiry));
            insertBan.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        ProxiedPlayer target = plugin.getProxy().getPlayer(id);
        String timeFormatted = expiry == -1 ? "Permanent" : TimeFormatUtil.toDetailedDate(expiry, true);
        if (target != null) {
        	target.disconnect(ChatColor.translateAlternateColorCodes('&',
                    String.format("&cYou are banned from &b&lElytra&f&lForce" +
                    		"\n\n&cAuthor: &7%s" +
                            "\n\n&cReason: &7%s" +
                            "\nExpires: &7%s" +
                            "\n\n&c&lAppeal at &7elytraforce.com", sender.getName(), reason, timeFormatted)));
        }
        String name = target == null ? targetName : target.getName();
        // Broadcast full message
        plugin.broadcast(PluginConfig.get().getPrefix() + ChatColor.RED + String.format("%s was banned by %s for %s (%s)", name, sender.getName(), reason, timeFormatted), "elytraforce.helper");
    }
}
