package com.elytraforce.bungeesuite.command;

import com.elytraforce.bungeesuite.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;

public class UnbanCommand extends BungeeCommand {

    public UnbanCommand(Main plugin) {
        super(plugin, "unban", "elytraforce.admin");
    }

    @SuppressWarnings("deprecation")
	@Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            // TODO: Better usage messages
            sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Usage: /unban <player> [reason]");
            return;
        }

        getUuidFromArg(0,args).thenAccept(uuid -> {
            if (uuid == null) {
                sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "That player has never joined the server");
            } else {
                getStorage().getActiveBan(uuid).thenAccept(ban -> {
                    if (ban == null) {
                        sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "That user is not banned");
                    } else {
                        String reason = args.length > 1 ? getReasonFromArgs(1, args) : null;

                        if (reason == null && !sender.hasPermission("elytraforce.admin")) { // Check sender perms
                            sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Please specify a valid unban reason");
                            sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Usage: /unban <player> [reason]");
                            return;
                        }

                        getStorage().unBanPlayer(ban,sender,reason);

                        getPlugin().broadcast(getConfig().getPrefix() + ChatColor.RED + String.format("%s was unbanned by %s", args[0], sender.getName()), "elytraforce.helper");
                    }
                });
            }
        });
    }
}
