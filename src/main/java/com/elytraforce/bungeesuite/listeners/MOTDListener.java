package com.elytraforce.bungeesuite.listeners;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.config.PluginConfig;
import com.elytraforce.bungeesuite.util.AuriBungeeUtil;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class MOTDListener implements Listener{

	private final Main plugin;
	private final PluginConfig config;

	public MOTDListener(Main main) {
		this.plugin = main;
		this.config = PluginConfig.get();
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onProxyPing(ProxyPingEvent event) {


		if (config.getMaintenance()) {
			event.getResponse().setDescription(AuriBungeeUtil.centerMOTD(config.getTopMOTD(),45) + "\n" + AuriBungeeUtil.centerMOTD("&c&lMAINTENANCE &7(Come back later!)",45));
			event.getResponse().setVersion(new ServerPing.Protocol("Maintenance!", 47));
		} else {
			event.getResponse().setDescription(AuriBungeeUtil.centerMOTD(config.getTopMOTD(),45) + "\n" + AuriBungeeUtil.centerMOTD(config.getBottomMOTD(),45));
			event.getResponse().setVersion(new ServerPing.Protocol("ElytraCord 1.15-RO2", 578));
		}
	}
}
