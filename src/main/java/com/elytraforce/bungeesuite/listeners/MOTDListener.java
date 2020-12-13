package com.elytraforce.bungeesuite.listeners;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.config.PluginConfig;
import net.md_5.bungee.api.ChatColor;
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
			event.getResponse().setDescription(centerMOTD(config.getTopMOTD(),45) + "\n" + centerMOTD("&c&lMAINTENANCE &7(Come back later!)",45));
			event.getResponse().setVersion(new ServerPing.Protocol("Maintenance!", 47));
		} else {
			event.getResponse().setDescription(centerMOTD(config.getTopMOTD(),45) + "\n" + centerMOTD(config.getBottomMOTD(),45));
			event.getResponse().setVersion(new ServerPing.Protocol("ElytraCord 1.15-RO2", 578));
		}
	}

	public static String centerMOTD(String text, int lineLength) {
		String pex = ChatColor.translateAlternateColorCodes('&', text);
		char[] chars = pex.toCharArray(); // Get a list of all characters in text
		boolean isBold = false;
		double length = 0;
		ChatColor pholder = null;
		for (int i = 0; i < chars.length; i++) { // Loop through all characters
			// Check if the character is a ColorCode..
			if (chars[i] == '&' && chars.length != (i + 1) && (pholder = ChatColor.getByChar(chars[i + 1])) != null) {
				if (pholder != ChatColor.UNDERLINE && pholder != ChatColor.ITALIC
						&& pholder != ChatColor.STRIKETHROUGH && pholder != ChatColor.MAGIC) {
					isBold = (chars[i + 1] == 'l'); // Setting bold  to true or false, depending on if the ChatColor is Bold.
					i += isBold ? 1 : 0;
				}
			} else {
				// If the character is not a color code:
				length++; // Adding a space
				length += (isBold ? (chars[i] != ' ' ? 0.1555555555555556 : 0) : 0); // Adding 0.156 spaces if the character is bold.
			}
		}

		double spaces = (lineLength - length) / 2; // Getting the spaces to add by (max line length - length) / 2

		// Adding the spaces
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < spaces; i++) {
			builder.append(' ');
		}
		String copy = builder.toString();
		builder.append(pex).append(copy);

		return builder.toString();
	}
}
