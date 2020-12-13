package com.elytraforce.bungeesuite.punish;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.config.PluginConfig;
import com.elytraforce.bungeesuite.model.Ban;
import com.elytraforce.bungeesuite.model.Mute;
import com.elytraforce.bungeesuite.storage.SQLStorage;
import com.elytraforce.bungeesuite.util.TimeFormatUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@SuppressWarnings("deprecated")
public class PunishController {

    private static PunishController instance;
    private static SQLStorage storage;
    private static Main main;
    private Map<UUID, Mute> activeMute = new ConcurrentHashMap<>();
    private PluginConfig config;

    private PunishController() {
        storage = SQLStorage.get();
        main = Main.get();
        config = PluginConfig.get();
    }

    private boolean isMutedCommand(String fullCommand) {
        String[] split = fullCommand.split(" ");
        for (int i = split.length ; i >= 0 ; i--) {
            String command = Arrays.stream(split, 0, i).collect(Collectors.joining(" "));
            if (config.getMuteCommands().contains(command.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public void handleChat(ChatEvent event) {
        if (event.isCancelled() || !(event.getSender() instanceof ProxiedPlayer)) { return; }
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        Mute mute = this.getActiveMute(player);
        if (mute == null) { return; }

        if (event.isCommand()) {
            if (isMutedCommand(event.getMessage())) {
                event.setCancelled(true);
                player.sendMessage(config.getPrefix() + ChatColor.RED + "You cannot use this command while muted");
            }
            return;
        }

        event.setCancelled(true);
        long expiry = mute.getExpiry() == null ? -1 : mute.getExpiry().getTime();
        if (expiry == -1) {
            player.sendMessage(config.getPrefix() + ChatColor.RED + "You are permanently muted");
        } else {
            player.sendMessage(config.getPrefix() + ChatColor.RED + "Your mute expires in " + TimeFormatUtil.toDetailedDate(expiry));
        }
    }

    public void registerMute(UUID player, Mute mute) {
        activeMute.put(player, mute);
    }

    public void unregisterMute(ProxiedPlayer player) {
        activeMute.remove(player.getUniqueId());
    }

    public Mute getActiveMute(ProxiedPlayer player) {
        Mute mute = activeMute.get(player.getUniqueId());
        if (mute != null) {
            // Check if the mute is expired
            if (mute.getExpiry() != null && mute.getExpiry().getTime() < System.currentTimeMillis()) {
                activeMute.remove(player.getUniqueId());
                return null;
            }
            return mute;
        }
        return null;
    }

    public static PunishController get() {
       return Objects.requireNonNullElseGet(instance, () -> instance = new PunishController());
    }

    public void warnPlayer(CommandSender sender, String targetName, UUID id, String reason) {
        storage.warnPlayer(main.getUniqueIdSafe(sender),targetName,id,reason);
        ProxiedPlayer target = main.getProxy().getPlayer(id);
        if (target != null) {
            target.sendMessage(PluginConfig.get().getPrefix() + ChatColor.RED + "You were warned by " + sender.getName() + " for (" + reason + ")");
        }

        String name = target == null ? targetName : target.getName();
        // Broadcast full message
        main.broadcast(ChatColor.RED + String.format(PluginConfig.get().getPrefix() + "%s was warned by %s for (%s)", name, sender.getName(), reason), "elytraforce.helper");

    }

    public void mutePlayer(CommandSender sender, String targetName, UUID id, long expiry, String reason) {
        storage.mutePlayer(Main.get().getUniqueIdSafe(sender), targetName,id,expiry,reason).thenAccept(integer -> {
            //this happens after the callback is done
            Timestamp created = new Timestamp(System.currentTimeMillis());

            ProxiedPlayer target = main.getProxy().getPlayer(id);
            String timeFormatted = expiry == -1 ? "Permanent" : TimeFormatUtil.toDetailedDate(expiry, true);

            storage.getActiveMute(id).thenAccept(mute -> {
                assert mute != null;
                if (target != null) {
                    registerMute(id, mute);
                    target.sendMessage(String.format(PluginConfig.get().getPrefix() + ChatColor.RED + "You were muted by %s for %s (%s)", sender.getName(), reason, timeFormatted));
                }
                String name = target == null ? targetName : target.getName();
                main.broadcast(ChatColor.RED + String.format(PluginConfig.get().getPrefix() + "%s was muted by %s for %s (%s)", name, sender.getName(), reason, timeFormatted), "elytraforce.helper");

            });
        });
    }

    public void kickPlayer(CommandSender sender, String targetName, UUID id, String reason) {
        storage.kickPlayer(main.getUniqueIdSafe(sender),targetName,id,reason);
        ProxiedPlayer target = main.getProxy().getPlayer(id);
        if (target == null) { return; }

        target.disconnect(ChatColor.translateAlternateColorCodes('&',
                String.format("&cYou were kicked from &b&lElytra&f&lForce" +
                        "\n\n&cAuthor: &7%s" +
                        "\n&cReason: &7%s" +
                        "\n\n&c&lAppeal at &7elytraforce.com", sender.getName(), reason)));


        main.broadcast(ChatColor.RED + String.format(PluginConfig.get().getPrefix() + "%s was kicked by %s for (%s)", targetName, sender.getName(), reason), "elytraforce.helper");
    }
    public void banPlayer(CommandSender sender, String targetName, UUID id, long expiry, String reason) {
        storage.banPlayer(main.getUniqueIdSafe(sender),targetName,id,expiry,reason);

        ProxiedPlayer target = main.getProxy().getPlayer(id);
        String timeFormatted = expiry == -1 ? "Permanent" : TimeFormatUtil.toDetailedDate(expiry, true);

        if (target != null) {
            target.disconnect(ChatColor.translateAlternateColorCodes('&',
                    String.format("&cYou are banned from &b&lElytra&f&lForce" +
                            "\n\n&cAuthor: &7%s" +
                            "\n\n&cReason: &7%s" +
                            "\nExpires: &7%s" +
                            "\n\n&c&lAppeal at &7elytraforce.com", sender.getName(), reason, timeFormatted)));
        }
        String name = target == null ? targetName : target.getName();
        // Broadcast full message
        main.broadcast(PluginConfig.get().getPrefix() + ChatColor.RED + String.format("%s was banned by %s for %s (%s)", name, sender.getName(), reason, timeFormatted), "elytraforce.helper");
    }

    public CompletableFuture<String> isDisallowed(UUID uuid) {
        CompletableFuture<ArrayList<String>> isAltBanned = storage.getBannedAlts(uuid);
        CompletableFuture<Ban> isBanned = storage.getActiveBan(uuid);

        return CompletableFuture.allOf(isAltBanned,isBanned).thenApply((ignored) -> {
            Ban ban = isBanned.join();
            ArrayList<String> altBanned = isAltBanned.join();

            if (LockoutController.isLockdown()) {
                return ChatColor.translateAlternateColorCodes('&',
                        "&cDisconnected from &b&lElytra&f&lForce" +
                                "\n\n&cWe are undergoing maintenance!" +
                                "\nPlease come back in a little while!" +
                                "\n\n&c&lChat with us at &7discord.elytraforce.com");
            }
            else if (isAltBanned.join().size() > 3) {
                String formatted = altBanned.stream().limit(3).collect(Collectors.joining("\n&c"));

                return ChatColor.translateAlternateColorCodes('&',
                        String.format("&cYou are banned on too many accounts!" +
                                "\n&c%s" +
                                "\n&c..." +
                                "\n&c&lAppeal at &7elytraforce.com", formatted));
            }
            else if (isBanned.join() != null) {
                String expiry = ban.getExpiry() == null ? "Permanent" : TimeFormatUtil.toDetailedDate(ban.getExpiry().getTime());

                return ChatColor.translateAlternateColorCodes('&',
                    String.format("&cYou are banned from &b&lElytra&f&lForce" +
                            "\n\n&cReason: &7%s" +
                            "\nExpires: &7%s" +
                            "\n\n&c&lAppeal at &7elytraforce.com", ban.getReason(), expiry));

            }
            else { return null; }
        });

    }

}
