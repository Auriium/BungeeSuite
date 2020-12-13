package com.elytraforce.bungeesuite.storage;

import com.elytraforce.aUtils.database.BDatabase;
import com.elytraforce.aUtils.logger.BLogger;
import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.config.PluginConfig;
import com.elytraforce.bungeesuite.localchat.player.ChatPlayer;
import com.elytraforce.bungeesuite.localchat.player.Settings;
import com.elytraforce.bungeesuite.localchat.player.Stats;
import com.elytraforce.bungeesuite.model.Ban;
import com.elytraforce.bungeesuite.model.IpBan;
import com.elytraforce.bungeesuite.model.Mute;
import com.elytraforce.bungeesuite.util.TimeFormatUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@SuppressWarnings("unused")
public class SQLStorage {
    private static SQLStorage instance;
    private BDatabase database;

    private final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");

    private SQLStorage() {
        database = BDatabase.newDatabase();
        database.withPluginUsing(Main.get())
                .withUsername(PluginConfig.get().getDatabaseUser())
                .withPassword(PluginConfig.get().getDatabasePassword())
                .withConnectionInfo(PluginConfig.get().getDatabase(), PluginConfig.get().getDatabasePort(), PluginConfig.get().getDatabaseName(),false)
                .withDefaultProperties()
                .open();
        try {
            database.createTablesFromSchema("table.ddl", Main.class);
        } catch (SQLException | IOException e) {
            BLogger.error("Error when initializing the BungeeSuite SQL table! See this error:");
            e.printStackTrace();
        }
        
    }

    public void shutdown() {
        database.close();
    }

    public CompletableFuture<Void> insertPlayer(@NotNull ChatPlayer player) {
        CompletableFuture<Void> future = new CompletableFuture<>();

            String first = "INSERT INTO players(id, name, nickname) VALUES (?,?,?);";
            String second = "INSERT INTO player_settings(foreign_id, discord_in, discord_out, pms, chat_color, chat_enabled, ignored_players) VALUES (?, ?, ?, ?, ?, ?, ?);";

            Object[] firstSet = new Object[]{
                    player.getUUID().toString(),
                    player.getName(),
                    player.getNicknameInternal()
            };

            Object[] secondSet = new Object[]{
                    player.getUUID().toString(),
                    player.getSettings().isDiscordIn(),
                    player.getSettings().isDiscordOut(),
                    player.getSettings().isPmsEnabled(),
                    player.getSettings().getChatColor().name(),
                    player.getSettings().isChatEnabled(),
                    new Gson().toJson(player.getSettings().getIgnoredPlayers())
            };

            return CompletableFuture.supplyAsync(() -> {
                try {
                    database.update(first,firstSet);
                    database.update(second,secondSet);
                    return null;
                } catch (SQLException e) {
                    throw new CompletionException(e);
                }
            });
    }

    public CompletableFuture<Void> updatePlayer(@NotNull ChatPlayer player) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        String first = "UPDATE players SET name = ?, nickname = ? WHERE id = ?;";
        String second = "UPDATE player_settings SET discord_in = ?, discord_out = ?, pms = ?, chat_color = ?, chat_enabled = ?, ignored_players = ? WHERE foreign_id = ?;";

        Object[] firstSet = new Object[]{
                player.getName(),
                player.getNicknameInternal(),
                player.getUUID().toString()
        };

        Object[] secondSet = new Object[]{
                player.getSettings().isDiscordIn(),
                player.getSettings().isDiscordOut(),
                player.getSettings().isPmsEnabled(),
                player.getSettings().getChatColor().name(),
                player.getSettings().isChatEnabled(),
                new Gson().toJson(player.getSettings().getIgnoredPlayers()),
                player.getUUID().toString()
        };

        return CompletableFuture.supplyAsync(() -> {
            try {
                database.update(first,firstSet);
                database.update(second,secondSet);
                return null;
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    public CompletableFuture<Void> trackLogin(@NotNull PendingConnection connection) {
        String sql = "INSERT INTO player_login(foreign_id, name, ip_address) VALUES (?, ?, INET_ATON(?));";

        Object[] toSet = new Object[]{
                connection.getUniqueId().toString(),
                connection.getName(),
                connection.getAddress().getAddress().getHostAddress()
        };

        return CompletableFuture.supplyAsync(() -> {
            try {
                database.update(sql,toSet);
                return null;
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    //things we've learned retard - these cannot have fucking return values. do not try to completeAsync them either, they must be completed synchronously.
    public CompletableFuture<Void> mutePlayer(String sender, String targetName, UUID id, long expiry, String reason) {
        String sql = "INSERT INTO player_punish(banned_id, sender_id, reason, creation_date, expiry_date, type) VALUES (?, ?, ?, ?, ?, 'mute');";

        Object[] toSet = new Object[]{
                id.toString(),
                sender,
                reason,
                new Timestamp(System.currentTimeMillis()),
                expiry == -1 ? null : new Timestamp(expiry)
        };

        return CompletableFuture.supplyAsync(() -> {
            try {
                database.update(sql,toSet);
                return null;
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    public CompletableFuture<Void> kickPlayer(String sender, String targetName, UUID id, String reason) {
        String sql = "INSERT INTO player_punish (banned_id, sender_id, reason, creation_date, expiry_date, type) VALUES (?, ?, ?, ?, ?, 'kick');";

        Object[] toSet = new Object[]{
                id.toString(),
                sender,
                reason,
                new Timestamp(System.currentTimeMillis()),
                null
        };

        return CompletableFuture.supplyAsync(() -> {
            try {
                database.update(sql,toSet);
                return null;
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }
    public CompletableFuture<Void> banPlayer(String sender, String targetName, UUID id, long expiry, String reason) {
        String sql = "INSERT INTO player_punish (banned_id, sender_id, reason, creation_date, expiry_date, type) VALUES (?, ?, ?, ?, ?, 'ban');";

        Object[] toSet = new Object[]{
                id.toString(),
                sender,
                reason,
                new Timestamp(System.currentTimeMillis()),
                expiry == -1 ? null : new Timestamp(expiry)
        };

        return CompletableFuture.supplyAsync(() -> {
            try {
                database.update(sql,toSet);
                return null;
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    public CompletableFuture<Void> warnPlayer(String sender, String targetName, UUID id, String reason) {
        String sql = "INSERT INTO player_punish (banned_id, sender_id, reason, creation_date, expiry_date, type) VALUES (?, ?, ?, ?, ?, 'warn');";

        Object[] toSet = new Object[]{
                id.toString(),
                sender,
                reason,
                new Timestamp(System.currentTimeMillis()),
                null
        };

        return CompletableFuture.supplyAsync(() -> {
            try {
                database.update(sql,toSet);
                return null;
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    public CompletableFuture<Void> unBanPlayer(Ban ban, CommandSender sender, String reason) {
        String sql = "INSERT INTO player_punish_reverse (punish_id, banned_id, sender_id, reason) VALUES (?, ?, ?, ?);";

        Object[] toSet = new Object[]{
                ban.getId(),
                ban.getPunished().toString(),
                Main.get().getUniqueIdSafe(sender),
                reason
        };

        return CompletableFuture.supplyAsync(() -> {
            try {
                database.update(sql,toSet);
                return null;
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    public CompletableFuture<Void> unMutePlayer(Mute mute, CommandSender sender, String reason) {
        String sql = "INSERT INTO player_punish_reverse(punish_id, banned_id, sender_id, reason) VALUES (?, ?, ?, ?);";

        Object[] toSet = new Object[]{
                mute.getId(),
                mute.getPunished().toString(),
                Main.get().getUniqueIdSafe(sender),
                reason
        };

        return CompletableFuture.supplyAsync(() -> {
            try {
                database.update(sql,toSet);
                return null;
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    /*tldr make sre to store whatever cumes out of this*/
    /*get an unstored chatplayer object thing i dont know what the fuck im doing i just want to play rimworld god help me*/
    public CompletableFuture<ChatPlayer> getOrDefaultPlayer(@NotNull UUID player) {
        CompletableFuture<ChatPlayer> future = new CompletableFuture<>();

        Objects.requireNonNull(player);

        String sql = "SELECT * FROM player_combined_info WHERE id = ?;";

        future.completeAsync(() -> {
            try (ResultSet set = database.query(sql, new Object[]{player.toString()})) {
                int level;
                int exp;
                int money;
                String nick;
                String name;
                boolean pms;
                boolean discord_out;
                boolean discord_in;
                ChatColor color;
                boolean inDatabase;
                boolean chatEnabled;
                ArrayList<Integer> rewards;
                ArrayList<UUID> ignoredPlayers;

                if (set.next()) {
                    name = set.getString("name");
                    nick = set.getString("nickname");
                    pms = set.getBoolean("pms");
                    discord_out = set.getBoolean("discord_out");
                    discord_in = set.getBoolean("discord_in");
                    color = ChatColor.valueOf(set.getString("chat_color"));
                    chatEnabled = set.getBoolean("chat_enabled");
                    inDatabase = true;
                    level = set.getInt("level");
                    exp = set.getInt("experience");
                    money = set.getInt("money");
                    rewards = new Gson().fromJson(set.getString("unlocked_rewards"), new TypeToken<ArrayList<Integer>>() {}.getType());
                    ignoredPlayers = new Gson().fromJson(set.getString("ignored_players"), new TypeToken<ArrayList<UUID>>() {}.getType());
                } else {
                    nick = null;
                    name = null;
                    pms = true;
                    discord_in = true;
                    discord_out = true;
                    inDatabase = false;
                    color = ChatColor.WHITE;
                    chatEnabled = true;
                    level = 0;
                    exp = 0;
                    money = 0;
                    rewards = new ArrayList<>();
                    ignoredPlayers = new ArrayList<>();
                }

                return new ChatPlayer.Builder()
                        .setId(player)
                        .setName(name)
                        .setNickname(nick)
                        .setSettings(new Settings(discord_in,discord_out,pms,color,chatEnabled,ignoredPlayers))
                        .setStats(new Stats(level,exp,money,rewards))
                        .setInDatabase(inDatabase)
                        .build();
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });

        return future;
    }

    public CompletableFuture<UUID> getIDFromUsername(@NotNull String name) {
        CompletableFuture<UUID> future = new CompletableFuture<>();

        Objects.requireNonNull(name);

        ProxiedPlayer online = Main.get().getProxy().getPlayer(name);
        if (online != null) {
            future.complete(online.getUniqueId()); return future;
        }

        String sql = "SELECT id FROM players WHERE name = ?;";

        future.completeAsync(() -> {
            try (ResultSet set = database.query(sql, new Object[]{name})) {
                if (set.next()) {
                    return UUID.fromString(set.getString("id"));
                } else {
                    return null;
                }
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });

        return future;
    }

    public CompletableFuture<String> getUsernameFromID(@NotNull UUID id) {
        CompletableFuture<String> future = new CompletableFuture<>();

            Objects.requireNonNull(id);

            ProxiedPlayer online = Main.get().getProxy().getPlayer(id);
            if (online != null) {
                future.complete(online.getName());
                return future;
            }

            String sql = "SELECT name FROM players WHERE id = ?;";

            future.completeAsync(() -> {
                try (ResultSet set = database.query(sql, new Object[]{id.toString()})) {
                    if (set.next()) {
                        return set.getString("name");
                    } else {
                        return null;
                    }
                } catch (SQLException throwables) {
                    throw new CompletionException(throwables);
                }
            });
        return future;
    }
    /*future.completeAsync(() -> {
        try (ResultSet set = database.query(sql, new Object[]{id.toString()})) {
            if (set.next()) {

            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new CompletionException(e);
        }
    });*/

    public CompletableFuture<Ban> getActiveBan(@NotNull UUID id) {
        CompletableFuture<Ban> future = new CompletableFuture<>();

        Objects.requireNonNull(id);

        String sql = "SELECT * FROM player_active_punishment WHERE banned_id = ? AND type = 'ban';";

        future.completeAsync(() -> {
            try (ResultSet set = database.query(sql, new Object[]{id.toString()})) {
                if (set.next()) {
                    String sender = set.getString("sender_id");
                    return new Ban(set.getInt("id"),
                            sender != null ? UUID.fromString(set.getString("sender_id")) : null,
                            id,
                            set.getString("reason"),
                            set.getTimestamp("creation_date"),
                            set.getTimestamp("expiry_date"));
                } else {
                    return null;
                }
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });

        return future;
    }

    public CompletableFuture<Mute> getActiveMute(@NotNull UUID id) {
        CompletableFuture<Mute> future = new CompletableFuture<>();

        Objects.requireNonNull(id);

        String sql = "SELECT * FROM player_active_punishment WHERE banned_id = ? AND type = 'mute';";

        future.completeAsync(() -> {
            try (ResultSet set = database.query(sql, new Object[]{id.toString()})) {
                if (set.next()) {
                    String sender = set.getString("sender_id");
                    return new Mute(set.getInt("id"),
                            sender != null ? UUID.fromString(set.getString("sender_id")) : null,
                            id,
                            set.getString("reason"),
                            set.getTimestamp("creation_date"),
                            set.getTimestamp("expiry_date"));
                } else {
                    return null;
                }
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });

        return future;
    }

    public CompletableFuture<IpBan> getActiveIpBan(@NotNull String address) {
        CompletableFuture<IpBan> future = new CompletableFuture<>();

        Objects.requireNonNull(address);

        String sql = "SELECT * FROM player_active_ip_ban WHERE ip_address = ?;";

        future.completeAsync(() -> {
            try (ResultSet set = database.query(sql, new Object[]{address})) {
                if (set.next()) {
                    String sender = set.getString("sender_id");
                    return new IpBan(set.getInt("id"),
                            sender != null ? UUID.fromString(set.getString("sender_id")) : null,
                            set.getString("ip_address"),
                            set.getString("reason"),
                            set.getTimestamp("creation_date"),
                            set.getTimestamp("expiry_date"));
                } else {
                    return null;
                }
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });

        return future;
    }

    public CompletableFuture<ArrayList<String>> getAlts(@NotNull UUID id) {
        CompletableFuture<ArrayList<String>> future = new CompletableFuture<>();

        Objects.requireNonNull(id);

        String sql = "SELECT DISTINCT name FROM player_login WHERE ip_address IN (SELECT DISTINCT ip_address FROM player_login WHERE foreign_id = ?);";

        future.completeAsync(() -> {
            try (ResultSet set = database.query(sql, new Object[]{id.toString()})) {
                ArrayList<String> collectedString = new ArrayList<>();
                while (set.next()) {
                    String result = set.getString("name");
                    collectedString.add(result);
                }
                return collectedString;
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });

        return future;
    }

    public CompletableFuture<ArrayList<ComponentBuilder>> getPunishments(@NotNull UUID id) {
        CompletableFuture<ArrayList<ComponentBuilder>> future = new CompletableFuture<>();

        Objects.requireNonNull(id);

        String sql = "SELECT " +
                "(SELECT name FROM player_login pl WHERE pl.foreign_id = p.sender_id ORDER BY time DESC LIMIT 1) name, " +
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

        future.completeAsync(() -> {
            try (ResultSet set = database.query(sql, new Object[]{id.toString()})) {
                ArrayList<ComponentBuilder> collectedString = new ArrayList<>();
                while (set.next()) {

                    String raw = set.getString("name");
                    String name = raw == null ? "Console" : raw;
                    String reason = set.getString("reason");
                    Timestamp created = set.getTimestamp("creation_date");
                    Timestamp expiry = set.getTimestamp("expiry_date");
                    String type = set.getString("type");

                    ComponentBuilder builder = new ComponentBuilder(" ");
                    ComponentBuilder hoverBuilder = new ComponentBuilder("Reason: ").color(ChatColor.GRAY)
                            .append(reason).color(ChatColor.WHITE);
                    if (set.getObject("reverse_date") != null) {
                        builder.append("").strikethrough(true);
                        String reversed = set.getString("reverse_sender_id");
                        String reverseName = reversed == null ? "Console" : reversed;

                        hoverBuilder.append("\n").append("Reversed By: ").color(ChatColor.GRAY)
                                .append(reverseName).color(ChatColor.WHITE);

                        String reverseReason = set.getString("reverse_reason");
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

                return collectedString;
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });

        return future;
    }

    public CompletableFuture<ArrayList<String>> getBannedAlts(@NotNull UUID uuid) {
        CompletableFuture<ArrayList<String>> future = new CompletableFuture<>();

        Objects.requireNonNull(uuid);

        String sql = "SELECT DISTINCT name " +
                "FROM player_login " +
                "INNER JOIN (SELECT DISTINCT banned_id " +
                "FROM player_active_punishment " +
                "WHERE type = 'ban') AS b " +
                "WHERE player_login.foreign_id = b.banned_id " +
                "AND ip_address " +
                "IN (SELECT DISTINCT ip_address " +
                "FROM player_login " +
                "WHERE foreign_id = ?);";

        future.completeAsync(() -> {
            try (ResultSet set = database.query(sql, new Object[]{uuid.toString()})) {
                ArrayList<String> collectedString = new ArrayList<>();
                while (set.next()) {
                    collectedString.add(set.getString("name"));
                }
                return collectedString;
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });

        return future;
    }

    public static SQLStorage get() {
        return Objects.requireNonNullElseGet(instance, () -> instance = new SQLStorage());
    }
}
