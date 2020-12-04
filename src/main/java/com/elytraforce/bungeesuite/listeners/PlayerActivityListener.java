package com.elytraforce.bungeesuite.listeners;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.config.PluginConfig;
import com.elytraforce.bungeesuite.discord.DiscordController;
import com.elytraforce.bungeesuite.localchat.ChatController;
import com.elytraforce.bungeesuite.punish.PunishController;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class PlayerActivityListener implements Listener {

    private final Main plugin;
    private final PluginConfig config;
    private final PunishController punishController;

    public PlayerActivityListener(Main plugin) {
        this.plugin = plugin;
        this.config = PluginConfig.get();
        this.punishController = PunishController.get();
    }

	@EventHandler
    public void onPostLogin(PostLoginEvent event) {
    	if (config.getMaintenance()) {
         	ProxiedPlayer checkPlayer = event.getPlayer();
        	if (!checkPlayer.hasPermission("elytraforce.helper")) {
        		checkPlayer.disconnect(ChatColor.translateAlternateColorCodes('&',
                        String.format("&cDisconnected from &b&lElytra&f&lForce" +
                                "\n\n&cWe are undergoing maintenance!" +
                                "\nPlease come back in a little while!" +
                                "\n\n&c&lChat with us at &7discord.elytraforce.com")));
        		return;
        	}

        }
        ChatController.get().handleEvent(event);
        DiscordController.get().onPlayerJoin(event);
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        punishController.handleLogin(event);
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        punishController.handleDC(event);
        DiscordController.get().onPlayerLeave(event);
    }

    @EventHandler
    public void onChatEvent(ChatEvent event) {
        ChatController.get().onChat(event);
    }
}
