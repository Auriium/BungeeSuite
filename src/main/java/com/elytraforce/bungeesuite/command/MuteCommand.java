package com.elytraforce.bungeesuite.command;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.model.Mute;
import net.md_5.bungee.api.CommandSender;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MuteCommand extends PunishCommand {

    public MuteCommand(Main plugin) {
        super(plugin, "mute", "elytraforce.helper",
                "/mute <player> [duration] <reason>");
    }

    @Override
    public CompletableFuture<Mute> getExistingPunishment(UUID id) {
        return getStorage().getActiveMute(id);
    }

	@Override
    public void issueNewPunishment(CommandSender sender, String targetName, UUID id, long expiry, String reason) {
        getPunishController().mutePlayer(sender, targetName, id, expiry, reason);
    }
}
