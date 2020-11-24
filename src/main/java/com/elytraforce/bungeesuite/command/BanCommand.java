package com.elytraforce.bungeesuite.command;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.model.Ban;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BanCommand extends PunishCommand {

    public BanCommand(Main plugin) {
        super(plugin, "ban", "elytraforce.mod",
                "/ban <player> [duration] <reason>");
    }

    @Override
    public CompletableFuture<Ban> getExistingPunishment(UUID id) {
        return getStorage().getActiveBan(id);
    }

    @SuppressWarnings("deprecation")
	@Override
    public void issueNewPunishment(CommandSender sender, String targetName, UUID id, long expiry, String reason) {
        if (reason.toLowerCase().startsWith("blacklist")
            && sender != getPlugin().getProxy().getConsole()) {
            sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Blacklist bans may only be issued from the console");
            return;
        }

        getPunishController().banPlayer(sender,targetName,id,expiry,reason);
    }

}
