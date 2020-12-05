package com.elytraforce.bungeesuite.command;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.localchat.ChatController;
import com.elytraforce.bungeesuite.localchat.model.ChatPlayer;
import com.elytraforce.bungeesuite.util.AuriBungeeUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NickCommand extends BungeeCommand{

    private String commandName;

    public NickCommand(Main plugin, String nickname) {
        super(plugin, nickname, "elytraforce.donator");
        commandName = nickname;
    }

    public boolean isGood(String string) {

        Pattern letter = Pattern.compile("[a-zA-z]");
        Pattern digit = Pattern.compile("[0-9]");
        Pattern special = Pattern.compile ("[!@#$%&*()_+=|<>?{}\\[\\]~-]");
        //Pattern eight = Pattern.compile (".{8}");


        Matcher hasLetter = letter.matcher(string);
        Matcher hasDigit = digit.matcher(string);
        Matcher hasSpecial = special.matcher(string);

        return hasLetter.find() && hasDigit.find() && hasSpecial.find();
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Usage: /" + commandName + " <name> / [off]");
            return;
        }
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            ChatPlayer pp = ChatController.get().getPlayer(player);

            String nick = args[0];

            AuriBungeeUtil.logError(nick);

            if (Main.get().getFilters().handleString(nick)) {
                sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "You cannot make your nickname that!");
                return;
            }

            if (nick.length() > 23) {
                sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Your nickname is too long!");
                return;
            }

            if (isGood(nick)) {
                sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Your nickname has special characters!");
                return;
            }

            if (nick.equalsIgnoreCase("off")) {
                pp.setNickname(null);
                sender.sendMessage(getConfig().getPrefix() + ChatColor.GREEN + "You have successfully removed your nickname");
            } else {
                pp.setNickname(nick);
                sender.sendMessage(getConfig().getPrefix() + ChatColor.GREEN + "You have successfully changed your nickname!");
            }

        }
    }

}
