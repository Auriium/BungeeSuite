package com.elytraforce.bungeesuite.command.chat;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.command.BungeeCommand;
import com.elytraforce.bungeesuite.localchat.ChatController;
import com.elytraforce.bungeesuite.localchat.PlayerController;
import com.elytraforce.bungeesuite.localchat.player.ChatPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PMCommand extends BungeeCommand {
    private final String commandName;

    public PMCommand(Main plugin, String nickname) {
        super(plugin, nickname, "elytraforce.default");
        commandName = nickname;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ChatPlayer pp = PlayerController.get().getPlayer(((ProxiedPlayer) sender).getUniqueId());
            if (args.length < 2) {
                sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Usage: /" + commandName + " <player> <message>");
                return;
            }

            if (Main.get().getProxy().getPlayer(args[0]) == null) {
                sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "That isn't a valid player!");
                return;
            }

            StringBuilder sb = new StringBuilder();
            for(int i = 1; i < args.length; i++) {
                if (i > 0) sb.append(" ");
                sb.append(args[i]);
            }

            ChatPlayer target = PlayerController.get().getPlayer(Main.get().getProxy().getPlayer(args[0]));

            pp.setPmReciever(target);
            target.setPmReciever(pp);

            ChatController.get().sendPM(sb.toString(),pp,target);
        }
    }
}
