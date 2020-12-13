package com.elytraforce.bungeesuite.command.chat;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.command.BungeeCommand;
import com.elytraforce.bungeesuite.localchat.ChatController;
import com.elytraforce.bungeesuite.localchat.PlayerController;
import com.elytraforce.bungeesuite.localchat.model.ChatMode;
import com.elytraforce.bungeesuite.localchat.player.ChatPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class GCCommand extends BungeeCommand {
    private final String commandName;

    public GCCommand(Main plugin, String nickname) {
        super(plugin, nickname, "elytraforce.donator");
        commandName = nickname;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ChatPlayer pp = PlayerController.get().getPlayer(((ProxiedPlayer) sender).getUniqueId());
            if (args.length == 0) {
                if (pp.getChatMode() != ChatMode.GLOBAL) {
                    sender.sendMessage(getConfig().getPrefix() + ChatColor.GREEN + "Switched you to Global Chat");
                    pp.setChatMode(ChatMode.GLOBAL);
                } else {
                    sender.sendMessage(getConfig().getPrefix() + ChatColor.GREEN + "Switched you to Normal Chat");
                    pp.setChatMode(ChatMode.NORMAL);
                }
            } else {
                ChatController.get().sendGlobal(String.join(" ", args),pp);
                //send chat
            }
        }
    }
}
