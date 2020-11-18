package com.elytraforce.bungeesuite.command;

import com.elytraforce.bungeesuite.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;

public class MaintenanceCommand extends BungeeCommand {

    public MaintenanceCommand(Main plugin) {
        super(plugin, "lockdown", "elytraforce.admin");
    }

    @SuppressWarnings("deprecation")
	@Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
        	sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Usage: /lockdown <on/off>");
            return;
        }
        
        if (args[0].equalsIgnoreCase("on")) {
			if (getConfig().getMaintenance()) {
        		sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Lockdown Mode is already on!");
        		return;
        	}
        	sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Lockdown Mode On!");
        	getConfig().setMaintenance(true);
    		getPlugin().getProxy().getPlayers().stream().filter(p -> !p.hasPermission("elytraforce.helper"))
            .forEach(p -> p.disconnect(ChatColor.translateAlternateColorCodes('&',
                    String.format("&cDisconnected from &b&lElytra&f&lForce" +
                            "\n\n&cWe are undergoing maintenance!" +
                            "\nPlease come back in a little while!" +
                            "\n\n&c&lChat with us at &7discord.elytraforce.com"))));
        } else if (args[0].equalsIgnoreCase("off")) {
			if (!getConfig().getMaintenance()) {
        		sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Lockdown Mode is already off!");
        		return;
        	}
        	
			getConfig().setMaintenance(false);
        	sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Lockdown Mode Off!");
        } else {
        	sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Usage: /lockdown <on/off>");
        }
 
    }

}