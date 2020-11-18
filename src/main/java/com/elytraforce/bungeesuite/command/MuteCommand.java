package com.elytraforce.bungeesuite.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.model.Mute;
import com.elytraforce.bungeesuite.model.Punishment;
import com.elytraforce.bungeesuite.util.TimeFormatUtil;

public class MuteCommand extends PunishCommand {

    public MuteCommand(Main plugin) {
        super(plugin, "mute", "elytraforce.helper",
                "/mute <player> [duration] <reason>");
    }

    @Override
    public Punishment<?> getExistingPunishment(Connection connection, UUID id) {
        try {
            return getPlugin().getActiveMute(connection, id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("deprecation")
	@Override
    public void issueNewPunishment(CommandSender sender, Connection connection, String targetName, UUID id, long expiry, String reason) {
        Mute mute = null;
        try (PreparedStatement insertBan =
                     connection.prepareStatement("INSERT INTO player_punish(banned_id, sender_id, reason, creation_date, expiry_date, type) " +
                             "VALUES (?, ?, ?, ?, ?, 'mute')", PreparedStatement.RETURN_GENERATED_KEYS)) {
            Timestamp created = new Timestamp(System.currentTimeMillis());
            insertBan.setString(1, id.toString());
            insertBan.setString(2, getPlugin().getUniqueIdSafe(sender));
            insertBan.setString(3, reason);
            insertBan.setTimestamp(4, created);
            insertBan.setTimestamp(5, expiry == -1 ? null : new Timestamp(expiry));
            insertBan.executeUpdate();

            try (ResultSet result = insertBan.getGeneratedKeys()) {
                // Get the generated ID for the death
                if (result.next()) {
                    mute =  new Mute(result.getInt(1), getPlugin().getUniqueId(sender), id, reason,
                            new Timestamp(System.currentTimeMillis()), expiry == -1 ? null : new Timestamp(expiry));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        ProxiedPlayer target = getPlugin().getProxy().getPlayer(id);
        String timeFormatted = expiry == -1 ? "Permanent" : TimeFormatUtil.toDetailedDate(expiry, true);
        if (target != null) {
            getPlugin().registerMute(id, mute);
            target.sendMessage(String.format(getConfig().getPrefix() + ChatColor.RED + "You were muted by %s for %s (%s)", sender.getName(), reason, timeFormatted));
        }
        String name = target == null ? targetName : target.getName();
        // Broadcast full message
        getPlugin().broadcast(ChatColor.RED + String.format(getConfig().getPrefix() + "%s was muted by %s for %s (%s)", name, sender.getName(), reason, timeFormatted), "elytraforce.helper");
    }
}
