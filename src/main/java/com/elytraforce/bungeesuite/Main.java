package com.elytraforce.bungeesuite;

import com.elytraforce.bungeesuite.announce.AnnounceController;
import com.elytraforce.bungeesuite.announce.RestartController;
import com.elytraforce.bungeesuite.antiswear.Filters;
import com.elytraforce.bungeesuite.command.*;
import com.elytraforce.bungeesuite.config.PluginConfig;
import com.elytraforce.bungeesuite.discord.DiscordController;
import com.elytraforce.bungeesuite.listeners.MOTDListener;
import com.elytraforce.bungeesuite.listeners.PlayerActivityListener;
import com.elytraforce.bungeesuite.localchat.ChatController;
import com.elytraforce.bungeesuite.localchat.ChatSpyListener;
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
        ChatController.get().shutdown();
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

        getProxy().getPluginManager().registerCommand(this, new NickCommand(this,"nick"));
        getProxy().getPluginManager().registerCommand(this, new NickCommand(this,"nickname"));

        getProxy().getPluginManager().registerListener(this, new PlayerActivityListener(this));
        getProxy().getPluginManager().registerListener(this, new MOTDListener(this));
        getProxy().getPluginManager().registerListener(this, chatSpyListener = new ChatSpyListener(this));

        AnnounceController.get();
        RestartController.get();
        DiscordController.get();


        this.filters = new Filters();

    }

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

}
