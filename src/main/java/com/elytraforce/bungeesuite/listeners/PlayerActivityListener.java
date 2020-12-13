package com.elytraforce.bungeesuite.listeners;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.config.PluginConfig;
import com.elytraforce.bungeesuite.discord.DiscordController;
import com.elytraforce.bungeesuite.localchat.ChatController;
import com.elytraforce.bungeesuite.localchat.PlayerController;
import com.elytraforce.bungeesuite.punish.PunishController;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerActivityListener implements Listener {

    private final Main plugin;
    private final PluginConfig config;

    public PlayerActivityListener(Main plugin) {
        this.plugin = plugin;
        this.config = PluginConfig.get();
    }

	@EventHandler
    public void onPostLogin(PostLoginEvent event) {
        DiscordController.get().onPlayerJoin(event);
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        PlayerController.get().handleLogin(event);
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        DiscordController.get().onPlayerLeave(event);
        PlayerController.get().handleDC(event);
    }

    @EventHandler
    public void onChatEvent(ChatEvent event) {
        PlayerController.get().onChat(event);
    }
}
