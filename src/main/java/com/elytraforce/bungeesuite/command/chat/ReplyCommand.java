package com.elytraforce.bungeesuite.command.chat;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.command.BungeeCommand;
import com.elytraforce.bungeesuite.localchat.ChatController;
import com.elytraforce.bungeesuite.localchat.model.ChatPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ReplyCommand extends BungeeCommand {
    private final String commandName;

    public ReplyCommand(Main plugin, String nickname) {
        super(plugin, nickname, "elytraforce.default");
        commandName = nickname;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ChatPlayer pp = ChatController.get().getPlayer(((ProxiedPlayer) sender).getUniqueId());
            if (args.length == 0) {
                sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Usage: /" + commandName + " <message>");
                return;
            }

            //set target's reciever to the person sending this reply
            pp.getPmReciever().setPmReciever(pp);

            ChatController.get().sendReply(String.join(" ",args),pp);
        }
    }
}
