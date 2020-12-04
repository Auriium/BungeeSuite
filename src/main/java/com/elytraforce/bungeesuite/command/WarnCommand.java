package com.elytraforce.bungeesuite.command;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.punish.PunishController;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class WarnCommand extends BungeeCommand {

    public WarnCommand(Main plugin) {
        super(plugin, "warn", "elytraforce.helper");
        // "/mute <player> <reason>"
    }

    @SuppressWarnings("deprecation")
	@Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "/warn <player> <reason>");
            return;
        }

        getUuidFromArg(0,args).thenAccept(uuid -> {
            ProxiedPlayer target = getPlugin().getProxy().getPlayer(uuid);
            if (uuid == null) {
                sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "That player has never joined the server");
            } else {
                String reason = getReasonFromArgs(1, args);
                PunishController.get().warnPlayer(sender,target.getName(),uuid,reason);
            }
        });

        // ban <player> [duration] <reason>
    }
}
