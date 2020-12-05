package com.elytraforce.bungeesuite.localchat;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.config.PluginConfig;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.HashMap;

//TODO: add this to chatcontroller
public class ChatSpyListener implements Listener{
	private HashMap<ProxiedPlayer, Boolean> spyMap;
	private Main instance;
	private PluginConfig config;
	
	public ChatSpyListener(Main main) {
		this.spyMap = new HashMap<>();
		this.instance = main;
		this.config = PluginConfig.get();
	}
	
	public void enableSpy(ProxiedPlayer player) {
		spyMap.put(player, true);
	}
	
	public void disableSpy(ProxiedPlayer player) {
		spyMap.put(player, false);
	}
	
	public boolean getIsSpying(ProxiedPlayer player) {
		return spyMap.getOrDefault(player, false);
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGH) //must be higher than antiswear priority.
	public void onPlayerChat(ChatEvent event) {
		if (event.isCommand()) {
			if (event.getMessage().startsWith("/mc") || event.getMessage().startsWith("/ac")) { return; }
		}
		
		ProxiedPlayer sender = (ProxiedPlayer) event.getSender();
		
		for (ProxiedPlayer player : Main.get().getProxy().getPlayers()) {
			
			if (sender.equals(player)) { continue; }
			
			if (getIsSpying(player)) {
				if (sender.hasPermission("elytraforce.admin")) {
					if (player.hasPermission("elytraforce.owner")) {
						player.sendMessage(config.getPrefix() + ChatColor.translateAlternateColorCodes(
								'&', "&7(" + sender.getServer().getInfo().getName() + "&7) &b" + sender.getName() + "&f: &7" + event.getMessage()));
					} else {
						return;
					}
				} else {
					player.sendMessage(config.getPrefix() + ChatColor.translateAlternateColorCodes(
							'&', "&7(" + sender.getServer().getInfo().getName() + "&7) &b" + sender.getName() + "&f: &7" + event.getMessage()));
				}
				
				
			}
		}
	}
}
