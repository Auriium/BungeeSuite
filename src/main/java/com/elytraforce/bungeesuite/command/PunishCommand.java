package com.elytraforce.bungeesuite.command;

import com.elytraforce.aUtils.logger.BLogger;
import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.model.Punishment;
import com.elytraforce.bungeesuite.punish.PunishController;
import com.elytraforce.bungeesuite.util.TimeFormatUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class PunishCommand extends BungeeCommand {

    private final String usage;
    private final PunishController punishController;

    protected PunishCommand(Main plugin, String name, String permission, String usage) {
        super(plugin, name, permission);
        this.usage = ChatColor.RED + "Usage: " + usage;
        punishController = PunishController.get();
    }

    public PunishController getPunishController() { return punishController; }

    @SuppressWarnings("deprecation")
	@Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(getConfig().getPrefix() + usage);
            return;
        }
        getUuidFromArg(0, args).thenCompose(uuid -> {

            if (uuid == null) {
                sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "That player has never joined the server");
                return CompletableFuture.completedFuture(null);
            } else {
                CompletableFuture<? extends Punishment<UUID>> punishment = getExistingPunishment(uuid);
                punishment.thenAccept(pun -> {
                    BLogger.error("AFTER ACCEPTING PUNISH");
                    if (pun != null) {
                        sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "That user has already been dealt with");
                    } else {
                        String reason;
                        long expiryDate;
                        long duration = TimeFormatUtil.parseIntoMilliseconds(args[1]);
                        if (duration == -1) {
                            if (!sender.hasPermission("elytraforce.admin")) {
                                sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Please specify a valid duration");
                                sender.sendMessage(usage);
                                return;
                            }
                            expiryDate = -1;
                            reason = getReasonFromArgs(1, args);
                        } else if (args.length < 3) {
                            sender.sendMessage(usage);
                            return;
                        } else {
                            expiryDate = System.currentTimeMillis() + duration;
                            reason = getReasonFromArgs(2, args);
                        }

                        issueNewPunishment(sender, args[0], uuid, expiryDate, reason);
                    }
                });
                return punishment;
            }
        });
    }

    public abstract CompletableFuture<? extends Punishment<UUID>> getExistingPunishment(UUID id);
    
    public abstract void issueNewPunishment(CommandSender sender, String targetName, UUID id, long expiry, String reason);
}
