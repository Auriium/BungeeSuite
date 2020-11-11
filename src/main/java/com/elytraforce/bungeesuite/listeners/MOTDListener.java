package com.elytraforce.bungeesuite.listeners;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.util.AuriBungeeUtil;

import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class MOTDListener implements Listener{
	
	private Main plugin;
	
	public MOTDListener(Main main) {
		this.plugin = main;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onProxyPing(ProxyPingEvent event) {
		
		
		if (plugin.getConfig().getMaintenance()) {
			event.getResponse().setDescription(AuriBungeeUtil.centerMOTD(plugin.getConfig().getTopMOTD(),45) + "\n" + AuriBungeeUtil.centerMOTD("&c&lMAINTENANCE &7(Come back later!)",45));
			event.getResponse().setVersion(new ServerPing.Protocol("Maintenance!", 47));
		} else {
			event.getResponse().setDescription(AuriBungeeUtil.centerMOTD(plugin.getConfig().getTopMOTD(),45) + "\n" + AuriBungeeUtil.centerMOTD(plugin.getConfig().getBottomMOTD(),45));
			event.getResponse().setVersion(new ServerPing.Protocol("ElytraCord 1.15-RO2", 578));
		}
	}
}
