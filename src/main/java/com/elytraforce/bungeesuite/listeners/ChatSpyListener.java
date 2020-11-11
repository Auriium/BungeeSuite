package com.elytraforce.bungeesuite.listeners;

import java.util.HashMap;
import com.elytraforce.bungeesuite.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class ChatSpyListener implements Listener{
	private HashMap<ProxiedPlayer, Boolean> spyMap;
	private Main instance;
	
	public ChatSpyListener(Main main) {
		this.spyMap = new HashMap<>();
		this.instance = main;
	}
	
	public void enableSpy(ProxiedPlayer player) {
		spyMap.put(player, true);
	}
	
	public void disableSpy(ProxiedPlayer player) {
		spyMap.put(player, false);
	}
	
	public boolean getIsSpying(ProxiedPlayer player) {
		if (!spyMap.containsKey(player)) {
			return false;
		} else {
			return spyMap.get(player);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onSwear(ChatEvent event) {
		Main.get().getFilters().handleEvent(event);
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
						player.sendMessage(Main.get().getConfig().getPrefix() + ChatColor.translateAlternateColorCodes(
								'&', "&7(" + sender.getServer().getInfo().getName() + "&7) &b" + sender.getName() + "&f: &7" + event.getMessage()));
					} else {
						return;
					}
				} else {
					player.sendMessage(Main.get().getConfig().getPrefix() + ChatColor.translateAlternateColorCodes(
							'&', "&7(" + sender.getServer().getInfo().getName() + "&7) &b" + sender.getName() + "&f: &7" + event.getMessage()));
				}
				
				
			}
		}
	}
}
