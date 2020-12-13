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

public class ACCommand extends BungeeCommand {

    private final String commandName;

    public ACCommand(Main plugin, String nickname) {
        super(plugin, nickname, "elytraforce.admin");
        commandName = nickname;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ChatPlayer pp = PlayerController.get().getPlayer(((ProxiedPlayer) sender).getUniqueId());
            if (args.length == 0) {
                if (pp.getChatMode() != ChatMode.ADMIN) {
                    sender.sendMessage(getConfig().getPrefix() + ChatColor.GREEN + "Switched you to Admin Chat");
                    pp.setChatMode(ChatMode.ADMIN);
                } else {
                    sender.sendMessage(getConfig().getPrefix() + ChatColor.GREEN + "Switched you to Normal Chat");
                    pp.setChatMode(ChatMode.NORMAL);
                }
            } else {
                ChatController.get().sendAdmin(String.join(" ", args),pp);
                //send chat
            }
        }
    }

}