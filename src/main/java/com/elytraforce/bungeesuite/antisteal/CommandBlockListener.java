package com.elytraforce.bungeesuite.antisteal;

import java.util.List;
import com.elytraforce.bungeesuite.Main;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class CommandBlockListener implements Listener {
	
	private Main plugin;
	
	public CommandBlockListener(Main main) {
		this.plugin = main;
	}
	
	public BaseComponent[] transformString(final String string) {
        if (string == null) {
            throw new NullPointerException("string cannot be null");
        }
        return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', string));
    }
	
	public boolean equalsIgnoreCase(List<String> list, String searchString) {
        if (list == null || searchString == null) {
            return false;
        }
        if (searchString.isEmpty()) {
            return true;
        }
        for (String string : list) {
            if (string == null) {
                continue;
            }
            if (string.equalsIgnoreCase(searchString)) {
                return true;
            }
        }
        return false;
    }
	
	@EventHandler
    public void onPlayerChat(final ChatEvent event) {
		
        if (event.isCancelled()) {
            return;
        }
        if (!(event.getSender() instanceof ProxiedPlayer)) {
            return;
        }
        if (!event.isCommand()) {
            return;
        }
        final ProxiedPlayer player = (ProxiedPlayer)event.getSender();
        if (player.hasPermission("elytraforce.admin")) {
            return;
        }
        String command = event.getMessage().split(" ")[0].toLowerCase().replaceAll("/","");
        if (command.length() < 1) {
            return;
        }
        
        
        if (this.equalsIgnoreCase(plugin.getConfig().getBlockedCommands(), command)) {
            event.setCancelled(true);
            player.sendMessage("Unknown command. Type /help for help.");
            
        } else {
        }
    }
}
