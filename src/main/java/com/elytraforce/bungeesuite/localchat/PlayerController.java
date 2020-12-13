package com.elytraforce.bungeesuite.localchat;

import com.elytraforce.aUtils.logger.BLogger;
import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.localchat.model.ChatMode;
import com.elytraforce.bungeesuite.localchat.player.ChatPlayer;
import com.elytraforce.bungeesuite.punish.PunishController;
import com.elytraforce.bungeesuite.storage.SQLStorage;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PlayerController {

    private final ArrayList<ChatPlayer> players;
    private final SQLStorage storage;
    private final PunishController punish;
    private final ChatController chat;

    private static PlayerController instance;

    public static PlayerController get() { return Objects.requireNonNullElseGet(instance, () -> instance = new PlayerController()); }

    public PlayerController() {
        players = new ArrayList<>();
        storage = SQLStorage.get();
        punish = PunishController.get();
        chat = ChatController.get();

        Main.get().getProxy().getScheduler().schedule(Main.get(), () -> {
            for (ChatPlayer player : players) {
                player.update();
            }
        },2L, TimeUnit.MINUTES);
    }

    public void handleLogin(LoginEvent event) {
        if (event.isCancelled()) { return; }

        Instant timestamp = Instant.now();

        UUID who = event.getConnection().getUniqueId();

        event.registerIntent(Main.get());

        storage.getOrDefaultPlayer(who).thenCompose(player -> {
            CompletableFuture<Void> future = new CompletableFuture<>();

            if (!player.isInDatabase()) {
                BLogger.error("Does not exist in database!");
                player.setName(event.getConnection().getName());
                future = storage.insertPlayer(player);
            } else {
                BLogger.error("exists!");
                future.complete(null);
            }

            return future.thenCompose(s -> {
                BLogger.error("beginning the thing");

                CompletableFuture<String> isDisallowed = PunishController.get().isDisallowed(who);
                return isDisallowed.thenCompose(reason -> {
                    if (reason == null) {
                        players.add(player);
                        storage.trackLogin(event.getConnection());
                        return storage.getActiveMute(who).thenAccept(mute -> {
                            if (mute != null) {
                                punish.registerMute(who,mute);
                            }
                        });

                    } else {
                        event.setCancelled(true);
                        event.setCancelReason(reason);
                        return CompletableFuture.completedFuture(null);
                    }
                });
            });
        }).whenComplete((n,e) -> {
            Instant timestamp2 = Instant.now();
            event.completeIntent(Main.get());

            BLogger.error(Duration.between(timestamp,timestamp2).toMillis() + " ms to complete!");

            if (e != null) {
                e.printStackTrace();
            }
        });
    }

    public void handleDC(PlayerDisconnectEvent event) {
        ChatPlayer player = getPlayer(event.getPlayer());
        if (player != null) {
            player.update();
            players.remove(player);
        }
        PunishController.get().unregisterMute(event.getPlayer());
    }

    public void onChat(ChatEvent event) {
        PunishController.get().handleChat(event);
        chat.handleCommandSpy(event);

        if (event.isCommand()) { return; }

        Main.get().getFilters().handleEvent(event);

        if (!event.isCancelled()) {
            event.setCancelled(true);

            //now we talk
            if (event.getSender() instanceof ProxiedPlayer) {
                ChatPlayer player = PlayerController.get().getPlayer((ProxiedPlayer) event.getSender());

                ChatMode mode = player.getChatMode();

                switch(mode) {
                    case NORMAL:
                        chat.sendNormal(event.getMessage(),player);
                        break;
                    case MOD:
                        chat.sendStaff(event.getMessage(),player);
                        break;
                    case ADMIN:
                        chat.sendAdmin(event.getMessage(),player);
                        break;
                    case GLOBAL:
                        chat.sendGlobal(event.getMessage(),player);
                        break;
                }
            }
        }
    }

    public ChatPlayer getPlayer(UUID uuid) {
        for (ChatPlayer p : this.players) {
            if (p.getUUID().equals(uuid)) {
                return p;
            }
        }
        return null;
    }

    public ChatPlayer getPlayer(ProxiedPlayer player) {
        return getPlayer(player.getUniqueId());
    }

    public ChatPlayer getPlayerAbsolute(UUID id) {
        if (Main.get().getProxy().getPlayer(id) != null) {
            return this.getPlayer(id);
        } else {
            return SQLStorage.get().getOrDefaultPlayer(id).join();
        }
    }

    public void shutdown() {
        for (ChatPlayer player : players) {
            player.update();
        }
    }

}
