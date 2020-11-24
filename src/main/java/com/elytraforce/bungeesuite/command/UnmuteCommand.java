package com.elytraforce.bungeesuite.command;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.punish.PunishController;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class UnmuteCommand extends BungeeCommand {

    public UnmuteCommand(Main plugin) {
        super(plugin, "unmute", "elytraforce.mod");
    }

    @SuppressWarnings("deprecation")
	@Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            // TODO: Better usage messages
            sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Usage: /unmute <player> [reason]");
            return;
        }

        getUuidFromArg(0,args).thenAccept(uuid -> {
           if (uuid == null) {
               sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "That player has never joined the server");
           } else {
               getStorage().getActiveMute(uuid).thenAccept(mute -> {
                   if (mute == null) {
                       sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "That user is not muted");
                   } else {
                       String reason = args.length > 1 ? getReasonFromArgs(1, args) : null;

                       if (reason == null && !sender.hasPermission("elytraforce.admin")) { // Check sender perms
                           sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Please specify a valid unmute reason");
                           sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Usage: /unmute <player> [reason]");
                           return;
                       }

                       getStorage().unMutePlayer(mute,sender,reason);

                       ProxiedPlayer target = getPlugin().getProxy().getPlayer(uuid);
                       if (target != null) {
                           target.sendMessage(getConfig().getPrefix() + ChatColor.GREEN + "You were unmuted by " + sender.getName());
                           PunishController.get().unregisterMute(target);
                       }
                       String name = target == null ? args[0] : target.getName();
                       getPlugin().broadcast(getConfig().getPrefix() + ChatColor.RED + String.format("%s was unmuted by %s", name, sender.getName()), "elytraforce.mod");
                   }
               });
           }
        });
    }
}
