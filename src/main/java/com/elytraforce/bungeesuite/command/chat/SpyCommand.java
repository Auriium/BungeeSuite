package com.elytraforce.bungeesuite.command.chat;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.command.BungeeCommand;
import com.elytraforce.bungeesuite.localchat.ChatController;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class SpyCommand extends BungeeCommand {

	public SpyCommand(Main plugin) {
		super(plugin, "cspy", "elytraforce.admin");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCommand(CommandSender sender, String[] args) {
        if (args.length > 1) {
            sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Usage: /cspy");
            return;
        }
        
        if (sender == getPlugin().getProxy().getConsole()) { return; }
        
        ProxiedPlayer player = (ProxiedPlayer) sender;
        
        if (ChatController.get().getIsSpying(player)) {
			ChatController.get().disableSpy(player);
        	sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Chat Spy Disabled!");
        	
        } else {
			ChatController.get().enableSpy(player);
        	sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Chat Spy Enabled!");
        }

       
           
	}

}
