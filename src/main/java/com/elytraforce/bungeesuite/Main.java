package com.elytraforce.bungeesuite;

import codecrafter47.bungeetablistplus.api.bungee.BungeeTabListPlusAPI;
import com.elytraforce.aUtils.logger.BLogger;
import com.elytraforce.aUtils.util.BUtil;
import com.elytraforce.bungeesuite.announce.AnnounceController;
import com.elytraforce.bungeesuite.announce.RestartController;
import com.elytraforce.bungeesuite.antiswear.Filters;
import com.elytraforce.bungeesuite.command.*;
import com.elytraforce.bungeesuite.command.chat.*;
import com.elytraforce.bungeesuite.command.punish.BanCommand;
import com.elytraforce.bungeesuite.command.punish.KickCommand;
import com.elytraforce.bungeesuite.command.punish.MuteCommand;
import com.elytraforce.bungeesuite.command.punish.WarnCommand;
import com.elytraforce.bungeesuite.config.PluginConfig;
import com.elytraforce.bungeesuite.config.TestConfig;
import com.elytraforce.bungeesuite.discord.DiscordController;
import com.elytraforce.bungeesuite.hook.TabListVar;
import com.elytraforce.bungeesuite.listeners.MOTDListener;
import com.elytraforce.bungeesuite.listeners.PlayerActivityListener;
import com.elytraforce.bungeesuite.localchat.ChatController;
import com.elytraforce.bungeesuite.localchat.PlayerController;
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

    private Filters filters;

    public static Main get() { return instance; }
    public Filters getFilters() { return this.filters; }

    @Override
    public void onDisable() {
        SQLStorage.get().shutdown();
        PlayerController.get().shutdown();
    }

    @Override
    public void onEnable() {
    	instance = this;

        BUtil.register(this);
        BLogger.error("Initializing BungeeSuite - Loading BUtils");

        TestConfig fig = new TestConfig(); fig.create().load();

        BLogger.error(fig.cumGod);
    	config = PluginConfig.get();

    	//register database
        SQLStorage.get();
        PunishController.get();
        PlayerController.get();

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
        getProxy().getPluginManager().registerCommand(this, new MCCommand(this, "mc"));
        getProxy().getPluginManager().registerCommand(this, new MCCommand(this, "sc"));
        getProxy().getPluginManager().registerCommand(this, new ACCommand(this, "ac"));
        getProxy().getPluginManager().registerCommand(this, new GCCommand(this, "g"));
        getProxy().getPluginManager().registerCommand(this, new GCCommand(this, "global"));

        getProxy().getPluginManager().registerCommand(this, new PMCommand(this, "msg"));
        getProxy().getPluginManager().registerCommand(this, new PMCommand(this, "pm"));
        getProxy().getPluginManager().registerCommand(this, new ReplyCommand(this, "r"));
        getProxy().getPluginManager().registerCommand(this, new ReplyCommand(this, "reply"));
        getProxy().getPluginManager().registerCommand(this,new IgnoreCommand(this, "block"));
        getProxy().getPluginManager().registerCommand(this,new IgnoreCommand(this, "ignore"));

        getProxy().getPluginManager().registerListener(this, new PlayerActivityListener(this));
        getProxy().getPluginManager().registerListener(this, new MOTDListener(this));

        AnnounceController.get();
        RestartController.get();
        DiscordController.get();


        this.filters = new Filters();

    }

    @Override
    public void onLoad() {
            BungeeTabListPlusAPI.registerVariable(this, new TabListVar());
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
