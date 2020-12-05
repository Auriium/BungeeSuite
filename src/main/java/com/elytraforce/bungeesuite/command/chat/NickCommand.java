package com.elytraforce.bungeesuite.command.chat;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.command.BungeeCommand;
import com.elytraforce.bungeesuite.localchat.ChatController;
import com.elytraforce.bungeesuite.localchat.model.ChatPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NickCommand extends BungeeCommand {

    private final String commandName;

    public NickCommand(Main plugin, String nickname) {
        super(plugin, nickname, "elytraforce.donator");
        commandName = nickname;
    }

    public boolean isBad(String string) {

        Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(string);

        return m.find();

    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length > 2 || args.length < 1) {
            sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Usage: /" + commandName + " (target) <name>/[off]");
            return;
        }

        if (sender instanceof ProxiedPlayer) {
            ChatPlayer pp = ChatController.get().getPlayer(((ProxiedPlayer) sender).getUniqueId());

            ChatPlayer target;
            String nick;

            if (args.length == 1) {
                nick = args[0];
                target = pp;
            } else {
                nick = args[1];
                if (Main.get().getProxy().getPlayer(args[0]) == null) {
                    sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "That isn't a valid player!");
                    return;
                } else  {
                    target = ChatController.get().getPlayer(Main.get().getProxy().getPlayer(args[0]));
                }
            }

            if (Main.get().getFilters().handleString(nick)) {
                sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "You cannot make a nickname that!");
                return;
            }

            if (nick.length() > 23) {
                sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Nickname is too long!");
                return;
            }

            if (isBad(nick)) {
                sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Nickname has special characters!");
                return;
            }

            if (nick.equalsIgnoreCase("off")) {
                target.setNickname(null);
                sender.sendMessage(getConfig().getPrefix() + ChatColor.GREEN + "You have successfully removed a nickname");
            } else {
                target.setNickname(nick);
                sender.sendMessage(getConfig().getPrefix() + ChatColor.GREEN + "You have successfully changed a nickname!");
            }
        }
    }

}
