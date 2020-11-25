package com.elytraforce.bungeesuite.command;

import com.elytraforce.bungeesuite.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;

public class AltsCommand extends BungeeCommand {

    public AltsCommand(Main plugin) {
        super(plugin, "alts", "elytraforce.helper");
    }

    @SuppressWarnings("deprecation")
	@Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Usage: /alts <player>");
            return;
        }

        getStorage().getIDFromUsername(args[0]).thenAccept(s -> getStorage().getAlts(s).thenAccept(names -> {
            if (names.isEmpty()) {
                sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Target player has no alternate accounts"); return;
            }
            sender.sendMessage(ChatColor.YELLOW + args[0] + " has shared an IP address with the following users (Including themselves):");
            sender.sendMessage(String.join(", ", names));
        }));

    }
}
