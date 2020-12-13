package com.elytraforce.bungeesuite.command.chat;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.command.BungeeCommand;
import com.elytraforce.bungeesuite.localchat.PlayerController;
import com.elytraforce.bungeesuite.localchat.player.ChatPlayer;
import com.elytraforce.bungeesuite.storage.SQLStorage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class IgnoreCommand extends BungeeCommand {

    private final String commandName;

    public IgnoreCommand(Main plugin, String nickname) {
        super(plugin, nickname, "elytraforce.default");
        commandName = nickname;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Usage: /" + commandName + " <name>");
            return;
        }

        if (sender instanceof ProxiedPlayer) {
            ChatPlayer pp = PlayerController.get().getPlayer(((ProxiedPlayer) sender).getUniqueId());

            CompletableFuture<UUID> future = new CompletableFuture<UUID>();

            if (Main.get().getProxy().getPlayer(args[0]) == null) {
                future = SQLStorage.get().getIDFromUsername(args[0]);
            } else  {
                future.complete(Main.get().getProxy().getPlayer(args[0]).getUniqueId());
            }
            future.thenAccept(id -> {
                if (id == null) {
                    sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "That isn't a valid player!");
                    return;
                }

                if (pp.getSettings().getIgnoredPlayers().contains(id)) {
                    sender.sendMessage(getConfig().getPrefix() + ChatColor.GRAY + "Successfully un-ignored player " + ChatColor.GREEN + args[0]);
                    pp.getSettings().getIgnoredPlayers().remove(id);
                } else {
                    sender.sendMessage(getConfig().getPrefix() + ChatColor.GRAY + "Successfully ignored player " + ChatColor.GREEN + args[0]);
                    pp.getSettings().getIgnoredPlayers().add(id);
                }


            });
        }

    }

}
