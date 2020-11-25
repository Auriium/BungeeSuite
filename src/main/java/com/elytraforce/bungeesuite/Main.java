package com.elytraforce.bungeesuite;

import com.elytraforce.bungeesuite.announce.AnnounceController;
import com.elytraforce.bungeesuite.announce.RestartController;
import com.elytraforce.bungeesuite.antiswear.Filters;
import com.elytraforce.bungeesuite.command.*;
import com.elytraforce.bungeesuite.config.PluginConfig;
import com.elytraforce.bungeesuite.discord.DiscordController;
import com.elytraforce.bungeesuite.elytracore.ElytraSQLStorage;
import com.elytraforce.bungeesuite.listeners.MOTDListener;
import com.elytraforce.bungeesuite.listeners.PlayerActivityListener;
import com.elytraforce.bungeesuite.localChat.ChatSpyListener;
import com.elytraforce.bungeesuite.punish.PunishController;
import com.elytraforce.bungeesuite.storage.SQLStorage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.UUID;

public class Main extends Plugin {

    private static Main instance;
    private PluginConfig config;
    private ChatSpyListener chatSpyListener;


    private Filters filters;

    public static Main get() { return instance; }
    public Filters getFilters() { return this.filters; }
    public ChatSpyListener getChatSpy() { return this.chatSpyListener; }

    @Override
    public void onDisable() {
        SQLStorage.get().shutdown();
        //shitcunpp.get().shutdown();
    }

    @Override
    public void onEnable() {
    	instance = this;

    	config = PluginConfig.get();

    	//register database
        SQLStorage.get();
        PunishController.get();

        getProxy().registerChannel("ConsoleBanUser");
        // Register commands
        getProxy().getPluginManager().registerCommand(this, new AltsCommand(this));
        getProxy().getPluginManager().registerCommand(this, new BanCommand(this));
        getProxy().getPluginManager().registerCommand(this, new InfoCommand(this));
        getProxy().getPluginManager().registerCommand(this, new MuteCommand(this));
        getProxy().getPluginManager().registerCommand(this, new UnbanCommand(this));
        getProxy().getPluginManager().registerCommand(this, new UnmuteCommand(this));
        getProxy().getPluginManager().registerCommand(this, new WarnCommand(this));
        getProxy().getPluginManager().registerCommand(this, new SpyCommand(this));
        getProxy().getPluginManager().registerCommand(this, new MaintenanceCommand(this));
        getProxy().getPluginManager().registerCommand(this, new AnnounceCommand(this));
        getProxy().getPluginManager().registerCommand(this, new KickCommand(this));

        getProxy().getPluginManager().registerListener(this, new PlayerActivityListener(this));
        getProxy().getPluginManager().registerListener(this, new MOTDListener(this));
        getProxy().getPluginManager().registerListener(this, chatSpyListener = new ChatSpyListener(this));

        AnnounceController.get();
        RestartController.get();
        DiscordController.get();

        if (config.useElytraCoreSupport()) {
            ElytraSQLStorage.get();
        }

        this.filters = new Filters();

    }

    // Creates the Hikari database connection
    /*private void loadHikariDataSource() throws IOException, SQLException {
        HikariDataSource dataSource = new HikariDataSource();
        getLogger().info("About to open connection with database " + config.getDatabaseURL()
        + " user " + config.getDatabaseUser() + " password " + config.getDatabasePassword());
        dataSource.setJdbcUrl(config.getDatabaseURL());
        dataSource.setUsername(config.getDatabaseUser());
        dataSource.setPassword(config.getDatabasePassword());
        dataSource.setMaximumPoolSize(config.getDatabaseThreads());
        dataSource.setThreadFactory(new ThreadFactoryBuilder().setDaemon(true)
                .setNameFormat("hikari-sql-pool-%d").build());
        this.hikariDataSource = dataSource;
        this.upgradeDatabase();
    }*/

    // Creates and updates the SQL schema
    /*private void upgradeDatabase() throws IOException, SQLException {
        getLogger().info("Upgrading database using schema.ddl");
        InputStream schemaDdl = getResourceAsStream("schema.ddl");
        InputStreamReader schemaReader = new InputStreamReader(schemaDdl);

        Connection connection = hikariDataSource.getConnection();
        ScriptRunner runner = new ScriptRunner(connection);
        runner.setLogWriter(null);
        runner.runScript(schemaReader);
        connection.close(); // Return the connection to the pool
        getLogger().info("Database successfully upgraded");
    }*/

    public void broadcast(String message, String permission) {
    	String last = ChatColor.translateAlternateColorCodes('&', message);
        getProxy().getPlayers().stream().filter(p -> p.hasPermission(permission))
                .forEach(p -> p.sendMessage(last));
        getProxy().getConsole().sendMessage(last);
    }

    public UUID getUniqueId(CommandSender sender) {
        return sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getUniqueId() : null;
    }

    public String getUniqueIdSafe(CommandSender sender) {
        UUID uuid = getUniqueId(sender);
        return uuid == null ? null : uuid.toString();
    }

    /*public Ban getActiveBan(UUID banned) throws SQLException {
        try (Connection connection = hikariDataSource.getConnection()) {
            return getActiveBan(connection, banned);
        }
    }

    public Ban getActiveBan(Connection connection, UUID banned) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * " +
                "FROM player_active_punishment " +
                "WHERE banned_id = ? " +
                "AND type = 'ban'")) {
            ps.setString(1, banned.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String sender = rs.getString("sender_id");
                    return new Ban(rs.getInt("id"),
                            sender != null ? UUID.fromString(rs.getString("sender_id")) : null,
                            banned,
                            rs.getString("reason"),
                            rs.getTimestamp("creation_date"),
                            rs.getTimestamp("expiry_date"));
                } else {
                    return null;
                }
            }
        }
    }*/



/*    public Mute getActiveMute(UUID banned) throws SQLException {
        try (Connection connection = hikariDataSource.getConnection()) {
            return getActiveMute(connection, banned);
        }
    }

    public Mute getActiveMute(Connection connection, UUID banned) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * " +
                "FROM player_active_punishment " +
                "WHERE banned_id = ? " +
                "AND type = 'mute'")) {
            ps.setString(1, banned.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ?
                        new Mute(rs.getInt("id"),
                                UUID.fromString(rs.getString("sender_id")),
                                banned,
                                rs.getString("reason"),
                                rs.getTimestamp("creation_date"),
                                rs.getTimestamp("expiry_date"))
                        : null;
            }
        }
    }

    public IpBan getActiveIpBan(String hostAddress) throws SQLException {
        try (Connection connection = hikariDataSource.getConnection()) {
            return getActiveIpBan(connection, hostAddress);
        }
    }

    public IpBan getActiveIpBan(Connection connection, String hostAddress) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * " +
                "FROM player_active_ip_ban " +
                "WHERE ip_address = ?")) {
            ps.setString(1, hostAddress);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ?
                        new IpBan(rs.getInt("id"),
                                UUID.fromString(rs.getString("sender_id")),
                                rs.getString("ip_address"),
                                rs.getString("reason"),
                                rs.getTimestamp("creation_date"),
                                rs.getTimestamp("expiry_date"))
                        : null;
            }
        }
    }*/

}
