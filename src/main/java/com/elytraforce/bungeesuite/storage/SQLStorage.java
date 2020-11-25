package com.elytraforce.bungeesuite.storage;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.config.PluginConfig;
import com.elytraforce.bungeesuite.model.Ban;
import com.elytraforce.bungeesuite.model.IpBan;
import com.elytraforce.bungeesuite.model.Mute;
import com.elytraforce.bungeesuite.rappu_b.Database;
import com.elytraforce.bungeesuite.util.AuriBungeeUtil;
import com.elytraforce.bungeesuite.util.TimeFormatUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class SQLStorage {
    private static SQLStorage instance;
    private Database database;

    private final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");

    private SQLStorage() {
        database = Database.newDatabase()
                .withPluginUsing(Main.get())
                .withUsername(PluginConfig.get().getDatabaseUser())
                .withPassword(PluginConfig.get().getDatabasePassword())
                .withConnectionInfo(PluginConfig.get().getDatabase(), PluginConfig.get().getDatabasePort(), PluginConfig.get().getDatabaseName(),false)
                .withDefaultProperties()
                .open();
        try {
            database.createTablesFromSchema("table.ddl", Main.class);
        } catch (SQLException | IOException e) {
            AuriBungeeUtil.logError("Error when initializing the BungeeSuite SQL table! See this error:");
            e.printStackTrace();
        }
    }

    public void shutdown() {
        database.close();
    }

    public void trackLogin(PendingConnection connection) {
        String sql = "INSERT INTO player_login(id, name, ip_address) VALUES (?, ?, INET_ATON(?));";

        Object[] toSet = new Object[]{
                connection.getUniqueId().toString(),
                connection.getName(),
                connection.getAddress().getAddress().getHostAddress()
        };

        database.updateAsync(sql,toSet,c -> {});
    }

    //things we've learned retard - these cannot have fucking return values. do not try to completeAsync them either, they must be completed synchronously.
    public void mutePlayer(String sender, String targetName, UUID id, long expiry, String reason) {
        String sql = "INSERT INTO player_punish(banned_id, sender_id, reason, creation_date, expiry_date, type) VALUES (?, ?, ?, ?, ?, 'mute');";

        Object[] toSet = new Object[]{
                id.toString(),
                sender,
                reason,
                new Timestamp(System.currentTimeMillis()),
                expiry == -1 ? null : new Timestamp(expiry)
        };

        database.updateAsync(sql,toSet, c-> {});

    }

    public void kickPlayer(String sender, String targetName, UUID id, String reason) {
        String sql = "INSERT INTO player_punish (banned_id, sender_id, reason, creation_date, expiry_date, type) VALUES (?, ?, ?, ?, ?, 'kick');";

        Object[] toSet = new Object[]{
                id.toString(),
                sender,
                reason,
                new Timestamp(System.currentTimeMillis()),
                null
        };

        database.updateAsync(sql,toSet,c -> {});
    }
    public void banPlayer(String sender, String targetName, UUID id, long expiry, String reason) {
        String sql = "INSERT INTO player_punish (banned_id, sender_id, reason, creation_date, expiry_date, type) VALUES (?, ?, ?, ?, ?, 'ban');";

        Object[] toSet = new Object[]{
                id.toString(),
                sender,
                reason,
                new Timestamp(System.currentTimeMillis()),
                expiry == -1 ? null : new Timestamp(expiry)
        };

        database.updateAsync(sql,toSet,c -> {});
    }

    public void warnPlayer(String sender, String targetName, UUID id, long expiry, String reason) {
        String sql = "INSERT INTO player_punish (banned_id, sender_id, reason, creation_date, expiry_date, type) VALUES (?, ?, ?, ?, ?, 'warn');";

        Object[] toSet = new Object[]{
                id.toString(),
                sender,
                reason,
                new Timestamp(System.currentTimeMillis()),
                expiry == -1 ? null : new Timestamp(expiry)
        };

        database.updateAsync(sql,toSet,c -> {});
    }

    public void unBanPlayer(Ban ban, CommandSender sender, String reason) {
        String sql = "INSERT INTO player_punish_reverse (punish_id, banned_id, sender_id, reason) VALUES (?, ?, ?, ?);";

        Object[] toSet = new Object[]{
                ban.getId(),
                ban.getPunished().toString(),
                Main.get().getUniqueIdSafe(sender),
                reason
        };

        database.updateAsync(sql,toSet,c -> {});
    }

    public void unMutePlayer(Mute mute, CommandSender sender, String reason) {
        String sql = "INSERT INTO player_punish_reverse(punish_id, banned_id, sender_id, reason) VALUES (?, ?, ?, ?);";

        Object[] toSet = new Object[]{
                mute.getId(),
                mute.getPunished().toString(),
                Main.get().getUniqueIdSafe(sender),
                reason
        };

        database.updateAsync(sql,toSet,c -> {});
    }

    public CompletableFuture<UUID> getIDFromUsername(String name) {
        CompletableFuture<UUID> future = new CompletableFuture<>();

        if (name == null) { future.complete(null); return future; }

        ProxiedPlayer online = Main.get().getProxy().getPlayer(name);
        if (online != null) {
            future.complete(online.getUniqueId()); return future;
        }

        String sql = "SELECT id FROM player_login WHERE name = ? ORDER BY time DESC LIMIT 1;";

        database.queryAsync(sql, new Object[]{name}, resultSet -> {
            if (resultSet.next()) {
                AuriBungeeUtil.logError("RESULTSET HAS NEXT");
                try {
                    AuriBungeeUtil.logError(resultSet.getString("id"));
                    future.complete(UUID.fromString(resultSet.getString("id")));
                } catch (SQLException e) {
                    e.printStackTrace();
                    future.complete(null);
                }
            } else {
                AuriBungeeUtil.logError("RESULTSET DOESNT HAVE NEXT");
                future.complete(null);
            }
        });

        return future;
    }
    public CompletableFuture<String> getUsernameFromID(UUID id) {
        CompletableFuture<String> future = new CompletableFuture<>();

        if (id == null) { future.complete(null); return future; }

        ProxiedPlayer online = Main.get().getProxy().getPlayer(id);
        if (online != null) {
            future.complete(online.getName()); return future;
        }

        String sql = "SELECT name FROM player_login WHERE id = ? ORDER BY time DESC LIMIT 1;";

        database.queryAsync(sql, new Object[]{id.toString()}, resultSet -> {
            if (resultSet.next()) {
                try {
                    future.complete(resultSet.getString("name"));
                } catch (SQLException e) {
                    e.printStackTrace(); e.printStackTrace();
                    future.complete(null);
                }
            } else {
                future.complete(null);
            }
        });

        return future;
    }

    public CompletableFuture<Ban> getActiveBan(UUID id) {
        CompletableFuture<Ban> future = new CompletableFuture<>();

        if (id == null) { future.complete(null); return future; }

        String sql = "SELECT * FROM player_active_punishment WHERE banned_id = ? AND type = 'ban';";

        database.queryAsync(sql, new Object[]{id.toString()}, resultSet -> {
            if (resultSet.next()) {

                String sender = resultSet.getString("sender_id");
                try {
                    future.complete(new Ban(resultSet.getInt("id"),
                            sender != null ? UUID.fromString(resultSet.getString("sender_id")) : null,
                            id,
                            resultSet.getString("reason"),
                            resultSet.getTimestamp("creation_date"),
                            resultSet.getTimestamp("expiry_date")));
                } catch (SQLException e) {
                    e.printStackTrace();
                    future.complete(null);
                }
            } else {
                future.complete(null);
            }
        });

        return future;
    }

    public CompletableFuture<Mute> getActiveMute(UUID id) {
        CompletableFuture<Mute> future = new CompletableFuture<>();

        if (id == null) { future.complete(null); return future; }

        String sql = "SELECT * FROM player_active_punishment WHERE banned_id = ? AND type = 'mute';";

        database.queryAsync(sql, new Object[]{id.toString()}, resultSet -> {
            if (resultSet.next()) {
                String sender = resultSet.getString("sender_id");
                try {
                    future.complete(new Mute(resultSet.getInt("id"),
                            sender != null ? UUID.fromString(resultSet.getString("sender_id")) : null,
                            id,
                            resultSet.getString("reason"),
                            resultSet.getTimestamp("creation_date"),
                            resultSet.getTimestamp("expiry_date")));
                } catch (SQLException e) {
                    e.printStackTrace();;
                    future.complete(null);
                }
            } else {
                future.complete(null);
            }
        });

        return future;
    }

    public CompletableFuture<IpBan> getActiveIpBan(String address) {
        CompletableFuture<IpBan> future = new CompletableFuture<>();

        if (address == null) { future.complete(null); return future; }

        String sql = "SELECT * FROM player_active_ip_ban WHERE ip_address = ?;";

        database.queryAsync(sql, new Object[]{address}, resultSet -> {
            if (resultSet.next()) {
                try {
                    String sender = resultSet.getString("sender_id");
                    future.complete(new IpBan(resultSet.getInt("id"),
                            sender != null ? UUID.fromString(resultSet.getString("sender_id")) : null,
                            resultSet.getString("ip_address"),
                            resultSet.getString("reason"),
                            resultSet.getTimestamp("creation_date"),
                            resultSet.getTimestamp("expiry_date")));
                } catch (SQLException e) {
                    e.printStackTrace();
                    future.complete(null);
                }

            } else {
                future.complete(null);
            }
        });

        return future;
    }

    public CompletableFuture<ArrayList<String>> getAlts(UUID id) {
        CompletableFuture<ArrayList<String>> future = new CompletableFuture<>();

        if (id == null) { AuriBungeeUtil.logError("id is null!");future.complete(new ArrayList<>()); return future; }

        String sql = "SELECT DISTINCT NAME FROM player_login WHERE ip_address IN (SELECT DISTINCT ip_address FROM player_login WHERE id = ?);";

        database.queryAsync(sql, new Object[]{id.toString()}, resultSet -> {
            ArrayList<String> collectedString = new ArrayList<>();
            try {
                while (resultSet.next()) {
                    String result = resultSet.getString("name");
                    collectedString.add(result);
                }
                future.complete(collectedString);
            } catch (SQLException e) {
                e.printStackTrace();
                future.complete(new ArrayList<>());
            }

        });

        return future;
    }

    public CompletableFuture<ArrayList<ComponentBuilder>> getPunishments(UUID id) {
        CompletableFuture<ArrayList<ComponentBuilder>> future = new CompletableFuture<>();

        if (id == null) { future.complete(new ArrayList<>()); return future; }

        String sql = "SELECT " +
                "(SELECT name FROM player_login pl WHERE pl.id = p.sender_id ORDER BY time DESC LIMIT 1) name, " +
                "pr.sender_id reverse_sender_id, " +
                "pr.reason reverse_reason, " +
                "pr.creation_date reverse_date, " +
                "p.reason, " +
                "p.creation_date, " +
                "expiry_date, " +
                "type " +
                "FROM player_punish p " +
                "LEFT JOIN player_punish_reverse pr " +
                "ON pr.punish_id = p.id " +
                "WHERE p.banned_id = ? " +
                "ORDER BY creation_date;";

        database.queryAsync(sql, new Object[]{id.toString()}, resultSet -> {
            ArrayList<ComponentBuilder> collectedString = new ArrayList<>();
            try {
                while (resultSet.next()) {
                    String raw = resultSet.getString("name");
                    String name = raw == null ? "Console" : raw;
                    String reason = resultSet.getString("reason");
                    Timestamp created = resultSet.getTimestamp("creation_date");
                    Timestamp expiry = resultSet.getTimestamp("expiry_date");
                    String type = resultSet.getString("type");

                    ComponentBuilder builder = new ComponentBuilder(" ");
                    ComponentBuilder hoverBuilder = new ComponentBuilder("Reason: ").color(ChatColor.GRAY)
                            .append(reason).color(ChatColor.WHITE);
                    if (resultSet.getObject("reverse_date") != null) {
                        builder.append("").strikethrough(true);
                        String reversed = resultSet.getString("reverse_sender_id");
                        String reverseName = reversed == null ? "Console" : reversed;

                        hoverBuilder.append("\n").append("Reversed By: ").color(ChatColor.GRAY)
                                .append(reverseName).color(ChatColor.WHITE);

                        String reverseReason = resultSet.getString("reverse_reason");
                        if (reverseReason != null) {
                            hoverBuilder.append("\n").append("Reverse Reason: ").color(ChatColor.GRAY)
                                    .append(reverseReason).color(ChatColor.WHITE);
                        }
                    }

                    builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverBuilder.create()));

                    if (type.equalsIgnoreCase("warn")) {
                        builder.append("[Warn]").color(ChatColor.YELLOW).append(" ");
                    } else if (type.equalsIgnoreCase("mute")) {
                        builder.append("[Mute]").color(ChatColor.GOLD).append(" ").color(ChatColor.BOLD);
                    } else if (type.equalsIgnoreCase("kick")) {
                        builder.append("[Kick]").color(ChatColor.GRAY).append("  ").color(ChatColor.BOLD);
                    } else {
                        builder.append("[Ban]").color(ChatColor.RED).append("  ").color(ChatColor.BOLD);
                    }

                    long duration = expiry == null ? -1 : expiry.getTime() - created.getTime();
                    builder.strikethrough(false).append(dateFormat.format(created)).color(ChatColor.GREEN)
                            .append(" ").append(name).color(ChatColor.GRAY)
                            .append(" (").color(ChatColor.RED)
                            .append(duration == -1 ? "Permanent" : TimeFormatUtil.toDetailedDate(0, duration, true))
                            .append(")");

                    if (expiry != null && expiry.getTime() < System.currentTimeMillis()) {
                        builder.append("(Expired)").color(ChatColor.RED);
                    }

                    collectedString.add(builder);

                }
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            future.complete(collectedString);
        });


        return future;
    }

    public CompletableFuture<ArrayList<String>> getBannedAlts(UUID uuid) {
        CompletableFuture<ArrayList<String>> future = new CompletableFuture<>();

        if (uuid == null) { future.complete(new ArrayList<>());return future; }

        String sql = "SELECT DISTINCT name " +
                "FROM player_login " +
                "INNER JOIN (SELECT DISTINCT banned_id " +
                "FROM player_active_punishment " +
                "WHERE type = 'ban') AS b " +
                "WHERE player_login.id = b.banned_id " +
                "AND ip_address " +
                "IN (SELECT DISTINCT ip_address " +
                "FROM player_login " +
                "WHERE id = ?);";

        database.queryAsync(sql, new Object[]{uuid.toString()}, resultSet -> {
            ArrayList<String> collectedString = new ArrayList<>();
            try {
                while (resultSet.next()) {
                    collectedString.add(resultSet.getString("name"));
                }
                future.complete(collectedString);
            } catch (SQLException e) {
                e.printStackTrace();
                future.complete(new ArrayList<>());
            }
        });

        return future;
    }

    public static SQLStorage get() {
        return Objects.requireNonNullElseGet(instance, () -> instance = new SQLStorage());
    }
}
