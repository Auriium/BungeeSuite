package com.elytraforce.bungeesuite;

import com.elytraforce.bungeesuite.announce.AnnounceController;
import com.elytraforce.bungeesuite.antiswear.Filters;
import com.elytraforce.bungeesuite.command.*;
import com.elytraforce.bungeesuite.config.PluginConfig;
import com.elytraforce.bungeesuite.discord.Discord;
import com.elytraforce.bungeesuite.listeners.*;
import com.elytraforce.bungeesuite.model.Ban;
import com.elytraforce.bungeesuite.model.IpBan;
import com.elytraforce.bungeesuite.model.Mute;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zaxxer.hikari.HikariDataSource;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import javax.sql.DataSource;

import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class Main extends Plugin {

    private static Main instance;
    private ChatSpyListener chatSpyListener;
    private PluginConfig config;
    
    private HikariDataSource hikariDataSource;
    private Map<UUID, Mute> activeMute = new ConcurrentHashMap<>();
    
    private Filters filters;
    
    public static Main get() { return instance; }
    public Filters getFilters() { return this.filters; }
    
    public ChatSpyListener getChatSpy() { return this.chatSpyListener; }
    
    @Override
    public void onDisable() {
    	
    }

    @Override
    public void onEnable() {
    	instance = this;
    	config = new PluginConfig();

        try {
            loadHikariDataSource();
            upgradeDatabase();
        } catch (IOException | SQLException e) {
        	getLogger().log(Level.SEVERE, "Data failed to load",e);
            return;
        }
        
        AnnounceController.get();

        getProxy().registerChannel("ConsoleBanUser");

        // Register commands
        getProxy().getPluginManager().registerCommand(this, new AltsCommand(this));
        getProxy().getPluginManager().registerCommand(this, new BanCommand(this));
        getProxy().getPluginManager().registerCommand(this, new InfoCommand(this));
        getProxy().getPluginManager().registerCommand(this, new MuteCommand(this));
        getProxy().getPluginManager().registerCommand(this, new UnbanCommand(this));
        getProxy().getPluginManager().registerCommand(this, new UnblacklistCommand(this));
        getProxy().getPluginManager().registerCommand(this, new UnmuteCommand(this));
        getProxy().getPluginManager().registerCommand(this, new WarnCommand(this));
        getProxy().getPluginManager().registerCommand(this, new SpyCommand(this));
        getProxy().getPluginManager().registerCommand(this, new MaintenanceCommand(this));
        getProxy().getPluginManager().registerCommand(this, new AnnounceCommand(this));

        getProxy().getPluginManager().registerListener(this, new PlayerActivityListener(this));
        getProxy().getPluginManager().registerListener(this, new ServerMessageListener(this));
        getProxy().getPluginManager().registerListener(this, new MOTDListener(this));
        getProxy().getPluginManager().registerListener(this, this.chatSpyListener = new ChatSpyListener(this));
        config.activate();
        
        new Discord(getConfig().getDiscordToken());
        
        this.filters = new Filters();
        
        
        getProxy().getPluginManager().registerListener(this, new DiscordListener(this, getConfig().getDiscordChannelID()));
        
    }

    // Creates the Hikari database connection
    private void loadHikariDataSource() throws IOException, SQLException {
        HikariDataSource dataSource = new HikariDataSource();
        getLogger().info("About to open connection with database " + getConfig().getDatabaseURL()
        + " user " + getConfig().getDatabaseUser() + " password " + getConfig().getDatabasePassword());
        dataSource.setJdbcUrl(getConfig().getDatabaseURL());
        dataSource.setUsername(getConfig().getDatabaseUser());
        dataSource.setPassword(getConfig().getDatabasePassword());
        dataSource.setMaximumPoolSize(getConfig().getDatabaseThreads());
        dataSource.setThreadFactory(new ThreadFactoryBuilder().setDaemon(true)
                .setNameFormat("hikari-sql-pool-%d").build());
        this.hikariDataSource = dataSource;
        this.upgradeDatabase();
    }

    // Creates and updates the SQL schema
    private void upgradeDatabase() throws IOException, SQLException {
        getLogger().info("Upgrading database using schema.ddl");
        InputStream schemaDdl = getResourceAsStream("schema.ddl");
        InputStreamReader schemaReader = new InputStreamReader(schemaDdl);

        Connection connection = hikariDataSource.getConnection();
        ScriptRunner runner = new ScriptRunner(connection); 
        runner.setLogWriter(null);
        runner.runScript(schemaReader);
        connection.close(); // Return the connection to the pool
        getLogger().info("Database successfully upgraded");
    }

    public void broadcast(String message, String permission) {
    	String last = ChatColor.translateAlternateColorCodes('&', message);
        getProxy().getPlayers().stream().filter(p -> p.hasPermission(permission))
                .forEach(p -> p.sendMessage(message));
        getProxy().getConsole().sendMessage(message);
    }

    public PluginConfig getConfig() {
        return this.config;
    }

    public DataSource getDatabase() {
        return hikariDataSource;
    }

    public UUID getUniqueId(CommandSender sender) {
        return sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getUniqueId() : null;
    }

    public String getUniqueIdSafe(CommandSender sender) {
        UUID uuid = getUniqueId(sender);
        return uuid == null ? null : uuid.toString();
    }

    public Ban getActiveBan(UUID banned) throws SQLException {
        try (Connection connection = hikariDataSource.getConnection()) {
            return getActiveBan(connection, banned);
        }
    }
    
    //TODO; do this later lmfao
    public boolean getDiscordEnabled(Connection connection, UUID id) throws SQLException{
    	try (PreparedStatement ps = connection.prepareStatement("SELECT * " +
                "FROM player_settings " +
                "WHERE banned_id = ? " +
                "AND type = 'ban'")) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("");
                } else {
                    return false;
                }
            }
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
    }

    public void registerMute(UUID player, Mute mute) {
        activeMute.put(player, mute);
    }

    public void unregisterMute(ProxiedPlayer player) {
        activeMute.remove(player.getUniqueId());
    }

    public Mute getActiveMute(ProxiedPlayer player) {
        Mute mute = activeMute.get(player.getUniqueId());
        if (mute != null) {
            // Check if the mute is expired
            if (mute.getExpiry() != null && mute.getExpiry().getTime() < System.currentTimeMillis()) {
                activeMute.remove(player.getUniqueId());
                return null;
            }
            return mute;
        }
        return null;
    }

    public Mute getActiveMute(UUID banned) throws SQLException {
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
    }
    
}
