package com.elytraforce.bungeesuite.command;

import com.elytraforce.bungeesuite.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class SpyCommand extends BungeeCommand{

	public SpyCommand(Main plugin) {
		super(plugin, "cspy", "elytraforce.mod");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 0) {
            sender.sendMessage(getPlugin().getConfig().getPrefix() + ChatColor.RED + "Usage: /cspy");
            return;
        }
        
        if (sender == getPlugin().getProxy().getConsole()) { return; }
        
        ProxiedPlayer player = (ProxiedPlayer) sender;
        
        if (getPlugin().getChatSpy().getIsSpying(player)) {
        	getPlugin().getChatSpy().disableSpy(player);
        	sender.sendMessage(getPlugin().getConfig().getPrefix() + ChatColor.RED + "Chat Spy Disabled!");
        	
        } else {
        	getPlugin().getChatSpy().enableSpy(player);
        	sender.sendMessage(getPlugin().getConfig().getPrefix() + ChatColor.RED + "Chat Spy Enabled!");
        }

       
           
	}

}
