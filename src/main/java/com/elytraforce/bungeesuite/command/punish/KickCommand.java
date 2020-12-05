package com.elytraforce.bungeesuite.command.punish;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.command.BungeeCommand;
import com.elytraforce.bungeesuite.punish.PunishController;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class KickCommand extends BungeeCommand {

    public KickCommand(Main plugin) {
        super(plugin, "kick", "elytraforce.helper");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "/kick <player> <reason>");
            return;
        }

        this.getUuidFromArg(0,args).thenAccept(uuid -> {
            ProxiedPlayer target = getPlugin().getProxy().getPlayer(uuid);

            if (target == null) {
                sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "That player is not online!");
            } else {
                String reason = getReasonFromArgs(1, args);
                PunishController.get().kickPlayer(sender,target.getName(),uuid,reason);
            }
        });
    }
}

