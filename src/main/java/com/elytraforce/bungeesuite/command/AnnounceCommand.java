package com.elytraforce.bungeesuite.command;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.announce.AnnounceController;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class AnnounceCommand extends BungeeCommand {

	public AnnounceCommand(Main plugin) {
		super(plugin, "announce", "elytraforce.mod");
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) { return; }
		ProxiedPlayer player = (ProxiedPlayer) sender;
		
		 if (args.length < 2) {
	            sender.sendMessage(getPlugin().getConfig().getPrefix() + ChatColor.RED + "Usage: /announce <message/title> <message>");
	            return;
	     }
		 
		 StringBuilder sb = new StringBuilder();
	     for(int i = 1; i < args.length; i++) {
	    	 if (i > 1) sb.append(" ");
	    	 sb.append(args[i]);
	     }
		 
		 if (args[0].equalsIgnoreCase("message")) {
			 AnnounceController.get().announceString(sb.toString());
		 } else if (args[0].equalsIgnoreCase("title")) {
			 AnnounceController.get().announceTitle(sb.toString()); 
		 } else {
			 sender.sendMessage(getPlugin().getConfig().getPrefix() + ChatColor.RED + "Usage: /announce <message/title> <message>");
		 }
		
	}

}
